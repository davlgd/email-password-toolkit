package http

import cats.effect.IO
import sttp.tapir.{endpoint, Endpoint, PublicEndpoint, stringToPath, header, query}
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.statusCode
import io.circe.generic.auto.*
import validation.{EmailValidator, PasswordStrengthAnalyzer, PasswordGenerator}
import dto.{EmailValidationRequest, PasswordValidationRequest, EmailValidationResponse, PasswordStrengthResponse, PasswordGenerationResponse, ErrorResponse}
import security.BearerAuth

object ValidationEndpoints:

  val validateEmailEndpoint: Endpoint[Option[String], EmailValidationRequest, Unit, EmailValidationResponse, Any] =
    endpoint
      .post
      .in("valid" / "email")
      .securityIn(header[Option[String]]("Authorization"))
      .in(jsonBody[EmailValidationRequest])
      .out(jsonBody[EmailValidationResponse])
      .errorOut(statusCode(sttp.model.StatusCode.Unauthorized))

  val validateEmailServerEndpoint = validateEmailEndpoint
    .serverSecurityLogic[Unit, IO](authHeader => IO.pure(security.BearerAuth.extractToken(authHeader)))
    .serverLogic(_ => request =>
      IO.pure(Right(EmailValidationResponse(EmailValidator.isValid(request.email))))
    )

  val validatePasswordEndpoint: Endpoint[Option[String], PasswordValidationRequest, Unit, PasswordStrengthResponse, Any] =
    endpoint
      .post
      .in("valid" / "password")
      .securityIn(header[Option[String]]("Authorization"))
      .in(jsonBody[PasswordValidationRequest])
      .out(jsonBody[PasswordStrengthResponse])
      .errorOut(statusCode(sttp.model.StatusCode.Unauthorized))

  val validatePasswordServerEndpoint = validatePasswordEndpoint
    .serverSecurityLogic[Unit, IO](authHeader => IO.pure(security.BearerAuth.extractToken(authHeader)))
    .serverLogic(_ => request =>
      val result = PasswordStrengthAnalyzer.analyze(request.password)
      IO.pure(Right(PasswordStrengthResponse(result.entropy, result.strength, result.valid, result.errors)))
    )

  val generatePasswordEndpoint: PublicEndpoint[Option[Int], ErrorResponse, PasswordGenerationResponse, Any] =
    endpoint
      .get
      .in("generate" / "password")
      .in(query[Option[Int]]("length"))
      .out(jsonBody[PasswordGenerationResponse])
      .errorOut(jsonBody[ErrorResponse])
      .errorOut(statusCode(sttp.model.StatusCode.BadRequest))

  val generatePasswordServerEndpoint = generatePasswordEndpoint
    .serverLogic { lengthOpt =>
      IO {
        PasswordGenerator
          .generate(lengthOpt.getOrElse(32))
          .map { password =>
            val result = PasswordStrengthAnalyzer.analyze(password)
            PasswordGenerationResponse(password, result.entropy, result.strength, result.valid)
          }
          .left.map(ErrorResponse.apply)
      }
    }

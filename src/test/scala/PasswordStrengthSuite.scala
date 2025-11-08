import cats.effect.IO
import munit.CatsEffectSuite
import org.http4s.*
import org.http4s.implicits.*
import org.http4s.circe.*
import org.typelevel.ci.CIString
import io.circe.Json
import io.circe.syntax.*
import io.circe.generic.auto.*

class PasswordStrengthSuite extends CatsEffectSuite {
  test("POST /valid/password without bearer token returns 401") {
    val json = Json.obj("password" -> Json.fromString("Password123"))
    val request = Request[IO](Method.POST, uri"/valid/password")
      .withEntity(json)
    val response = Main.httpApp.run(request)

    response.map { resp =>
      assertEquals(resp.status, Status.Unauthorized)
    }
  }

  test("POST /valid/password with strong password returns high entropy") {
    val json = Json.obj("password" -> Json.fromString("Tr0ng!P@ssw0rd#2024VerySecure!"))
    val request = Request[IO](Method.POST, uri"/valid/password")
      .withEntity(json)
      .putHeaders(Header.Raw(CIString("Authorization"), "Bearer valid-token"))
    val response = Main.httpApp.run(request)

    response.flatMap { resp =>
      resp.asJson.map { json =>
        assertEquals(resp.status, Status.Ok)
        assert(json.hcursor.get[Double]("entropy").isRight)
        assertEquals(json.hcursor.get[String]("strength"), Right("strong"))
        assertEquals(json.hcursor.get[Boolean]("valid"), Right(true))
      }
    }
  }

  test("POST /valid/password with weak password returns low entropy") {
    val json = Json.obj("password" -> Json.fromString("Pass1!"))
    val request = Request[IO](Method.POST, uri"/valid/password")
      .withEntity(json)
      .putHeaders(Header.Raw(CIString("Authorization"), "Bearer valid-token"))
    val response = Main.httpApp.run(request)

    response.flatMap { resp =>
      resp.asJson.map { json =>
        assertEquals(resp.status, Status.Ok)
        assert(json.hcursor.get[Double]("entropy").isRight)
        assertEquals(json.hcursor.get[String]("strength"), Right("weak"))
        assertEquals(json.hcursor.get[Boolean]("valid"), Right(false))
      }
    }
  }

  test("POST /valid/password with medium password returns medium entropy") {
    val json = Json.obj("password" -> Json.fromString("MyPass1!Word"))
    val request = Request[IO](Method.POST, uri"/valid/password")
      .withEntity(json)
      .putHeaders(Header.Raw(CIString("Authorization"), "Bearer valid-token"))
    val response = Main.httpApp.run(request)

    response.flatMap { resp =>
      resp.asJson.map { json =>
        assertEquals(resp.status, Status.Ok)
        assert(json.hcursor.get[Double]("entropy").isRight)
        assertEquals(json.hcursor.get[String]("strength"), Right("medium"))
        assertEquals(json.hcursor.get[Boolean]("valid"), Right(false))
      }
    }
  }

  test("POST /valid/password with 64-character password returns very high entropy") {
    val json = Json.obj("password" -> Json.fromString("aB3$xY9#mN2@pQ7!rT5&vW8*zK4%jL6^hF1+dG0-sC9=uE2~iO4"))
    val request = Request[IO](Method.POST, uri"/valid/password")
      .withEntity(json)
      .putHeaders(Header.Raw(CIString("Authorization"), "Bearer valid-token"))
    val response = Main.httpApp.run(request)

    response.flatMap { resp =>
      resp.asJson.map { json =>
        assertEquals(resp.status, Status.Ok)
        assert(json.hcursor.get[Double]("entropy").isRight)
        assertEquals(json.hcursor.get[String]("strength"), Right("strong"))
        assertEquals(json.hcursor.get[Boolean]("valid"), Right(true))
      }
    }
  }
}

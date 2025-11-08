import cats.effect.IO
import munit.CatsEffectSuite
import org.http4s.*
import org.http4s.implicits.*
import org.http4s.circe.*
import org.typelevel.ci.CIString
import io.circe.Json
import io.circe.syntax.*
import io.circe.generic.auto.*

class EmailValidationSuite extends CatsEffectSuite {
  test("POST /valid/email without bearer token returns 401") {
    val json = Json.obj("email" -> Json.fromString("user@example.com"))
    val request = Request[IO](Method.POST, uri"/valid/email")
      .withEntity(json)
    val response = Main.httpApp.run(request)

    response.map { resp =>
      assertEquals(resp.status, Status.Unauthorized)
    }
  }

  test("POST /valid/email with invalid bearer token returns 401") {
    val json = Json.obj("email" -> Json.fromString("user@example.com"))
    val request = Request[IO](Method.POST, uri"/valid/email")
      .withEntity(json)
      .putHeaders(Header.Raw(CIString("Authorization"), "Bearer invalid-token"))
    val response = Main.httpApp.run(request)

    response.map { resp =>
      assertEquals(resp.status, Status.Unauthorized)
    }
  }

  test("POST /valid/email with valid email returns valid: true") {
    val json = Json.obj("email" -> Json.fromString("user@example.com"))
    val request = Request[IO](Method.POST, uri"/valid/email")
      .withEntity(json)
      .putHeaders(Header.Raw(CIString("Authorization"), "Bearer valid-token"))
    val response = Main.httpApp.run(request)

    response.flatMap { resp =>
      resp.asJson.map { json =>
        assertEquals(resp.status, Status.Ok)
        assertEquals(json.hcursor.get[Boolean]("valid"), Right(true))
      }
    }
  }

  test("POST /valid/email with invalid email returns valid: false") {
    val json = Json.obj("email" -> Json.fromString("invalid-email"))
    val request = Request[IO](Method.POST, uri"/valid/email")
      .withEntity(json)
      .putHeaders(Header.Raw(CIString("Authorization"), "Bearer valid-token"))
    val response = Main.httpApp.run(request)

    response.flatMap { resp =>
      resp.asJson.map { json =>
        assertEquals(resp.status, Status.Ok)
        assertEquals(json.hcursor.get[Boolean]("valid"), Right(false))
      }
    }
  }

  test("POST /valid/email with email missing @ returns valid: false") {
    val json = Json.obj("email" -> Json.fromString("userexample.com"))
    val request = Request[IO](Method.POST, uri"/valid/email")
      .withEntity(json)
      .putHeaders(Header.Raw(CIString("Authorization"), "Bearer valid-token"))
    val response = Main.httpApp.run(request)

    response.flatMap { resp =>
      resp.asJson.map { json =>
        assertEquals(resp.status, Status.Ok)
        assertEquals(json.hcursor.get[Boolean]("valid"), Right(false))
      }
    }
  }
}

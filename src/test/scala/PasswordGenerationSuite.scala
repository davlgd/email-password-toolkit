import cats.effect.IO
import munit.CatsEffectSuite
import org.http4s.*
import org.http4s.implicits.*
import org.http4s.circe.*
import io.circe.Json

class PasswordGenerationSuite extends CatsEffectSuite {
  test("GET /generate/password returns a valid strong password with default length 32") {
    val request = Request[IO](Method.GET, uri"/generate/password")
    val response = Main.httpApp.run(request)

    response.flatMap { resp =>
      resp.asJson.map { json =>
        assertEquals(resp.status, Status.Ok)

        val password = json.hcursor.get[String]("password")
        assert(password.isRight, "Response should contain password field")

        val pwd = password.getOrElse("")
        assertEquals(pwd.length, 32, s"Default password length should be 32 characters")
        assert(pwd.exists(_.isUpper), "Password should contain uppercase letters")
        assert(pwd.exists(_.isLower), "Password should contain lowercase letters")
        assert(pwd.exists(_.isDigit), "Password should contain digits")
        assert(pwd.exists(c => !c.isLetterOrDigit), "Password should contain special characters")
        assert(!pwd.exists(_.isWhitespace), "Password should not contain whitespace")

        val entropy = json.hcursor.get[Double]("entropy")
        assert(entropy.isRight, "Response should contain entropy field")
        assert(entropy.getOrElse(0.0) >= 32.0, "Generated password should have strong entropy (>= 32)")

        val strength = json.hcursor.get[String]("strength")
        assertEquals(strength, Right("strong"), "Generated password should be strong")

        val valid = json.hcursor.get[Boolean]("valid")
        assertEquals(valid, Right(true), "Generated password should be valid")
      }
    }
  }

  test("GET /generate/password?length=24 returns a 24-character password") {
    val request = Request[IO](Method.GET, uri"/generate/password?length=24")
    val response = Main.httpApp.run(request)

    response.flatMap { resp =>
      resp.asJson.map { json =>
        assertEquals(resp.status, Status.Ok)

        val password = json.hcursor.get[String]("password").getOrElse("")
        assertEquals(password.length, 24, "Password should be 24 characters")

        val valid = json.hcursor.get[Boolean]("valid")
        assertEquals(valid, Right(true), "Generated password should be valid")
      }
    }
  }

  test("GET /generate/password?length=128 returns a 128-character password") {
    val request = Request[IO](Method.GET, uri"/generate/password?length=128")
    val response = Main.httpApp.run(request)

    response.flatMap { resp =>
      resp.asJson.map { json =>
        assertEquals(resp.status, Status.Ok)

        val password = json.hcursor.get[String]("password").getOrElse("")
        assertEquals(password.length, 128, "Password should be 128 characters")

        val valid = json.hcursor.get[Boolean]("valid")
        assertEquals(valid, Right(true), "Generated password should be valid")
      }
    }
  }

  test("GET /generate/password?length=8 returns 400 Bad Request") {
    val request = Request[IO](Method.GET, uri"/generate/password?length=8")
    val response = Main.httpApp.run(request)

    response.map { resp =>
      assertEquals(resp.status, Status.BadRequest)
    }
  }

  test("GET /generate/password?length=200 returns 400 Bad Request") {
    val request = Request[IO](Method.GET, uri"/generate/password?length=200")
    val response = Main.httpApp.run(request)

    response.map { resp =>
      assertEquals(resp.status, Status.BadRequest)
    }
  }

  test("GET /generate/password returns different passwords on multiple calls") {
    val request1 = Request[IO](Method.GET, uri"/generate/password")
    val request2 = Request[IO](Method.GET, uri"/generate/password")

    for {
      resp1 <- Main.httpApp.run(request1)
      json1 <- resp1.asJson
      password1 = json1.hcursor.get[String]("password").getOrElse("")

      resp2 <- Main.httpApp.run(request2)
      json2 <- resp2.asJson
      password2 = json2.hcursor.get[String]("password").getOrElse("")
    } yield {
      assert(password1 != password2, "Consecutive password generations should produce different passwords")
    }
  }
}

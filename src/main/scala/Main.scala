import cats.effect.*
import org.http4s.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import com.comcast.ip4s.*
import sttp.tapir.*
import sttp.tapir.server.http4s.Http4sServerInterpreter

object Main extends IOApp:

  // Définition de l'endpoint Tapir pour /api
  val helloEndpoint = endpoint
    .get
    .in("api")
    .out(stringBody)
    .serverLogicSuccess[IO](_ => IO.pure("Hello, World!"))

  // Conversion de l'endpoint Tapir en routes http4s
  val routes = Http4sServerInterpreter[IO]()
    .toRoutes(helloEndpoint)

  val httpApp = Router("/" -> routes).orNotFound

  // Démarrage du serveur
  def run(args: List[String]): IO[ExitCode] =
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(httpApp)
      .build
      .use(_ => IO.println("Serveur démarré sur http://localhost:8080/api") *> IO.never)
      .as(ExitCode.Success)

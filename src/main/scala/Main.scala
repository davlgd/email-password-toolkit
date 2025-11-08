import cats.effect.{IO, IOApp, ExitCode}
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import com.comcast.ip4s.{ipv4, Port}
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.swagger.SwaggerUIOptions
import http.ValidationEndpoints

object Main extends IOApp:

  private val apiEndpoints = List(
    ValidationEndpoints.validateEmailEndpoint,
    ValidationEndpoints.validatePasswordEndpoint,
    ValidationEndpoints.generatePasswordEndpoint
  )

  private val swaggerEndpoints = SwaggerInterpreter(
    swaggerUIOptions = SwaggerUIOptions.default.copy(pathPrefix = List("docs"), yamlName = "openapi.yaml")
  ).fromEndpoints[IO](apiEndpoints, "TDD API", "0.1.0")

  private val serverEndpoints = List(
    ValidationEndpoints.validateEmailServerEndpoint,
    ValidationEndpoints.validatePasswordServerEndpoint,
    ValidationEndpoints.generatePasswordServerEndpoint
  )

  private val routes = Http4sServerInterpreter[IO]()
    .toRoutes(serverEndpoints ++ swaggerEndpoints)

  val httpApp = Router("/" -> routes).orNotFound

  private def getPort: Int =
    sys.props.get("http.port").flatMap(_.toIntOption).getOrElse(8080)

  def run(args: List[String]): IO[ExitCode] =
    Port.fromInt(getPort).fold(
      IO.raiseError(new IllegalArgumentException(s"Invalid port: $getPort"))
    ) { port =>
      EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port)
        .withHttpApp(httpApp)
        .build
        .use(_ => IO.println(s"Server started on http://localhost:$getPort") *> IO.never)
        .as(ExitCode.Success)
    }

import cats.effect.*
import org.http4s.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import com.comcast.ip4s.*
import sttp.tapir.*
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.swagger.SwaggerUIOptions

object Main extends IOApp:

  val helloEndpoint: PublicEndpoint[Unit, Unit, String, Any] = endpoint
    .get
    .in("api")
    .out(stringBody)

  val helloServerEndpoint = helloEndpoint
    .serverLogicSuccess[IO](_ => IO.pure("Hello, World!"))

  val apiEndpoints = List(helloEndpoint)

  val swaggerEndpoints = SwaggerInterpreter(
    swaggerUIOptions = SwaggerUIOptions.default.copy(pathPrefix = List("docs"), yamlName = "openapi.yaml")
  ).fromEndpoints[IO](apiEndpoints, "TDD API", "0.1.0")

  val routes = Http4sServerInterpreter[IO]()
    .toRoutes(helloServerEndpoint :: swaggerEndpoints)

  val httpApp = Router("/" -> routes).orNotFound

  def run(args: List[String]): IO[ExitCode] =
    val serverPort = sys.props.get("http.port").flatMap(_.toIntOption).getOrElse(8080)

    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(Port.fromInt(serverPort).get)
      .withHttpApp(httpApp)
      .build
      .use(_ => IO.println(s"Server started on http://localhost:$serverPort/api") *> IO.never)
      .as(ExitCode.Success)

import zio._
import zio.json._
import zio.http._
import zio.stream._
import scala.util.Try

import zio._

import zio.http._
import zio.http.netty.NettyConfig
import zio.http.ChannelEvent.{ExceptionCaught, Read, UserEvent, UserEventTriggered}

case class Credentials(username: String, password: String)
object Credentials {
  implicit val encoder: JsonEncoder[Credentials] = DeriveJsonEncoder.gen[Credentials]
  implicit val decoder: JsonDecoder[Credentials] = DeriveJsonDecoder.gen[Credentials]
}

sealed trait AppError extends Throwable

object AppError {

  case object MissingBodyError extends AppError

  final case class JsonDecodingError(message: String) extends AppError

  final case class InvalidIdError(message: String) extends AppError

}

object HelloWorld extends ZIOAppDefault {
  def parseBody[A: JsonDecoder](request: Request): IO[AppError, A] =
    for {
      body   <- request.body.asString.orElseFail(AppError.MissingBodyError)
      parsed <- ZIO.from(body.fromJson[A]).mapError(AppError.JsonDecodingError)
    } yield parsed

  val routes: Http[Any, Throwable, Request, Response] = Http.collectZIO[Request] {
  case req @ Method.POST -> Root / "login" =>
      for {
        cred <- parseBody[Credentials](req)
      } yield Response.json(cred.toJson)
  }

  val socketApp: SocketApp[Any] =
    Handler.webSocket { channel =>
      channel.receiveAll {
        case Read(WebSocketFrame.Text("end")) => {
          Console.printLine("Ending") *> channel.shutdown
        }

        // Send a "bar" if the server sends a "foo"
        case Read(WebSocketFrame.Text("foo")) =>
          channel.send(Read(WebSocketFrame.text("bar")))

        // Send a "foo" if the server sends a "bar"
        case Read(WebSocketFrame.Text("bar")) =>
          channel.send(Read(WebSocketFrame.text("foo")))

        // Echo the same message 10 times if it's not "foo" or "bar"
        case Read(WebSocketFrame.Text(text)) => {
          Console.printLine(s"Received: $text") *> channel.send(Read(WebSocketFrame.text(text))).repeatN(3)
        }

        // Send a "greeting" message to the server once the connection is established
        case UserEventTriggered(UserEvent.HandshakeComplete) =>
          channel.send(Read(WebSocketFrame.text("Greetings!")))

        // Log when the channel is getting closed
        case Read(WebSocketFrame.Close(status, reason)) =>
          Console.printLine("Closing channel with status: " + status + " and reason: " + reason)

        // Print the exception if it's not a normal close
        case ExceptionCaught(cause) =>
          Console.printLine(s"Channel error!: ${cause.getMessage}")

        case _ =>
          ZIO.unit
      }
    }
  
  val app1: Handler[Any, Nothing, Any, Response] = Handler.text("Hello World")
  val app2: Handler[Any, Throwable, Request, Response] = Handler.fromFunctionZIO[Request] {request => 
      request.body.asString.map(s => Response.json(Credentials("foo", "bar").toJson))
  }

  val app = Http.collectHandler[Request] {
    case Method.GET -> Root => app1
    case Method.GET -> Root / "hello" => app1
    case Method.POST -> Root / "login2" => app2
  }

  val appWS: Http[Any, Nothing, Request, Response] =
    Http.collectZIO[Request] {
      case Method.GET -> Root / "subscriptions" => socketApp.toResponse
    }


  //override val run = Server.serve(app.withDefaultErrorResponse).provide(Server.defaultWithPort(8888))
  //override val run = Server.serve(app.mapError{ str => Response(Status.InternalServerError, body = Body.fromString(str.getMessage))}).provide(Server.defaultWithPort(8888))
  val run = ZIOAppArgs.getArgs.flatMap { args =>
    // Configure thread count using CLI
    val nThreads: Int = args.headOption.flatMap(x => Try(x.toInt).toOption).getOrElse(20)

    val config           = Server.Config.default.port(8888)
    val nettyConfig      = NettyConfig.default
      //.leakDetection(LeakDetectionLevel.PARANOID)
      .maxThreads(nThreads)
    val configLayer      = ZLayer.succeed(config)
    val nettyConfigLayer = ZLayer.succeed(nettyConfig)

    (Server.install({app ++ routes ++ appWS}.withDefaultErrorResponse).flatMap { port =>
      Console.printLine(s"Started server on port: $port")
    } *> ZIO.never)
      .provide(configLayer, nettyConfigLayer, Server.customized)
  }
}

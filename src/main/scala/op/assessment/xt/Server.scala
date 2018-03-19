package op.assessment.xt

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor}

object Server extends App with ApiRoutes {

  implicit val system: ActorSystem = ActorSystem("RecommendationSystem")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val useVideoRepo = system.actorOf(UserVideoActor.props)

  Http().bindAndHandle(routes, "localhost", 8085)
  println(s"Server online at http://localhost:8085/")

  Await.result(system.whenTerminated, Duration.Inf)
}

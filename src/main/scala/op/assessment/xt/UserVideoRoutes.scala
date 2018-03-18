package op.assessment.xt

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import op.assessment.sn.JsonSupport
import op.assessment.xt.UserVideoRoutes.{Register, User}

object UserVideoRoutes {

  final case class User(
      name: String,
      email: String,
      age: Int,
      gender: Int
    )

  final case class Register(userId: Long, videoId: Long)

  final case class UserAction(
      userId: Long,
      videoId: Long,
      action: Int
    )

  final case class Errors(errs: List[String])
}

trait UserVideoRoutes extends JsonSupport {

  lazy val routes: Route = path("register") {
    post {
      entity(as[User]) { u =>
        complete((StatusCodes.OK, Register(userId = 9797345L, videoId = 4324556L)))
      }
    }
  }
}

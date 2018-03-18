package op.assessment.xt

import akka.actor.Actor
import op.assessment.xt.UserVideoRoutes.User

object UseVideoRepo {
  case class RegisterUser(user: User)

  sealed trait ActionResult
  case class UserRecommendation(userId: Long, videoId: Long) extends ActionResult
  case class UserNotExist(userId: Long) extends ActionResult
}

class UseVideoRepo extends Actor {
  override def receive: Receive = ???
}

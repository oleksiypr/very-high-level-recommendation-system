package op.assessment.xt

import akka.actor.Actor
import op.assessment.xt.UserVideoRoutes.User

object UseVideoRepo {
  case class RegisterUser(user: User)
}

class UseVideoRepo extends Actor {
  override def receive: Receive = ???
}

package op.assessment.xt

import akka.actor.Actor
import op.assessment.xt.UserVideoRoutes.User

object UseVideoRepo {

  final case class RegisterUser(user: User)
  final case class UserAction(
      userId: Long,
      videoId: Long,
      action: Int
    )

  sealed trait ActionResult
  final case class UserRecommendation(userId: Long, videoId: Long) extends ActionResult
  final case class UserNotExist(userId: Long) extends ActionResult
  final case class VideoNotCorrespond(videoId: Long, lastVideoId: Long) extends ActionResult
}

class UseVideoRepo extends Actor {
  override def receive: Receive = ???
}

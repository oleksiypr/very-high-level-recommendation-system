package op.assessment.xt

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import op.assessment.xt.UserActivityTracker.{Track, TrackResult, Tracked, UnableToTrackVideo}
import op.assessment.xt.UserVideoActor._
import op.assessment.xt.ApiRoutes.User
import scala.concurrent.duration._
import scala.util.Random

object UserVideoActor {

  def props: Props = Props[UserVideoRepo]

  sealed trait UserVideoCommand
  final case class RegisterUser(user: User) extends UserVideoCommand
  final case class UserAction(userId: Long, videoId: Long, actionId: Int) extends UserVideoCommand

  sealed trait ActionResult
  final case class UserRecommendation(userId: Long, videoId: Long) extends ActionResult
  final case class UserNotExist(userId: Long) extends ActionResult
  final case class VideoNotCorrespond(videoId: Long, lastVideoId: Long) extends ActionResult
}

trait UserVideoActor extends Actor {

  type UserId = Long
  type VideoId = Long
  type ActivityTracker = ActorRef
  type Repo = Map[UserId, ActivityTracker]

  private[this] var users: Repo = Map.empty
  private[this] val videos = Array.fill(10)(Random.nextLong)

  private[this] var leastUserId: UserId = 0L
  private[this] var leastVideoIndex: Int = Random.nextInt(10)

  def trackerProps(userId: Long, videoId: Long): Props

  implicit val timeout: Timeout = 5.seconds
  import context.dispatcher

  val log = Logging(context.system, this)

  val receive: Receive = {
    case RegisterUser(user) =>
      log.info("New user: {}", user)
      val userId = nextUserId()
      val videoId = leastVideo()
      val tracker = context.actorOf(trackerProps(userId, videoId))
      users += userId -> tracker
      sender ! UserRecommendation(userId, videoId)

    case UserAction(u, v, a) if users.contains(u) =>
      val tracker = users(u)
      val least = leastVideo()
      (tracker ? Track(v, least, a)).mapTo[TrackResult] map {
        case Tracked(userId, videoId) => UserRecommendation(userId, least)
        case UnableToTrackVideo(actualId, attemptedId) =>
          VideoNotCorrespond(actualId, attemptedId)
      } pipeTo sender

    case UserAction(u, _, _) => sender ! UserNotExist(u)
  }

  private def leastVideo() = {
    val i = leastVideoIndex
    leastVideoIndex = Random.nextInt(10)
    videos(i)
  }

  private def nextUserId() = {
    val id = leastUserId
    leastUserId += 1
    id
  }
}

class UserVideoRepo extends UserVideoActor {
  def trackerProps(userId: Long, videoId: Long): Props = {
    UserActivityTracker.props(userId, videoId)
  }
}

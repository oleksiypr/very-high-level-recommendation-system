package op.assessment.xt

import akka.actor.{Actor, Props}
import akka.event.Logging
import op.assessment.xt.UserActivityTracker.{Track, Tracked, UnableToTrackVideo}

object UserActivityTracker {

  def props(userId: Long, videoId: Long): Props = {
    Props(new UserActivityTracker(userId, videoId))
  }

  final case class Track(videoId: Long, leastVideoId: Long, action: Int)

  sealed trait TrackResult
  final case class Tracked(userId: Long, videoId: Long) extends TrackResult
  final case class UnableToTrackVideo(actualId: Long, attemptedId: Long) extends TrackResult
}

class UserActivityTracker private(
    userId: Long,
    private[this] var videoId: Long
  ) extends Actor {

  val log = Logging(context.system, this)

  val receive: Receive = {
    case Track(v, _, _) if v != videoId =>
      sender ! UnableToTrackVideo(videoId, v)

    case Track(v, leastVideoId, action) =>
      videoId = leastVideoId
      log.info(
        "UserId {} videoId {} action {} performed",
        userId, v, action
      )
      sender ! Tracked(userId, v)
  }
}

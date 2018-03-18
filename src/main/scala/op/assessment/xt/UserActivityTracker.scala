package op.assessment.xt

import akka.actor.{Actor, Props}

object UserActivityTracker {

  def props(userId: Long, videoId: Long): Props = {
    Props(new UserActivityTracker(userId, videoId))
  }

  final case class Track(videoId: Long, lastVideoId: Long, action: Int)

  sealed trait TrackResult
  final case class Tracked(userId: Long, videoId: Long) extends TrackResult
  final case class UnableToTrackVideo(actualId: Long, attemptedId: Long) extends TrackResult
}

class UserActivityTracker private(userId: Long, videoId: Long) extends Actor {
  override def receive: Receive = ???
}

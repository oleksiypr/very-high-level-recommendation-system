package op.assessment.xt

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import op.assessment.xt.UserActivityTracker.{Track, Tracked, UnableToTrackVideo}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class UserActivityTrackerSpec(_system: ActorSystem) extends TestKit(_system)
  with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("UserActivityTrackerSpec"))

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "UserActivityTracker" should {
    "track user videos" in {
      val userId = 1L
      val videoId = 0L
      val tracker = system.actorOf(UserActivityTracker.props(userId, videoId))

      val leastVideoId_1 = 1L
      val someUnknownVideoId = videoId + 100

      tracker ! Track(someUnknownVideoId, leastVideoId_1, action = 2)
      expectMsg(UnableToTrackVideo(videoId, someUnknownVideoId))

      tracker ! Track(videoId, leastVideoId_1, action = 3)
      expectMsg(Tracked(userId, videoId))

      val leastVideoId_2 = 2L
      tracker ! Track(leastVideoId_1, leastVideoId_2, action = 3)
      expectMsg(Tracked(userId, leastVideoId_1))
    }
  }
}

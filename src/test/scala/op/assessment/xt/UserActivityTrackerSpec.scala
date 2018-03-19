package op.assessment.xt

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class UserActivityTrackerSpec(_system: ActorSystem) extends TestKit(_system)
  with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("UserActivityTrackerSpec"))

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "UserActivityTracker" should {
    "track user videos" in {
      val userId = 9797345L
      val videoId = 6454556l
      val tracker = system.actorOf(UserActivityTracker.props(userId, videoId))


    }
  }
}

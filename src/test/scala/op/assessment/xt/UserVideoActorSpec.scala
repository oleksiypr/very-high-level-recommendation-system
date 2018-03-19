package op.assessment.xt

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActor, TestKit, TestProbe}
import op.assessment.xt.UserActivityTracker.{Track, Tracked, UnableToTrackVideo}
import op.assessment.xt.UserVideoActor._
import op.assessment.xt.ApiRoutes.User
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

object UserVideoActorSpec {

  class FakeTracker private[UserVideoActorSpec](probe: ActorRef) extends Actor {
    val receive: Receive = {
      case msg => probe.forward(msg)
    }
  }

  class TestUserVideoActor private[UserVideoActorSpec](probe: ActorRef)
    extends UserVideoActor {

    def trackerProps(
        userId: Long, videoId: Long
      ): Props = Props {
      new FakeTracker(probe)
    }
  }

  def props(probe: ActorRef): Props = Props {
    new TestUserVideoActor(probe)
  }
}

class UserVideoActorSpec(_system: ActorSystem) extends TestKit(_system)
  with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

  import UserVideoActorSpec._

  def this() = this(ActorSystem("UserVideoActorSpec"))

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "UserVideoActor" should {
    "handle registrations and actions" in {
      val probe = TestProbe()
      val userVideoRepo = system.actorOf(props(probe.ref))

      userVideoRepo !  UserAction(userId = 1L, videoId = 2L, actionId = 3)
      expectMsg(UserNotExist(1))

      userVideoRepo ! RegisterUser(User(
        userName = "David",
        email = "david@gmail.com",
        age = 25,
        gender = 1
      ))
      val (userId, videoId) = expectMsgPF() {
        case UserRecommendation(u, v) => (u, v)
      }

      val videoToExpect = videoId
      val someDifference = 5
      val unexpectedVideo = videoId + someDifference
      probe.setAutoPilot(
        (sender: ActorRef, msg: Any) => {
          msg match {
            case Track(`videoToExpect`, _, _) =>
              sender ! Tracked(userId, videoId)
            case Track(_, _, _) =>
              sender ! UnableToTrackVideo(unexpectedVideo, videoToExpect)
          }
          TestActor.KeepRunning
        }
      )

      userVideoRepo ! UserAction(userId, unexpectedVideo, actionId = 1)
      expectMsg(VideoNotCorrespond(unexpectedVideo, videoToExpect))

      userVideoRepo ! UserAction(userId, videoToExpect, actionId = 1)
      expectMsgPF() {
        case UserRecommendation(_, _) => succeed
        case other => fail(s"Unexpected message: $other")
      }
    }
  }

  "UserVideoActor" should {
    "handle multiple users" in {
      val probe = TestProbe()
      val userVideoRepo = system.actorOf(props(probe.ref))

      userVideoRepo ! RegisterUser(User(
        userName = "Joe",
        email = "joe@gmail.com",
        age = 25,
        gender = 1
      ))
      val (joeId, joeVideoId) = expectMsgPF() {
        case UserRecommendation(u, v) => (u, v)
      }

      userVideoRepo ! RegisterUser(User(
        userName = "Alice",
        email = "joe@gmail.com",
        age = 25,
        gender = 2
      ))

      val (aliceId, aliceVideoId) = expectMsgPF() {
        case UserRecommendation(u, v) => (u, v)
      }

      joeId shouldNot equal(aliceId)

      probe.setAutoPilot(
        (sender: ActorRef, msg: Any) => {
          msg match {
            case Track(`joeVideoId`, _, _) =>
              sender ! Tracked(joeId, joeVideoId)
            case Track(`aliceVideoId`, _, _) =>
              sender ! Tracked(aliceId, aliceVideoId)
          }
          TestActor.KeepRunning
        }
      )

      userVideoRepo ! UserAction(joeId, joeVideoId, actionId = 2)
      expectMsgPF() {
        case UserRecommendation(_, _) => succeed
        case other => fail(s"Unexpected message: $other")
      }

      userVideoRepo ! UserAction(aliceId, aliceVideoId, actionId = 3)
      expectMsgPF() {
        case UserRecommendation(_, _) => succeed
        case other => fail(s"Unexpected message: $other")
      }

      val someUnknownUserId = 2 * (joeId + aliceId)
      userVideoRepo ! UserAction(someUnknownUserId, aliceId, actionId = 3)
      expectMsg(UserNotExist(someUnknownUserId))
    }
  }
}

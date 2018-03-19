package op.assessment.xt

import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{MessageEntity, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.{TestActor, TestProbe}
import op.assessment.xt.UserVideoActor._
import op.assessment.xt.ApiRoutes.{Errors, Register, User}
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.concurrent.ScalaFutures

class ApiRoutesSpec extends WordSpec
  with Matchers with ScalaFutures with ScalatestRouteTest
  with ApiRoutes {

  val probe = TestProbe()
  val useVideoRepo: ActorRef = probe.ref

  probe.setAutoPilot(
    (sender: ActorRef, msg: Any) => {
      msg match {
        case RegisterUser(_) =>
          sender ! Register(userId = 9797345L, videoId = 4324556L)
        case UserAction(9797345L, 4324556L, _) =>
          sender ! UserRecommendation(userId = 9797345L, videoId = 6454556L)
        case UserAction(-1L, 4324556L, _) =>
          sender ! UserNotExist(userId = -1L)
        case UserAction(9797345L, -1L, _) =>
          sender ! VideoNotCorrespond(videoId = -1L, lastVideoId = 6454556L)
      }
      TestActor.KeepRunning
    }
  )

  "UserVideoRoutes POST: /register" should {
    "return 200 Ok" in {
      val  request = Post(
          "/register"
        ).withEntity(
          Marshal(User(
            name = "David",
            email = "david@gmail.com",
            age = 28,
            gender = 1
          )).to[MessageEntity].futureValue
        )

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)
        entityAs[Register] should ===(
          Register(userId = 9797345L, videoId = 4324556L)
        )
      }
    }
    "return 400: email is not valid " in {
      val  request = Post(
        "/register"
      ).withEntity(
        Marshal(User(
          name = "David",
          email = "david-gmail.com",
          age = 28,
          gender = 1
        )).to[MessageEntity].futureValue
      )

      request ~> routes ~> check {
        status should ===(StatusCodes.BadRequest)
        entityAs[Errors] should ===(
          Errors(List("email is not valid"))
        )
      }
    }
    "return 400: age is not valid " in {
      val  request = Post(
        "/register"
      ).withEntity(
        Marshal(User(
          name = "David",
          email = "david@gmail.com",
          age = 127,
          gender = 1
        )).to[MessageEntity].futureValue
      )

      request ~> routes ~> check {
        status should ===(StatusCodes.BadRequest)
        entityAs[Errors] should ===(
          Errors(List("age is not valid"))
        )
      }
    }
    "return 400: email, gender and age are not valid " in {
      val  request = Post(
        "/register"
      ).withEntity(
        Marshal(User(
          name = "David",
          email = "david-gmail.com",
          age = 127,
          gender = 3
        )).to[MessageEntity].futureValue
      )

      request ~> routes ~> check {
        status should ===(StatusCodes.BadRequest)
        entityAs[Errors] should ===(
          Errors(List(
            "email is not valid",
            "age is not valid",
            "gender is not valid"))
        )
      }
    }
  }

  "UserVideoRoutes POST: /action" should {
    "return 200 Ok" in {
      val  request = Post(
        "/action"
      ).withEntity(
        Marshal(UserAction(
          userId = 9797345L,
          videoId = 4324556L,
          action = 3
        )).to[MessageEntity].futureValue
      )

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)
        entityAs[Register] should ===(
          Register(userId = 9797345L, videoId =6454556L)
        )
      }
    }
    "return 400: userId does not exist" in {
      val  request = Post(
        "/action"
      ).withEntity(
        Marshal(UserAction(
          userId = -1L,
          videoId = 4324556L,
          action = 3
        )).to[MessageEntity].futureValue
      )

      request ~> routes ~> check {
        status should ===(StatusCodes.BadRequest)
        entityAs[Errors] should ===(
          Errors(List("userId -1 not exist"))
        )
      }
    }
    "return 400: video does not correspond to last given" in {
      val  request = Post(
        "/action"
      ).withEntity(
        Marshal(UserAction(
          userId = 9797345L,
          videoId = -1L,
          action = 3
        )).to[MessageEntity].futureValue
      )

      request ~> routes ~> check {
        status should ===(StatusCodes.BadRequest)
        entityAs[Errors] should ===(
          Errors(List("video does not correspond to last given"))
        )
      }
    }
  }
}

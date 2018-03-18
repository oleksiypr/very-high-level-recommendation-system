package op.assessment.xt

import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{MessageEntity, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.{TestActor, TestProbe}
import op.assessment.xt.UseVideoRepo.RegisterUser
import op.assessment.xt.UserVideoRoutes.{Errors, Register, User}
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.concurrent.ScalaFutures

class UserVideoRoutesSpec extends WordSpec
  with Matchers with ScalaFutures with ScalatestRouteTest
  with UserVideoRoutes {

  val probe = TestProbe()
  val useVideoRepo: ActorRef = probe.ref

  probe.setAutoPilot(
    (sender: ActorRef, msg: Any) => {
      msg match {
        case RegisterUser(_) =>
          sender ! Register(userId = 9797345L, videoId = 4324556L)
      }
      TestActor.KeepRunning
    }
  )

  "UserVideoRoutes POST /register" should {
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
}

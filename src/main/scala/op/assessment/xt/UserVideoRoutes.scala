package op.assessment.xt

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import cats.data.Validated._
import cats.data._
import cats.implicits._
import op.assessment.sn.JsonSupport
import op.assessment.xt.UseVideoRepo._
import op.assessment.xt.UserVideoRoutes._
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object UserVideoRoutes {

  final case class User(
      name: String,
      email: String,
      age: Int,
      gender: Int
    )

  final case class Register(userId: Long, videoId: Long)

  final case class Errors(errs: List[String])

  sealed abstract class UserValidation(val message: String)
  case object EmailNotValid extends UserValidation("email is not valid")
  case object AgeNotValid extends UserValidation("age is not valid")
  case object GenderNotValid extends UserValidation("gender is not valid")

  object UserValidation {

    type ValidationResult[A] = ValidatedNel[UserValidation, A]

    def validate(user: User): ValidationResult[User] = (
        validateName(user.name),
        validateEmail(user.email),
        valiadteAge(user.age),
        validateGender(user.gender)
      ).mapN(User)

    private def validateName(
        name: String): ValidationResult[String] = name.validNel

    private def validateEmail(
        email: String
      ): ValidationResult[String] = {
      if ("""(\w+)@([\w\.]+)""".r.unapplySeq(email).isDefined) email.validNel
      else EmailNotValid.invalidNel
    }

    private def valiadteAge(age: Int): ValidationResult[Int] = {
      if (age >= 5 && age <= 120) age.validNel
      else AgeNotValid.invalidNel
    }

    private def validateGender(gender: Int): ValidationResult[Int] = {
      if (gender == 1 || gender == 2) gender.validNel
      else GenderNotValid.invalidNel
    }
  }
}

trait UserVideoRoutes extends JsonSupport {

  val useVideoRepo: ActorRef

  implicit val timeout: Timeout = 5.seconds

  lazy val routes: Route = path("register") {
    post {
      entity(as[User]) { u =>
        UserValidation.validate(u) match {
          case Valid(user) =>
            onComplete((useVideoRepo ? RegisterUser(user)).mapTo[Register]) {
              case Success(res) => complete((StatusCodes.OK, res))
              case Failure(err) => complete((
                StatusCodes.InternalServerError,
                Errors(List(err.getMessage))
              ))
            }
          case Invalid(errors) =>
            complete((
              StatusCodes.BadRequest,
              Errors(errors.toList.map(_.message))))
        }
      }
    }
  } ~ path("action") {
    post {
      entity(as[UserAction]) { ua =>
        onComplete((useVideoRepo ? ua).mapTo[ActionResult]) {
          case Success(res) => res match {
            case UserRecommendation(u, v) =>
              complete((StatusCodes.OK, Register(u, v)))
            case UserNotExist(u) => complete((
                StatusCodes.BadRequest,
                Errors(List(s"userId $u not exist"))
              ))
            case VideoNotCorrespond(_, _) =>
              complete((
                StatusCodes.BadRequest,
                Errors(List(s"video does not correspond to last given"))
              ))
          }
       case Failure(err) => complete((
            StatusCodes.InternalServerError,
            Errors(List(err.getMessage))
          ))
        }
      }
    }
  }
}

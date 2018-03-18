package op.assessment.xt

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import op.assessment.sn.JsonSupport
import op.assessment.xt.UserVideoRoutes.{Errors, Register, User, UserValidation}
import cats.data._
import cats.data.Validated._
import cats.implicits._
import op.assessment.xt.UserVideoRoutes.UserValidation.ValidationResult

object UserVideoRoutes {

  final case class User(
      name: String,
      email: String,
      age: Int,
      gender: Int
    )

  final case class Register(userId: Long, videoId: Long)

  final case class UserAction(
      userId: Long,
      videoId: Long,
      action: Int
    )

  final case class Errors(errs: List[String])

  sealed abstract class UserValidation(val message: String)
  case object EmailNotValid extends UserValidation("email is not valid")
  case object AgeNotValid extends UserValidation("age is not valid")
  case object GenderNotValid extends UserValidation("gender is not valid")

  object UserValidation {

    type ValidationResult[A] = ValidatedNel[UserValidation, A]

    def validate(user: User): ValidationResult[User] = (
        validatenName(user.name),
        validateEmail(user.email),
        valiadteAge(user.age),
        validateGender(user.gender)
      ).mapN(User)

    private def validatenName(name: String
      ): ValidationResult[String] = name.validNel

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

  lazy val routes: Route = path("register") {
    post {
      entity(as[User]) { u =>
        val validated: ValidationResult[User] = UserValidation.validate(u)
        validated match {
          case Valid(user) =>
            complete((
              StatusCodes.OK,
              Register(userId = 9797345L, videoId = 4324556L)))
          case Invalid(errors) =>
            complete((
              StatusCodes.BadRequest,
              Errors(errors.toList.map(_.message))))
        }
      }
    }
  }
}

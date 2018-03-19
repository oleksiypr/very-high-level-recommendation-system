package op.assessment.xt

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import op.assessment.xt.ApiRoutes.{Errors, Register, User}
import op.assessment.xt.UserVideoActor.UserAction
import spray.json.DefaultJsonProtocol

trait JsonSupport extends SprayJsonSupport {

  import DefaultJsonProtocol._

  implicit val userFormat = jsonFormat4(User)
  implicit val registerFormat = jsonFormat2(Register)
  implicit val actionFormat = jsonFormat3(UserAction)
  implicit val errorsFormat = jsonFormat1(Errors)
}


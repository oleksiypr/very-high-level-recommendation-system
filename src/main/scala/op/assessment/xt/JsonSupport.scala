package op.assessment.xt

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import op.assessment.sn.BidRoutes.{Ammount, Bids, Fail}
import op.assessment.sn.BidsRepository.Bid
import op.assessment.xt.ApiRoutes.{Errors, Register, User}
import op.assessment.xt.UserVideoActor.UserAction
import spray.json.DefaultJsonProtocol

trait JsonSupport extends SprayJsonSupport {

  import DefaultJsonProtocol._

  implicit val userFormat = jsonFormat4(User)
  implicit val registerFormat = jsonFormat2(Register)
  implicit val actionFormat = jsonFormat3(UserAction)

  implicit val errorsFormat = jsonFormat1(Errors)

  implicit val ammountFormat = jsonFormat1(Ammount)
  implicit val failFormat = jsonFormat1(Fail)
  implicit val bidFormat = jsonFormat3(Bid)
  implicit val bidsFormat = jsonFormat1(Bids)
}


package op.assessment.xt

import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.concurrent.ScalaFutures

class UserVideoRoutesSpec extends WordSpec
  with Matchers with ScalaFutures with ScalatestRouteTest
  with UserVideoRoutes {

  "UserVideoRoutes POST /register" should {
    "retirn 200 Ok" in {
      ???
    }
    "return "
  }
}

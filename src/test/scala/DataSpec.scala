import scala.util.{Success, Random}

import org.scalatest._

class DataSpec extends WordSpec with Matchers {
  "The Data" should {
    "retrieve content previously written to the cache" in {
      val content = "asdf"
      val url = Random.nextString(10)
      Data.cache.write(url, content)

      Data.fetch(url, x => Success(x)) should be(content)
    }
  }
}

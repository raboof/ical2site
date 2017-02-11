import scala.util.Success

import org.scalatest._

class CacheSpec extends WordSpec with Matchers {
  "The Cache" should {
    "Retrieve content previously written to the cache" in {
      val cache = new Cache()
      val content = "asdf"
      val url = "url.invalid"
      cache.write(url, content)

      cache.fetch(url) should be(Success(content))
    }
  }
}

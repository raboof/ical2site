import scala.util.{Random, Success}

import org.scalatest._

class CacheSpec extends WordSpec with Matchers {
  "The Cache" should {
    "retrieve content previously written to the cache" in {
      val cache = new Cache(_ => Success("generated"))
      val content = "asdf"
      val url = Random.nextString(10)
      cache.write(url, content)

      cache.fetch(url) should be(Success(content))
    }

    "retrieve content not previously written to the cache through the generator" in {
      val content = "asdf"
      var n = 0
      val cache = new Cache(_ => {
        n = n + 1
        Success(content + n)
      })
      val url = Random.nextString(10)

      cache.fetch(url) should be(Success(content + 1))
      // Value should be cached
      cache.fetch(url) should be(Success(content + 1))
    }
  }
}

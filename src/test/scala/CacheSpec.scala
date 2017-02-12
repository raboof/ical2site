import java.time.{ZonedDateTime}

import scala.util.{Failure, Random, Success, Try}

import org.scalatest._

class CacheSpec extends WordSpec with Matchers {
  "The Cache" should {
    "retrieve content previously written to the cache" in {
      val cache = new Cache(_ => Success("generated"))
      val content = "asdf"
      val url = Random.nextString(10)
      cache.write(url, content)

      cache.fetch(url, x => Success(x)) should be(Success(content))
    }

    "retrieve content not previously written to the cache through the generator" in {
      val content = "asdf"
      var n = 0
      val cache = new Cache(_ => {
        n = n + 1
        Success(content + n)
      })
      val url = Random.nextString(10)

      cache.fetch(url, x => Success(x)) should be(Success(content + 1))
      // Value should be cached:
      cache.fetch(url, x => Success(x)) should be(Success(content + 1))
    }

    "don't store content in the cache when it fails to marshal" in {
      var first = true
      val cache = new Cache(generator = _ => {
        if (first) {
          first = false
          Success("first")
        } else Success("notfirst")
      })

      val marshaller: String => Try[String] = {
        case "first" => Failure(new IllegalStateException("First try fails"))
        case "notfirst" => Success("Second try succeeds")
      }

      val url = Random.nextString(10)

      cache.fetch(url, marshaller) should be(Success("Second try succeeds"))
    }

    "retrieve content for which the cache is expired through the generator" in {
      val cache = new Cache(_ => Success("newlygenerated"))
      val url = Random.nextString(10)

      cache.write(url, "old", Some(ZonedDateTime.now().minusDays(3)))

      cache.fetch(url, x => Success(x)) should be(Success("newlygenerated"))
    }

  }
}

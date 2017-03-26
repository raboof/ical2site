import scala.util._

import com.typesafe.scalalogging.Logger

object Data {
  val cache = new Cache(fetch)
  val log = Logger("Data")

  def fetch[T](url: String, marshaller: String => Try[T]): Option[T] = {
    def decoratedMarshaller(content: String): Try[T] = {
      marshaller(content).recoverWith {
        case t => Failure(new IllegalStateException(s"Failed to marshall: ${t.getMessage}. Full text:\n$content", t))
      }
    }

    cache.fetch(url, decoratedMarshaller) match {
      case Success(value) => Some(value)
      case Failure(cause) => {
        log.warn(s"Failed to fetch '$url'", cause)
        None
      }
    }
  }

  private def fetch(url: String): Try[String] = Try {
    scala.io.Source.fromURL(resolvePlaceholders(url)).mkString
  }

  private def resolvePlaceholders(url: String): String = {
    if (url.contains("FB_TOKEN")) url.replaceAll("FB_TOKEN", java.net.URLEncoder.encode(Facebook.token, "UTF-8"))
    else url
  }
}

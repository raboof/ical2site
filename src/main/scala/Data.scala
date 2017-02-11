import scala.util._

object Data {
  val cache = new Cache(fetch)

  def fetch[T](url: String, marshaller: String => Try[T]): T = {
    def decoratedMarshaller(content: Try[String]): Try[T] = content match {
      case Success(txt) =>      marshaller(txt).recoverWith {
        case t => Failure(new IllegalStateException(s"Failed to marshall: ${t.getMessage}. Full text:\n$txt", t))
      }
      case Failure(f) => Failure(f)
    }

    decoratedMarshaller(cache.fetch(url).recoverWith { case _ => fetch(url) }).recoverWith {
      case _ => decoratedMarshaller(fetch(url))
    }.recoverWith {
      case _ => decoratedMarshaller(fetch(url))
    }.recoverWith { case t => Failure(new IllegalStateException(s"Failed to marshall $url: ${t.getMessage}}", t)) }.get
  }

  private def fetch(url: String): Try[String] = Try {
    val content = scala.io.Source.fromURL(resolvePlaceholders(url)).mkString
    cache.write(url, content)
    content
  }

  private def resolvePlaceholders(url: String): String = {
    if (url.contains("FB_TOKEN")) url.replaceAll("FB_TOKEN", java.net.URLEncoder.encode(Facebook.token, "UTF-8"))
    else url
  }
}

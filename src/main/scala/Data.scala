import java.nio.file.{Files, Paths}

import scala.util._

object Data {
  object Cache {
    val cacheDir = Paths.get("cache")
    Files.createDirectories(cacheDir)

    def fetch(url: String): Option[String] = {
      val cp = cachePath(url);
      if (Files.exists(cp)) Some(new String(Files.readAllBytes(cp), "UTF-8"))
      else None
    }

    def write(url: String, file: String) =
      Files.write(cachePath(url), file.getBytes)

    private def cachePath(url: String) = Paths.get("cache/" + url.replaceAll("/", "_"))
  }

  def fetch[T](url: String, marshaller: String => Try[T]): T = {
    marshaller(Cache.fetch(url).getOrElse(fetch(url)))
      .recoverWith { case _ => {
          val txt = fetch(url)
          marshaller(txt).recoverWith{ case t => Failure(new IllegalStateException(
           s"Failed to marshall $url: ${t.getMessage}. Full text:\n$txt", t)) }
        }
      }.get
  }

  private def fetch(url: String): String = {
    val content = scala.io.Source.fromURL(resolvePlaceholders(url)).mkString
    Cache.write(url, content)
    content
  }

  private def resolvePlaceholders(url: String): String = {
    if (url.contains("FB_TOKEN")) url.replaceAll("FB_TOKEN", java.net.URLEncoder.encode(Facebook.token, "UTF-8"))
    else url
  }
}

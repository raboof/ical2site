import java.nio.file.{Files, Paths}

object Data {
  def fetch(url: String): String = {
    val cacheDir = Paths.get("cache")
    Files.createDirectories(cacheDir)

    val cachePath = Paths.get("cache/" + url.replaceAll("/", "_"))
    if (Files.exists(cachePath)) {
      new String(Files.readAllBytes(cachePath), "UTF-8")
    } else {
      val content = scala.io.Source.fromURL(resolvePlaceholders(url)).mkString
      Files.write(cachePath, content.getBytes)
      content
    }
  }

  private def resolvePlaceholders(url: String): String = {
    if (url.contains("FB_TOKEN")) url.replaceAll("FB_TOKEN", java.net.URLEncoder.encode(Facebook.token, "UTF-8"))
    else url
  }
}

import java.nio.file.{Files, Paths}

import scala.util.{Try, Success, Failure}

class Cache(generator: String => Try[String]) {
  val cacheDir = Paths.get("cache")
  Files.createDirectories(cacheDir)

  def fetch(key: String): Try[String] = {
    val cp = cachePath(key);
    if (Files.exists(cp)) Success(new String(Files.readAllBytes(cp), "UTF-8"))
    else {
      val generated = generator(key)
      generated.foreach(write(key, _))
      generated
    }
  }

  def write(key: String, file: String) =
    Files.write(cachePath(key), file.getBytes)

  private def cachePath(key: String) = Paths.get("cache/" + key.replaceAll("/", "_"))
}

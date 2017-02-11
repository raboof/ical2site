import java.nio.file.{Files, Paths}

import scala.util.{Try, Success, Failure}

class Cache(generator: String => Try[String]) {
  val cacheDir = Paths.get("cache")
  Files.createDirectories(cacheDir)

  /**
   * Try to fetch the key from the cache, use the generator if necessary, but only consider the result
   * valid when it can be successfully marshalled
   */
  def fetch[T](key: String, marshaller: String => Try[T]): Try[T] = {
    fetch(key)
      .flatMap(marshallAndWrite(key, _, marshaller))
      .recoverWith {
        // Retry
        case _ => generator(key).flatMap(marshallAndWrite(key, _, marshaller))
      }
  }

  private def marshallAndWrite[T](key: String, value: String, marshaller: String => Try[T]): Try[T] = {
    val marshalled = marshaller(value)
    marshalled.foreach(_ => write(key, value))
    marshalled
  }

  private def fetch(key: String): Try[String] = {
    val cp = cachePath(key);
    if (Files.exists(cp)) Success(new String(Files.readAllBytes(cp), "UTF-8"))
    else generator(key)
  }

  def write(key: String, file: String) =
    Files.write(cachePath(key), file.getBytes)

  private def cachePath(key: String) = Paths.get("cache/" + key.replaceAll("/", "_"))
}

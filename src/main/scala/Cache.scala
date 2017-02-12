import java.nio.file.{Files, Paths}
import java.nio.file.attribute.FileTime
import java.time.ZonedDateTime

import scala.concurrent.duration._
import scala.util.{Try, Success, Failure}

import com.typesafe.scalalogging.Logger

class Cache(generator: String => Try[String], expiration: Duration = 1 hour) {
  val log = Logger(classOf[Cache])

  val cacheDir = Paths.get("cache")
  Files.createDirectories(cacheDir)

  /**
   * Try to fetch the key from the cache, use the generator if necessary, but only consider the result
   * valid when it can be successfully marshalled
   */
  def fetch[T](key: String, marshaller: String => Try[T]): Try[T] = {
    fetch(key) match {
      case Success((value, time)) if expired(time) =>
        generateAndMarshall(key, marshaller)
          .recoverWith { case _ => generateAndMarshall(key, marshaller) }
          .recoverWith { case t => {
              log.warn(s"Failed to process '$key', falling back to cached value", t)
              marshaller(value)
            }
          }
      case Success((value, time)) =>
        marshaller(value)
          .recoverWith { case _ => generateAndMarshall(key, marshaller) }
      case Failure(f) =>
        generateAndMarshall(key, marshaller)
          .recoverWith { case _ => generateAndMarshall(key, marshaller) }
    }
  }

  private def expired(time: FileTime) = System.currentTimeMillis > (time.toMillis + expiration.toMillis)

  private def generateAndMarshall[T](key: String, marshaller: String => Try[T]): Try[T] = {
    generator(key).flatMap(marshallAndWrite(key, _, marshaller))
  }

  private def marshallAndWrite[T](key: String, value: String, marshaller: String => Try[T]): Try[T] = {
    val marshalled = marshaller(value)
    marshalled.foreach(_ => write(key, value))
    marshalled
  }

  private def fetch(key: String): Try[(String, FileTime)] = {
    val cp = cachePath(key);
    if (Files.exists(cp)) Success((new String(Files.readAllBytes(cp), "UTF-8"), Files.getAttribute(cp, "lastModifiedTime").asInstanceOf[FileTime]))
    else Failure(new IllegalStateException(s"Key $key not found in cache"))
  }

  def write(key: String, file: String, modified: Option[ZonedDateTime] = None): Unit = {
    val path = cachePath(key)
    Files.write(path, file.getBytes)
    modified.foreach(date => {
      Files.setAttribute(path, "lastModifiedTime", FileTime.fromMillis(date.toInstant().toEpochMilli))
    })
  }

  private def cachePath(key: String) = Paths.get("cache/" + key.replaceAll("/", "_"))
}

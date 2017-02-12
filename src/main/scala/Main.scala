import java.nio.file.{Files, Paths, StandardCopyOption}
import java.time._

import scala.util._
import scala.collection.GenSeq

import biweekly.{Biweekly, ICalendar}
import collection.JavaConverters._

import spray.json._
import DefaultJsonProtocol._

object Main extends App {
  implicit val sourceFormat = jsonFormat4(Source)
  implicit val configFormat = jsonFormat4(Config)

  val config =
    new String(Files.readAllBytes(Paths.get("resources", "deventer.live.json")), "UTF-8").parseJson.convertTo[Config]

  def getDate(date: java.util.Date): LocalDate = {
    ZonedDateTime.ofInstant(date.toInstant, ZoneId.systemDefault()).toLocalDate;
  }

  val marshaller: String => Try[ICalendar] = txt => {
    val parsed = Biweekly.parse(txt)
    val warnings = new java.util.ArrayList[java.util.List[String]]()
    parsed.warnings(warnings)
    Option(parsed.first())
      .map(Success(_))
      .getOrElse(Failure(new IllegalStateException(s"Could not parse calendar: $warnings")))
  }

  val events: GenSeq[Event] = for {
    source <- config.sources.par
    cal <- Data.fetch(source.icalUrl, marshaller).toList
    event <- cal.getEvents.asScala.map(event =>
        Event(getDate(event.getDateStart.getValue),
              source,
              Option(event.getDescription).map(_.getValue).getOrElse(""),
              Option(event.getUrl).map(_.getValue),
              event)).toList
  } yield event

  val outputDir = Paths.get("target/site")
  Files.createDirectories(outputDir)
  Seq("style.css", ".htaccess", "favicon.ico", "ogimage_square.png").foreach(file =>
    Files.copy(Paths.get("resources", file), Paths.get(outputDir.toString, file), StandardCopyOption.REPLACE_EXISTING))

  Files.write(Paths.get(outputDir.toString, "index.html"),
              Html.list(config.mainTitle, config.subtitle, config.lang, events.toList).getBytes("UTF-8"))
  Files.write(Paths.get(outputDir.toString, "about.html"), Html.about(config).getBytes("UTF-8"))
}

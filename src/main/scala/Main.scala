import java.nio.file.{ Files, Paths, StandardCopyOption }
import java.time._

import scala.util._
import scala.collection.GenSeq

import biweekly.{ Biweekly, ICalendar }
import collection.JavaConverters._

import spray.json._
import DefaultJsonProtocol._

object Main extends App {
  implicit val sourceFormat = jsonFormat4(Source)
  implicit val configFormat = jsonFormat6(Config)

  implicit val iconFormat = jsonFormat(Icon, "src", "type", "sizes")
  implicit val webAppManifestFormat = jsonFormat6(WebAppManifest)

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

  def extractEvents(source: Source, events: Seq[biweekly.component.VEvent]): Seq[Event] = {
    val recurringSummaries = events.groupBy(_.getSummary).filter(_._2.size > 4).map(_._2.head.getSummary).toList
    events
      .filter(event => !recurringSummaries.contains(event.getSummary))
      .map(event =>
        Event(
          getDate(event.getDateStart.getValue),
          source,
          Option(event.getDescription).map(_.getValue).getOrElse(""),
          Option(event.getUrl).map(_.getValue).getOrElse(source.siteUrl),
          event
        )).toList
  }

  val events: GenSeq[Event] = for {
    source <- config.sources.par
    cal <- Data.fetch(source.icalUrl, marshaller).toList
    event <- extractEvents(source, cal.getEvents.asScala)
  } yield event

  val outputDir = Paths.get("target/site")
  Files.createDirectories(outputDir)
  Seq("style.css", ".htaccess", "favicon.ico", "favicon.png", "ogimage_square.png").foreach(file =>
    Files.copy(Paths.get("resources", file), Paths.get(outputDir.toString, file), StandardCopyOption.REPLACE_EXISTING))

  Seq(
    "index.html" -> Html.list(config.mainTitle, config.subtitle, config.lang, config.themeColor, events.toList),
    "about.html" -> Html.about(config),
    "manifest.json" -> WebAppManifest(
      config.mainTitle,
      config.mainTitle + " | " + config.subtitle,
      List(Icon("favicon.png", "image/png", "150x150")),
      config.backgroundColor,
      config.themeColor, start_url = "/"
    ).toJson.prettyPrint
  ).foreach {
      case (filename, content) =>
        Files.write(Paths.get(outputDir.toString, filename), content.getBytes("UTF-8"))
    }
}

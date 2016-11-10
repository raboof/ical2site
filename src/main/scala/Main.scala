import java.nio.file.{Files, Paths, StandardCopyOption}
import java.time._

import biweekly.Biweekly
import collection.JavaConverters._

import spray.json._
import DefaultJsonProtocol._

object Main extends App {
  implicit val sourceFormat = jsonFormat4(Source)
  implicit val configFormat = jsonFormat3(Config)

  val config = new String(Files.readAllBytes(Paths.get("resources", "deventer.live.json")), "UTF-8").parseJson.convertTo[Config]

  def getDate(date: java.util.Date): LocalDate = {
    ZonedDateTime.ofInstant(date.toInstant, ZoneId.systemDefault()).toLocalDate;
  }

  val events: Seq[Event] = config.sources.par
    .flatMap(source => {
      val txt = Data.fetch(source.icalUrl)
      val cal = Biweekly.parse(txt).first()
      cal.getEvents.asScala.map(
        event =>
          Event(getDate(event.getDateStart.getValue),
                source,
                Option(event.getDescription).map(_.getValue).getOrElse(""),
                Option(event.getUrl).map(_.getValue),
                event))
    })
    .toList

  val outputDir = Paths.get("target/site")
  Files.createDirectories(outputDir)
  Seq("style.css", ".htaccess", "favicon.ico", "ogimage_square.png").foreach(file =>
    Files.copy(Paths.get("resources", file), Paths.get(outputDir.toString, file), StandardCopyOption.REPLACE_EXISTING))

  Files.write(Paths.get(outputDir.toString, "index.html"), Html.list(config.mainTitle, config.subtitle, events).render.getBytes("UTF-8"))
  Files.write(Paths.get(outputDir.toString, "about.html"), Html.about(config).render.getBytes("UTF-8"))
}

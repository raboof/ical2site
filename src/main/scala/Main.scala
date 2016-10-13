import java.nio.file.{Files, Paths, StandardCopyOption}
import java.time._

import biweekly.Biweekly
import collection.JavaConverters._

object Main extends App {
  val sources = List(
    Source("BWH", "Burgerweeshuis", "http://burgerweeshuis.nl/agenda.ics"),
    Source("HK", "Havenkwartier", "http://www.havenkwartierdeventer.com/events.ics"),
    Source("ToJ", "Taste of Jazz", "https://www.tasteofjazz.nl/events.ics"),
    Source("BK", "Bouwkunde", "http://www.theaterbouwkunde.nl/programma/events.ics"),
    Source("VVV", "VVV", "https://bymo5ipzgg.execute-api.eu-west-1.amazonaws.com/prod/vvvdeventer"),
    Source("DS",
           "Deventer Schouwburg",
           "https://bymo5ipzgg.execute-api.eu-west-1.amazonaws.com/prod/deventerschouwburg2ical"),
    Source("HIP",
           "De Hip",
           "https://bymo5ipzgg.execute-api.eu-west-1.amazonaws.com/prod/facebook?page_id=DeHip&token=FB_TOKEN"),
    Source("DB",
           "Davo Bieren",
           "https://bymo5ipzgg.execute-api.eu-west-1.amazonaws.com/prod/facebook?page_id=davobieren&token=FB_TOKEN"),
    Source(
      "DH",
      "Bierencafe de Heks",
      "https://bymo5ipzgg.execute-api.eu-west-1.amazonaws.com/prod/facebook?page_id=bierencafedeheks&token=FB_TOKEN"),
    Source(
      "NB",
      "Muziekkoepel Nerging-Bogel",
      "http://www.muziekkoepelneringbogel.nl/index.php?option=com_jevents&task=icals.export&format=ical&catids=0&years=0&k=38f31bbc7bff3bce9137ac0e5a56adc2")
  )

  def getDate(date: java.util.Date): LocalDate = {
    ZonedDateTime.ofInstant(date.toInstant, ZoneId.systemDefault()).toLocalDate;
  }

  val events: Seq[Event] = sources.par
    .flatMap(source => {
      val txt = Data.fetch(source.url)
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
  Seq("style.css", ".htaccess").foreach(file =>
    Files.copy(Paths.get("resources", file), Paths.get(outputDir.toString, file), StandardCopyOption.REPLACE_EXISTING))

  Files.write(Paths.get(outputDir.toString, "index.html"), Html.list(events).render.getBytes("UTF-8"))
}

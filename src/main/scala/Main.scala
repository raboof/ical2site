import java.nio.file.{Files, Paths, StandardCopyOption}
import java.time._

import biweekly.Biweekly
import collection.JavaConverters._

object Main extends App {
  val sources = List(
    Source("BWH", "Burgerweeshuis", "http://burgerweeshuis.nl/agenda.ics", "http://burgerweeshuis.nl"),
    Source("HK",
           "Havenkwartier",
           "http://www.havenkwartierdeventer.com/events.ics",
           "http://www.havenkwartierdeventer.com"),
    Source("ToJ", "Taste of Jazz", "https://www.tasteofjazz.nl/events.ics", "https://www.tasteofjazz.nl"),
    Source("BK", "Bouwkunde", "http://www.theaterbouwkunde.nl/programma/events.ics", "http://www.theaterbouwkunde.nl"),
    Source("VVV",
           "VVV Deventer",
           "https://bymo5ipzgg.execute-api.eu-west-1.amazonaws.com/prod/vvvdeventer",
           "http://deventer.info"),
    Source("DS",
           "Deventer Schouwburg",
           "https://bymo5ipzgg.execute-api.eu-west-1.amazonaws.com/prod/deventerschouwburg2ical",
           "https://www.deventerschouwburg.nl"),
    Source("HIP",
           "De Hip",
           "https://bymo5ipzgg.execute-api.eu-west-1.amazonaws.com/prod/facebook?page_id=DeHip&token=FB_TOKEN",
           "http://dehip.nl"),
    Source("DB",
           "Davo Bieren",
           "https://bymo5ipzgg.execute-api.eu-west-1.amazonaws.com/prod/facebook?page_id=davobieren&token=FB_TOKEN",
           "http://www.davobieren.nl"),
    Source(
      "DH",
      "Bierencafe de Heks",
      "https://bymo5ipzgg.execute-api.eu-west-1.amazonaws.com/prod/facebook?page_id=bierencafedeheks&token=FB_TOKEN",
      "http://www.deheks.nl"),
    Source(
      "NB",
      "Muziekkoepel Nerging-Bogel",
      "http://www.muziekkoepelneringbogel.nl/index.php?option=com_jevents&task=icals.export&format=ical&catids=0&years=0&k=38f31bbc7bff3bce9137ac0e5a56adc2",
      "http://www.muziekkoepelneringbogel.nl"),
    Source(
      "GL",
      "Café het Glas in Lood",
      "https://bymo5ipzgg.execute-api.eu-west-1.amazonaws.com/prod/facebook?page_id=cafehetglasinlood&token=FB_TOKEN",
      "http://www.cafehetglasinlood.nl"),
    Source(
      "P",
      "Bier- en Danscafé Persee",
      "https://bymo5ipzgg.execute-api.eu-west-1.amazonaws.com/prod/facebook?page_id=DanscafePersee&token=FB_TOKEN",
      "http://www.persee.nl/"),
    Source(
      "V",
      "Vogeleiland",
      "https://bymo5ipzgg.execute-api.eu-west-1.amazonaws.com/prod/facebook?page_id=vogeleilanddeventer&token=FB_TOKEN",
      "http://vogeleilanddeventer.nl/")
  )

  def getDate(date: java.util.Date): LocalDate = {
    ZonedDateTime.ofInstant(date.toInstant, ZoneId.systemDefault()).toLocalDate;
  }

  val events: Seq[Event] = sources.par
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

  Files.write(Paths.get(outputDir.toString, "index.html"), Html.list(events).render.getBytes("UTF-8"))
  Files.write(Paths.get(outputDir.toString, "about.html"), Html.about(sources).render.getBytes("UTF-8"))
}

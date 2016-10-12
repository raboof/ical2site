import scala.io.Source
import java.nio.file.{Paths, Files}
import java.time._

import scalatags.Text.all._

import biweekly.Biweekly
import biweekly.component.VEvent
import biweekly.property.DateStart
import collection.JavaConverters._

object Main extends App {
  implicit val dateStartOrder = new Ordering[DateStart] {
    override def compare(x: DateStart, y: DateStart) = x.getValue.compareTo(y.getValue)
  }
  implicit val localDateOrder = new Ordering[LocalDate] {
    override def compare(x: LocalDate, y: LocalDate) = x.compareTo(y)
  }

  case class Source(tag: String, name: String, url: String)
  case class Event(
    startDate: LocalDate,
    source: Source,
    description: String,
    url: Option[String],
   data: VEvent)

  val sources = List(
    Source("BWH", "Burgerweeshuis", "http://burgerweeshuis.nl/agenda.ics"),
    Source("HK", "Havenkwartier", "http://www.havenkwartierdeventer.com/events.ics"),
    Source("ToJ", "Taste of Jazz", "https://www.tasteofjazz.nl/events.ics"),
    Source("BK", "Bouwkunde", "http://www.theaterbouwkunde.nl/programma/events.ics"),
    Source("VVV", "VVV", "https://bymo5ipzgg.execute-api.eu-west-1.amazonaws.com/prod/vvvdeventer"),
    Source("DS", "Deventer Schouwburg", "https://bymo5ipzgg.execute-api.eu-west-1.amazonaws.com/prod/deventerschouwburg2ical"),
    Source("HIP", "De Hip", "https://bymo5ipzgg.execute-api.eu-west-1.amazonaws.com/prod/facebook?page_id=DeHip&token=FB_TOKEN"),
    Source("DB", "Davo Bieren", "https://bymo5ipzgg.execute-api.eu-west-1.amazonaws.com/prod/facebook?page_id=davobieren&token=FB_TOKEN"),
    Source("DH", "Bierencafe de Heks", "https://bymo5ipzgg.execute-api.eu-west-1.amazonaws.com/prod/facebook?page_id=bierencafedeheks&token=FB_TOKEN"),
    Source(
      "NB",
      "Muziekkoepel Nerging-Bogel",
      "http://www.muziekkoepelneringbogel.nl/index.php?option=com_jevents&task=icals.export&format=ical&catids=0&years=0&k=38f31bbc7bff3bce9137ac0e5a56adc2")
  )

  def resolvePlaceholders(url: String): String = {
    if (url.contains("FB_TOKEN")) url.replaceAll("FB_TOKEN", java.net.URLEncoder.encode(Facebook.token, "UTF-8"))
    else url
  }

  def fetch(url: String): String = {
    val cacheDir = Paths.get("cache")
    if (!Files.exists(cacheDir))
      Files.createDirectory(Paths.get("cache"))

    val cachePath = Paths.get("cache/" + url.replaceAll("/", "_"))
    if (Files.exists(cachePath)) {
      new String(Files.readAllBytes(cachePath), "UTF-8")
    } else {
      val content = scala.io.Source.fromURL(resolvePlaceholders(url)).mkString
      Files.write(cachePath, content.getBytes)
      content
    }
  }

  def getDate(date: java.util.Date): LocalDate = {
    ZonedDateTime.ofInstant(date.toInstant, ZoneId.systemDefault()).toLocalDate;
  }

  def eventListHtml(events: Seq[Event]) = events
    .filter(!_.startDate.isBefore(LocalDate.now()))
    .groupBy(_.startDate)
    .toList
    .sortBy(_._1)
    .map {
      case (date, events) =>
        div(
          h2(s"$date"),
          events.groupBy(_.source).toList.map {
            case (source, events) =>
              div(
                h3(s"${source.name}:"),
                events.map(evt => {
                  val uid = evt.data.getUid.getValue
                  div(cls:="evt",style:="cursor:hand")(
                    label(`for`:=uid)(
                      div(cls:="evt-title")(s"${evt.data.getSummary.getValue}"),
                      input(cls:="togglebox",`type`:="checkbox",id:=uid),
                      div(cls:="evt-detail",id:=uid)(
                        s"${evt.description}",
                        evt.url.map(url => a(href:=url)("site"))
                      )
                    )
                  )
                }
                )
              )
          }
        )
    }

  def printList(events: Seq[Event]) = {
    Files.write(Paths.get("list.html"),
                html(
                    head(
                      link(href:="https://fonts.googleapis.com/css?family=Lobster%20Two|Raleway", rel:="stylesheet"),
                      link(href:="style.css", rel:="stylesheet")
                    ),
                    body(style:="font-family: 'Raleway', sans-serif")(
                      div(cls:="heading")(
                        div(cls:="title")(style:="font-family: 'Lobster Two', cursive; font-size: 62px")("Deventer.live"),
                        div(cls:="subtitle")("Concerten en andere activiteiten in Deventer")
                      ),
                      eventListHtml(events)
                    )
                )
                .render.getBytes("UTF-8"))
  }

  val events: Seq[Event] = sources.par
    .flatMap(source => {
      val txt = fetch(source.url)
      val cal = Biweekly.parse(txt).first()
      cal.getEvents.asScala.map(event =>
        Event(
          getDate(event.getDateStart.getValue),
           source,
           Option(event.getDescription).map(_.getValue).getOrElse(""),
           Option(event.getUrl).map(_.getValue),
           event))
    })
    .toList

  printList(events)
}

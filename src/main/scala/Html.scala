import java.time._
import java.time.temporal.ChronoUnit

import scalatags.Text.all._
import scalatags.Text.tags2.title
import scalatags.Text.TypedTag

object Html {
  def list(mainTitle: String, subtitle: String, language: String, themeColor: String, events: Seq[Event]) =
    page(mainTitle, subtitle, language, themeColor, div(eventListHtml(events)))

  def about(config: Config) = page(
    config.mainTitle,
    config.subtitle,
    config.lang,
    config.themeColor,
    div(style := "margin: 2em")(
      h2("Over deze site"),
      p(
        "In Deventer is veel te beleven - deze site brengt die informatie samen."
      ),
      h3("Waar komt de informatie op deze site vandaan?"),
      p(
        "De informatie op deze site wordt verzameld van een aantal bronnen:"
      ),
      ul(
        config.sources.map(source =>
          li(a(href := source.icalUrl)("[ical]"), " ", a(href := source.siteUrl)(source.name)))
      ),
      h3("Ook op deze site?"),
      p("Heb of weet je een website met activiteiten in Deventer die ook in dit overzicht thuis hoort?"),
      p(
        "Laten we die dan toevoegen! Maak daarvoor een ",
        a(href := "https://github.com/raboof/ical2site/issues")("issue aan op GitHub"),
        ". Heeft de site al een ICalendar-feed dan is het misschien eenvoudig, anders is er werk aan de winkel."
      ),
      h3("Ook zo'n site?"),
      p("Wil je voor jouw community ook een site zoals deze?"),
      p(
        "Ben je zelf handig met software, ga dan gerust je gang: alle software waar deze site op draait is ",
        a(href := "https://github.com/raboof/ical2site")("als gratis 'Open Source' software beschikbaar via GitHub"),
        "."
      ),
      p(
        "Heb je zelf deze kennis niet in huis, ",
        a(href := "http://arnout.engelen.eu")("neem dan gerust contact op"),
        ", wie weet kan ik helpen."
      ),
      h3("Verbeteringen?"),
      p(
        "Er valt nog genoeg te verbeteren. Idee\u00ebn kun je posten als ",
        a(href := "https://github.com/raboof/ical2site/issues")("issue aan op GitHub"),
        " - en hulp is altijd welkom!"
      ),
      p(style := "text-align: right")(a(href := "http://arnout.engelen.eu")("Arnout Engelen"))
    )
  )

  private def page(mainTitle: String, subtitle: String, language: String, themeColor: String, bodyContent: TypedTag[_]*) =
    "<!DOCTYPE html>" + html(lang := language)(
      head(
        title(mainTitle + " | " + subtitle),
        link(href := "/manifest.json", rel := "manifest"),
        link(href := "https://fonts.googleapis.com/css?family=Lobster%20Two%7CRaleway", rel := "stylesheet"),
        link(href := "style.css", rel := "stylesheet"),
        meta(name := "description", content := subtitle),
        meta(name := "viewport", content := "user-scalable=no"),
        meta(name := "theme-color", content := themeColor),
        meta(attr("property") := "og:url", content := "https://deventer.live"),
        meta(attr("property") := "og:title", content := mainTitle),
        meta(attr("property") := "og:description", content := subtitle),
        meta(attr("property") := "og:image", content := "https://deventer.live/ogimage_square.png"),
        meta(attr("property") := "og:image:width", content := "316"),
        meta(attr("property") := "og:image:height", content := "316"),
        meta(attr("property") := "og:image:type", content := "image/png")
      ),
      body(style := "font-family: 'Raleway', sans-serif")(
        div(cls := "about")(
          a(href := "about.html")("Over deze site")
        ),
        div(cls := "heading")(
          div(cls := "title")(style := "font-family: 'Lobster Two', cursive; font-size: 62px")(
            a(href := "index.html")(mainTitle)
          ),
          div(cls := "subtitle")(subtitle)
        ),
        bodyContent
      )
    )

  implicit val localDateOrder = new Ordering[LocalDate] {
    override def compare(x: LocalDate, y: LocalDate) = x.compareTo(y)
  }

  private def eventListHtml(events: Seq[Event]) =
    events.filter(!_.startDate.isBefore(LocalDate.now())).groupBy(_.startDate).toList.sortBy(_._1).map {
      case (date, events) =>
        div(id := anchor(date))(
          h2(cls := "day")(formatDate(date)),
          div(cls := "date")(subDate(date)),
          events.groupBy(_.source).toList.map {
            case (source, events) =>
              div(
                h3(s"${source.name}:"),
                events.map(evt => {
                  val uid = evt.data.getUid.getValue
                  div(cls := "evt", style := "cursor:hand")(
                    label(`for` := uid)(
                      span(cls := "evt-title")(s"${evt.data.getSummary.getValue}"),
                      input(cls := "togglebox", `type` := "checkbox", id := uid),
                      span(cls := "evt-detail")(
                        span(cls := "evt-description")(s"${evt.description}"),
                        evt.url.map(url => a(href := url)("site"))
                      )
                    )
                  )
                })
              )
          }
        )
    }

  private def anchor(date: LocalDate) = f"${date.getYear}${date.getMonth.ordinal + 1}%02d${date.getDayOfMonth}%02d"
  private def formatDate(date: LocalDate) = description(date)
  private def subDate(date: LocalDate) = s"${ofWeek(date)} $date"

  private def ofWeek(date: LocalDate) = date.getDayOfWeek match {
    case DayOfWeek.MONDAY => "maandag"
    case DayOfWeek.TUESDAY => "dinsdag"
    case DayOfWeek.WEDNESDAY => "woensdag"
    case DayOfWeek.THURSDAY => "donderdag"
    case DayOfWeek.FRIDAY => "vrijdag"
    case DayOfWeek.SATURDAY => "zaterdag"
    case DayOfWeek.SUNDAY => "zondag"
  }

  private def description(date: LocalDate) =
    Duration.between(LocalDate.now().atTime(0, 0), date.atTime(0, 0)).toDays match {
      case 0 => "vandaag"
      case 1 => "morgen"
      case n if n < 7 => s"over $n dagen"
      case n if n % 7 == 0 => s"over " + duration(n / 7, "week", "weken")
      case n => s"over " + duration(n / 7, "week", "weken") + " en " + duration(n % 7, "dag", "dagen")
    }

  private def duration(n: Long, singular: String, plural: String) = n + " " + (if (n == 1) singular else plural)
}

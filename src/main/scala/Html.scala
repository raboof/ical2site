import java.time._

import scalatags.Text.all._
import scalatags.Text.TypedTag

object Html {
  def list(events: Seq[Event]) = page(div(eventListHtml(events)))

  def about(sources: Seq[Source]) = page(
    div(style := "margin: 2em")(
      h2("Over deze site"),
      p(
        "In Deventer is veel te beleven - deze site brengt die informatie samen."
      ),
      h3("Waar komt de informatie op deze site vandaan?"),
      p(
        "De informatie op deze site wordt verzameld van een aantal bronnen:",
        ul(
          sources.map(source => li(a(href := source.icalUrl)("[ical]"), " ", a(href := source.siteUrl)(source.name)))
        )
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
      p("Heb je zelf deze kennis niet in huis, ",
        a(href := "http://arnout.engelen.eu")("neem dan gerust contact op"),
        ", wie weet kan ik helpen."),
      h3("Verbeteringen?"),
      p(
        "Er valt nog genoeg te verbeteren. Idee\u00ebn kun je posten als ",
        a(href := "https://github.com/raboof/ical2site/issues")("issue aan op GitHub"),
        " - en hulp is altijd welkom!"
      ),
      p(style := "text-align: right")(a(href := "http://arnout.engelen.eu")("Arnout Engelen"))
    )
  )

  private def page(content: TypedTag[_]*) =
    html(
      head(
        link(href := "https://fonts.googleapis.com/css?family=Lobster%20Two|Raleway", rel := "stylesheet"),
        link(href := "style.css", rel := "stylesheet")
      ),
      body(style := "font-family: 'Raleway', sans-serif")(
        div(cls := "about")(
          a(href := "about.html")("Over deze site")
        ),
        div(cls := "heading")(
          div(cls := "title")(style := "font-family: 'Lobster Two', cursive; font-size: 62px")(
            a(href := "index.html")("Deventer.live")
          ),
          div(cls := "subtitle")("Concerten en activiteiten in Deventer")
        ),
        content
      )
    )

  implicit val localDateOrder = new Ordering[LocalDate] {
    override def compare(x: LocalDate, y: LocalDate) = x.compareTo(y)
  }

  private def eventListHtml(events: Seq[Event]) =
    events.filter(!_.startDate.isBefore(LocalDate.now())).groupBy(_.startDate).toList.sortBy(_._1).map {
      case (date, events) =>
        div(
          h2(s"$date"),
          events.groupBy(_.source).toList.map {
            case (source, events) =>
              div(
                h3(s"${source.name}:"),
                events.map(evt => {
                  val uid = evt.data.getUid.getValue
                  div(cls := "evt", style := "cursor:hand")(
                    label(`for` := uid)(
                      div(cls := "evt-title")(s"${evt.data.getSummary.getValue}"),
                      input(cls := "togglebox", `type` := "checkbox", id := uid),
                      div(cls := "evt-detail", id := uid)(
                        p(s"${evt.description}"),
                        evt.url.map(url => a(href := url)("site"))
                      )
                    )
                  )
                })
              )
          }
        )
    }
}

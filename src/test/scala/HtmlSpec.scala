import java.io._
import java.time.LocalDate

import biweekly.component.VEvent

import org.scalatest._
import org.xml.sax.InputSource

import spray.json._

import nu.validator.validation._
import nu.validator.xml._
import nu.validator.source._
import nu.validator.messages._
import nu.validator.servlet.imagereview._

class HtmlSpec extends WordSpec with Matchers {
  "Rendering HTML" should {
    "produce valid HTML for the about page" in {
      val html = Html.about(Config("Main title", "subtitle", "nl", "white", "red", List(source))).getBytes("UTF-8")
      validateHtml(html)
    }

    "produce valid HTML for a list page without events" in {
      val html = Html.list("Main title", "subtitle", "nl", "red", List.empty).getBytes("UTF-8")
      validateHtml(html)
    }

    "produce valid HTML for a list page with one event" in {
      val html = Html.list("Main title", "subtitle", "nl", "red", List(event)).getBytes("UTF-8")
      validateHtml(html)
    }

    def validateHtml(html: Array[Byte]) {

      /** Based on https://gist.github.com/vincent-zurczak/23e0f626eaafab96cb32 - TODO needs Scala-ification ;) */
      val out: ByteArrayOutputStream = new ByteArrayOutputStream()
      val sourceCode: SourceCode = new SourceCode()
      val imageCollector: ImageCollector = new ImageCollector(sourceCode)
      val showSource = false
      val emitter: MessageEmitter = new TextMessageEmitter(out, /* asciiQuotes =*/ false)
      val errorHandler: MessageEmitterAdapter =
        new MessageEmitterAdapter(sourceCode, showSource, imageCollector, 0, false, emitter)

      val validator: SimpleDocumentValidator = new SimpleDocumentValidator()
      validator.setUpMainSchema("http://s.validator.nu/html5-rdfalite.rnc", new SystemErrErrorHandler())
      validator.setUpValidatorAndParsers(errorHandler, /* noStream =*/ false, /* loadExternalEnts =*/ false)
      validator.checkHtmlInputSource(new InputSource(new ByteArrayInputStream(html)))

      val successMessage = "Validated successfully"
      errorHandler.end(successMessage, "")

      out.toString.trim should be(successMessage)
      errorHandler.getFatalErrors() should be(0)
      errorHandler.getErrors() should be(0)
      errorHandler.getWarnings() should be(0)
    }
  }

  val source = Source("ES", "Example Source", "http://source.example/example.ics", "http://source.example")
  val vevent = new VEvent()
  vevent.setSummary("Summary")
  val event = Event(LocalDate.now, source, "Some description", None, vevent)
}

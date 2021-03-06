package jstack

import java.io.{PrintWriter, StringWriter}

import io.github.binaryfoo.yatal._
import org.scalajs.dom

import scala.concurrent.ExecutionContext.Implicits.global

case class JstackController(model: JstackModel, output: dom.Element, input: dom.html.TextArea, contains: dom.html.TextArea, viewType: dom.html.Input) {

  def update(): Unit = {
    val matchText = contains.value.toLowerCase
    for (threads <- model.from(input.value)) {
      val filtered = if (matchText.nonEmpty) {
        threads.filter(_.stackMentions(matchText))
      } else {
        threads
      }
      render(filtered, viewType.value)
    }
  }

  def render(threads: Seq[Thread], viewType: String): Unit = {
    val report = new InMemoryHtmlReport()

    viewType match {
      case "byStack" =>
        report.printGroupedByStack(Analyzer.groupByStack(threads).map(_._2))
      case "contention" =>
        report.printBlockingTree(BlockingTree.buildBlockingTree(threads))
      case _ =>
        report.printThreads(threads)
    }

    report.printHeading("Threads in each state")
    report.printStateSummary(Analyzer.groupByState(threads))

    output.innerHTML = report.render
  }
}

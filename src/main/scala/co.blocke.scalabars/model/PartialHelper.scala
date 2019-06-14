package co.blocke.scalabars
package model

import org.json4s._

case class PartialHelper(name: String, t: Template) extends Helper("givenContext") {

  def run()(implicit options: Options, partials: Map[String, Template]): EvalResult[_] = {

    val partialContextCandidate = arg("givenContext") match {
      case NoEvalResult() => options.context
      case e: EvalResult[_] =>
        implicit val baseCtx = options.context
        val c: Context = e // Behold the implicits...
        c
    }

    // NOTE: In Handlebars, AssignmentArguments are merged with the context, not accessed via options.hash, like they are in normal helpers.
    // Therefore, dump hash contents into context before rendering...
    //
    // Unless.... explicitPartialContext is set to true, in which case *only* AssignmentArguments are visible in the partial.  The current
    // context is otherwise thrown away.
    def getHashContents(): List[(String, JValue)] =
      options._hash.map {
        case (k, v) =>
          val v2: JValue = v
          (k, v2)
      }.toList

    val partialContext =
      if (options.hash("explicitPartialContext") == "true")
        partialContextCandidate.copy(value = new JObject(getHashContents()))
      else
        partialContextCandidate.value match {
          case jo: JObject =>
            partialContextCandidate.copy(value = Merge.merge(jo, new JObject(getHashContents())))
          case _ => partialContextCandidate // not an object context... not much we can do about assignments
        }

    val template = t match {
      // If t is empty it means we presume this is a ref to an inline template (stored in context).  Let's go find it...
      case EmptyTemplate() =>
        partialContext.partials.getOrElse(name, throw new BarsException(s"No partial named '${name}' registered"))
      case _ => t
    }

    options._fn match {
      case EmptyTemplate() => // Non-block partial
        template.render(partialContext)
      case _ =>
        val ctx = partialContext.setData("partial-block", options.fn(partialContext))
        template.render(ctx)
    }

    // TODO: Methinks we need to take all the whitespace info and pass it into Options so fn() & render() can process ws properly.
  }
}
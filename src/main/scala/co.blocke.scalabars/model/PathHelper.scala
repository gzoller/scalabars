package co.blocke.scalabars
package model

import org.json4s._
import helpers.stock.EachHelper
import renderables._

case class PathHelper(path: String) extends Helper() {

  def run()(implicit options: Options, partials: Map[String, Template]): EvalResult[_] = {
    (options.context.lookup(path), options._fn, options._inverse) match {
      case (ctx, fn, inv) if ctx.value == JNothing =>
        if (options.hash("strict") == "true" || options.hash("knownHelpersOnly") == "true")
          throw new BarsException("Path or helper not found: " + path)
        val result = options.handlebars.getHelper("helperMissing").get.run()
        if (fn == EmptyTemplate() && inv == EmptyTemplate())
          result // non-block
        else // block
          options.handlebars.getHelper("blockHelperMissing").get.run()

      case (ctx, EmptyTemplate(), EmptyTemplate()) =>
        ctx.toEvalResult // non-block path

      // If either array or object context create a synthetic each for this Handlebars behavior when it's a normal block label (non-partial, non-helper)
      case (ctx, fn, inv) if ctx.value.isInstanceOf[JArray] && options._fn != EmptyTemplate() =>
        val syntheticEach = BlockHelper(
          "each",
          EachHelper(false),
          isInverted = false,
          ParsedExpression("each", Seq(PathArgument(path))),
          3,
          Nil,
          Block(OpenTag(ParsedExpression("each", Seq(PathArgument(path))), false, false, 3), fn.compiled ++ inv.compiled, CloseTag(false, false, 3))
        )
        syntheticEach.eval(options)

      case (ctx, _, _) if options.isFalsy(ctx) => options.inverse(ctx)
      case (ctx, _, _)                         => options.fn(ctx)
    }
  }
}

package co.blocke.scalabars
package builtins.collections

import org.json4s.JArray
import scala.util.{ Try, Success }

case class WithAfterHelper() extends Helper(List("items", "loc")) {
  def run(expr: Expression)(implicit options: Options): StringWrapper =
    Try(resolve("loc").toInt) match {
      case Success(loc) =>
        lookup("items").value match {
          case a: JArray => a.arr.drop(loc).indices.map(i => options.fn(lookup(s"items.[${i + loc}]"))).mkString
          case _         => options.inverse()
        }
      case _ => options.inverse()
    }
}
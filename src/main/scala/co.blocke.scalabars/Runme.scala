package co.blocke.scalabars

import org.graalvm.polyglot.{ Context => GraalContext }

case class Person(name: String, age: Int)
case class Desc(heavy: String)
case class Data(
    name:   String,
    msg:    String,
    aNum:   Int,
    isOK:   Boolean,
    small:  Long,
    A:      List[Desc],
    player: Person
)

case class HashObjHelper() extends Helper() {
  def run(expr: Expression)(implicit options: Options): StringWrapper = "Hashed " + options.hash("msg.age")
  //  def run(expr: Expression)(implicit options: Options): StringWrapper = "Hashed " + resolve(options.hash("msg")+".heavy")
}

object Runme extends App {

  val sb = Scalabars().registerHelper("hashObj", HashObjHelper())

  val c = Data("Greg", "<p>Yay!</p>", 15, false, 2L, List(Desc("cool"), Desc("wicked")), Person("Mike", 32))

  println(sb.compile("Hello, {{hashObj msg=player}}!").render(c))

  //  val jsContext = GraalContext.create("js")
  //  jsContext.eval("js", "console.log('Hello from the project')")

  /*
  val sb = Scalabars(Map("foo" -> FooHelper(), "hash" -> HashHelper()))

  val input = """Hello, {{foo "Greg"}}!"""
  println(sb.compile(input).render(""))

  val i2 = """Hello, {{hash msg="Hola"}}!"""
  println(sb.compile(i2).render(""))

 */
}

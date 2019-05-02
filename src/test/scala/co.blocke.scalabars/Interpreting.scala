package co.blocke.scalabars

import org.scalatest.{ FunSpec, Matchers }

case class Person(name: String, age: Int)
case class Desc(heavy: String)
case class Data(
    name:   String,
    msg:    String,
    A:      List[Desc],
    player: Person
)

class Interpreting extends FunSpec with Matchers {

  val sb = ScalaBars()
    .registerHelper("foo", """function(tag) { return "FooBar "+tag+"!"; }""") // parameter
    .registerHelper("bar", """function() { return "Simple"; }""") // no param
    .registerHelper("hash", """function() { return "Hashed "+this.msg; }""") // parameter
    .registerHelper("hashObj", """function() { return "Hashed "+this.msg.heavy; }""") // object hash param
    .registerHelper("allTypes", """function() { return this.bool+" "+this.num+" "+this.nope+ " "+this.nada+ " "+this.s; }""") // object hash param
  val c = Data("Greg", "<p>Yay!</p>", List(Desc("cool"), Desc("wicked")), Person("Mike", 32))

  describe("-----------------------------\n:  Handlebars Interpreting  :\n-----------------------------") {
    describe("Thing interpreting") {
      it("Interprets 1-element path") {
        sb.compile("Hello, {{name}}!").render(c) should equal("Hello, Greg!")
      }
      it("Interprets 'up level' path") {
        sb.compile("Hello, {{player/../name}}!").render(c) should equal("Hello, Greg!")
      }
      it("Interprets dot separated path") {
        sb.compile("Hello, {{player.age}}!").render(c) should equal("Hello, 32!")
      }
      it("Interprets slash separated path") {
        sb.compile("Hello, {{player/age}}!").render(c) should equal("Hello, 32!")
      }
      it("Interprets array path") {
        sb.compile("Hello, {{A.[1].heavy}}!").render(c) should equal("Hello, wicked!")
      }
      it("Interprets 1-element path (unescaped)") {
        sb.compile("Hello, {{{msg}}}!").render(c) should equal("Hello, <p>Yay!</p>!")
      }
    }
    describe("Helper Interpreting") {
      it("Interprets no-arg helper") {
        sb.compile("Hello, {{bar}}!").render(c) should equal("Hello, Simple!")
      }
      it("Interprets simple string-arg helpers") {
        sb.compile("Hello, {{foo \"Greg\"}}!").render(c) should equal("Hello, FooBar Greg!!")
      }
      it("Interprets complex path-arg helpers") {
        sb.compile("Hello, {{foo A.[1].heavy}}!").render(c) should equal("Hello, FooBar wicked!!")
      }
      it("Interprets helper with hash string assignments") {
        sb.compile("Hello, {{hash msg=\"Hola\"}}!").render(c) should equal("Hello, Hashed Hola!")
      }
      it("Interprets helper with hash path assignments") {
        sb.compile("Hello, {{hash msg=A.[1].heavy}}!").render(c) should equal("Hello, Hashed wicked!")
      }
      it("Interprets helper with hash path/object assignments") {
        sb.compile("Hello, {{hashObj msg=A.[1]}}!").render(c) should equal("Hello, Hashed wicked!")
      }
      it("Interprets all data types of hash params") {
        sb.compile("Hello, {{allTypes bool=true num= 12.34 nope = null nada=undefined s=\"Greg\"}}!").render(c) should equal("Hello, true 12.34 null undefined Greg!")
      }
    }
  }
}

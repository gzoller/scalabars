package co.blocke.scalabars

import org.scalatest.{ FunSpec, Matchers }
import model._

/**
 * There are 16 possible spacing variations indicated by \n before/after the open & close of each block.
 * The combinations are shown below in the 't' declarations.
 *
 * Positions for WS ctl:
 * {{(1)# foo(2)}}
 * A
 * B
 * C
 * {{(3)/foo(4)}}
 */
class BlockSpacing() extends FunSpec with Matchers {

  val sb = Scalabars()
  val json = org.json4s.native.JsonMethods.parse("""
                                                   |{
                                                   |  "title": "My New Post",
                                                   |  "name" : "Greg"
                                                   |}
                                                 """.stripMargin)

  describe("-------------------\n:  Block Spacing  :\n-------------------") {
    describe("Simple replacement") {
      it("Normal (open tag first)") {
        sb.compile("""{{# name}}
                     |What'cha {{this}} doin?
                     |{{/name}}
                     |Done""".stripMargin)(json) should be("""What'cha Greg doin?
                                                             |Done""".stripMargin)
      }
      it("Leading ws (no NL)") {
        sb.compile("""   {{# name}}
                     |What'cha {{this}} doin?
                     |{{/name}}
                     |Done""".stripMargin)(json) should be("""What'cha Greg doin?
                                                             |Done""".stripMargin)
      }
      it("Leading ws (with NL)") {
        // @formatter:off
        sb.compile(
          """
          |  {{# name}}
          |What'cha {{this}} doin?
          |{{/name}}
          |Done""".stripMargin)(json) should be("""
                                                  |What'cha Greg doin?
                                                  |Done""".stripMargin)
        // @formatter:on
      }
      it("Open tag not clear/alone on line") {
        sb.compile("""abc{{# name}}
                     |What'cha {{this}} doin?
                     |{{/name}}
                     |Done""".stripMargin)(json) should be("""abc
                                                             |What'cha Greg doin?
                                                             |Done""".stripMargin)
      }
      it("Trailing char on open tag line") {
        sb.compile("""{{# name}}x
                     |What'cha {{this}} doin?
                     |{{/name}}
                     |Done""".stripMargin)(json) should be("""x
                                                             |What'cha Greg doin?
                                                             |Done""".stripMargin)
      }
      it("Close tag has preceding char") {
        sb.compile("""{{# name}}
                     |What'cha {{this}} doin?
                     |x{{/name}}
                     |Done""".stripMargin)(json) should be("""What'cha Greg doin?
                                                             |x
                                                             |Done""".stripMargin)
      }
      it("Close tag has trailing char") {
        sb.compile("""{{# name}}
                     |What'cha {{this}} doin?
                     |{{/name}}x
                     |Done""".stripMargin)(json) should be("""What'cha Greg doin?
                                                             |x
                                                             |Done""".stripMargin)
      }
    }
    describe("Whitespace control") {
      it("Before open tag") {
        sb.compile("""
                     |  {{~# name}}
                     |What'cha {{this}} doin?
                     |{{/name}}
                     |Done""".stripMargin)(json) should be("""What'cha Greg doin?
                                                             |Done""".stripMargin)
      }
      it("After open tag") {
        sb.compile("""x{{# name~}}
                     |What'cha {{this}} doin?
                     |{{/name}}
                     |Done""".stripMargin)(json) should be("""xWhat'cha Greg doin?
                                                             |Done""".stripMargin)
      }
      it("Before close tag") {
        sb.compile("""{{# name}}
                     |What'cha {{this}} doin?
                     |{{~/name}}
                     |Done""".stripMargin)(json) should be("""What'cha Greg doin?Done""")
      }
      it("After close tag") {
        sb.compile("""{{# name}}
                     |What'cha {{this}} doin?
                     |{{/name~}}
                     |
                     |  Done""".stripMargin)(json) should be("""What'cha Greg doin?
                                                               |Done""".stripMargin)
      }
    }
    describe("Block Partial -- default block") {
      it("Normal") {
        val t =
          """{{#* inline "nombre"}}
            |A
            |  B -- {{name}}
            |C
            |{{/inline}}
            |My name is:
            |{{#>bogus}}
            |Content here
            |{{/bogus}}
            |  Say it loud!""".stripMargin
        sb.compile(t)(json) should be("""My name is:
                                        |Content here
                                        |  Say it loud!""".stripMargin)
      }
      it("No whitespace at all in block") {
        val t =
          """{{#* inline "nombre"}}
            |A
            |  B -- {{name}}
            |C
            |{{/inline}}
            |My name is:
            |{{#>bogus}}Content here{{/bogus}}
            |  Say it loud!""".stripMargin
        sb.compile(t)(json) should be("""My name is:
                                        |Content here
                                        |  Say it loud!""".stripMargin)
      }
      it("No whitespace before") {
        val t =
          """{{#* inline "nombre"}}
            |A
            |  B -- {{name}}
            |C
            |{{/inline}}
            |My name is:{{#>bogus}}
            |Content here
            |{{/bogus}}
            |  Say it loud!""".stripMargin
        sb.compile(t)(json) should be("""My name is:
                                        |Content here
                                        |  Say it loud!""".stripMargin)
      }
      it("No whitespace after") {
        val t =
          """{{#* inline "nombre"}}
            |A
            |  B -- {{name}}
            |C
            |{{/inline}}
            |My name is:
            |{{#>bogus}}
            |Content here
            |{{/bogus}}  Say it loud!""".stripMargin
        sb.compile(t)(json) should be("""My name is:
                                        |Content here
                                        |  Say it loud!""".stripMargin)
      }
      it("ws ctl before open") {
        val t =
          """{{#* inline "nombre"}}
            |A
            |  B -- {{name}}
            |C
            |{{/inline}}
            |My name is:
            |{{~#>bogus}}
            |Content here
            |{{/bogus}}
            |  Say it loud!""".stripMargin
        sb.compile(t)(json) should be("""My name is:Content here
                                        |  Say it loud!""".stripMargin)
      }
      it("ws ctl after open") {
        val t =
          """{{#* inline "nombre"}}
            |A
            |  B -- {{name}}
            |C
            |{{/inline}}
            |My name is:
            |{{#>bogus~}}
            |Content here
            |{{/bogus}}
            |  Say it loud!""".stripMargin
        sb.compile(t)(json) should be("""My name is:
                                        |Content here
                                        |  Say it loud!""".stripMargin)
      }
      it("ws ctl before close") {
        val t =
          """{{#* inline "nombre"}}
            |A
            |  B -- {{name}}
            |C
            |{{/inline}}
            |My name is:
            |{{#>bogus}}
            |Content here
            |{{~/bogus}}
            |  Say it loud!""".stripMargin
        sb.compile(t)(json) should be("""My name is:
                                        |Content here  Say it loud!""".stripMargin)
      }
      it("ws ctl after close") {
        val t =
          """{{#* inline "nombre"}}
            |A
            |  B -- {{name}}
            |C
            |{{/inline}}
            |My name is:
            |{{#>bogus}}
            |Content here
            |{{/bogus~}}
            |  Say it loud!""".stripMargin
        sb.compile(t)(json) should be("""My name is:
                                        |Content here
                                        |Say it loud!""".stripMargin)
      }
    }
    describe("Block Partial -- @partial-block") {
      it("normal") {
        val t =
          """{{#* inline "nombre"}}
            |A
            |  B --
            |  {{@partial-block}}
            |C
            |{{/inline}}
            |My name is:
            |{{#>nombre}}
            |Content here
            |{{/nombre}}
            |  Say it loud!""".stripMargin
        sb.compile(t)(json) should be("""My name is:
                                        |A
                                        |  B --
                                        |  Content here
                                        |
                                        |C
                                        |  Say it loud!""".stripMargin)
      }
      it("no ws before > open tag") {
        val t =
          """{{#* inline "nombre"}}
            |A
            |  B --
            |  {{@partial-block}}
            |C
            |{{/inline}}
            |My name is:{{#>nombre}}
            |Content here
            |{{/nombre}}
            |  Say it loud!""".stripMargin
        // @formatter:off
        sb.compile(t)(json) should be("""My name is:A
                                        |  B --
                                        |  
                                        |Content here
                                        |
                                        |C
                                        |  Say it loud!""".stripMargin)
        // @formatter:on
      }
      it("no ws after > open tag") {
        val t =
          """{{#* inline "nombre"}}
            |A
            |  B --
            |  {{@partial-block}}
            |C
            |{{/inline}}
            |My name is:
            |{{#>nombre}}Content here
            |{{/nombre}}
            |  Say it loud!""".stripMargin
        sb.compile(t)(json) should be("""My name is:
                                        |A
                                        |  B --
                                        |  Content here
                                        |
                                        |C
                                        |  Say it loud!""".stripMargin)
      }
      it("no ws before > close tag") {
        val t =
          """{{#* inline "nombre"}}
            |A
            |  B --
            |  {{@partial-block}}
            |C
            |{{/inline}}
            |My name is:
            |{{#>nombre}}
            |Content here{{/nombre}}
            |  Say it loud!""".stripMargin
        sb.compile(t)(json) should be("""My name is:
                                        |A
                                        |  B --
                                        |  Content here
                                        |C
                                        |
                                        |  Say it loud!""".stripMargin)
      }
      it("no ws after > close tag") {
        val t =
          """{{#* inline "nombre"}}
            |A
            |  B --
            |  {{@partial-block}}
            |C
            |{{/inline}}
            |My name is:
            |{{#>nombre}}
            |Content here
            |{{/nombre}}Say it loud!""".stripMargin
        sb.compile(t)(json) should be("""My name is:
                                        |A
                                        |  B --
                                        |  Content here
                                        |
                                        |C
                                        |Say it loud!""".stripMargin)
      }
      it("no ws before @ tag") {
        val t =
          """{{#* inline "nombre"}}
            |A
            |  B --{{@partial-block}}
            |C
            |{{/inline}}
            |My name is:
            |{{#>nombre}}
            |Content here
            |{{/nombre}}
            |  Say it loud!""".stripMargin
        sb.compile(t)(json) should be("""My name is:
                                        |A
                                        |  B --Content here
                                        |
                                        |C
                                        |  Say it loud!""".stripMargin)
      }
      it("no ws after @ tag") {
        val t = """{{#* inline "nombre"}}
                  |A
                  |  B --
                  |  {{@partial-block}}C
                  |{{/inline}}
                  |My name is:
                  |{{#>nombre}}
                  |Content here
                  |{{/nombre}}
                  |  Say it loud!""".stripMargin
        sb.compile(t)(json) should be("""My name is:
                                        |A
                                        |  B --
                                        |  Content here
                                        |C
                                        |  Say it loud!""".stripMargin)
      }
      it("ws ctl before > open tag") {
        val t =
          """
            |  {{~#* inline "nombre"}}
            |A
            |  B --
            |  {{@partial-block}}
            |C
            |{{/inline}}
            |My name is:
            |{{#>nombre}}
            |Content here
            |{{/nombre}}
            |  Say it loud!""".stripMargin
        sb.compile(t)(json) should be("""My name is:
                                        |A
                                        |  B --
                                        |  Content here
                                        |
                                        |C
                                        |  Say it loud!""".stripMargin)
      }
      it("ws ctl after > open tag") {
        val t =
          """{{#* inline "nombre"~}}
            |A
            |  B --
            |  {{@partial-block}}
            |C
            |{{/inline}}
            |My name is:
            |{{#>nombre}}
            |Content here
            |{{/nombre}}
            |  Say it loud!""".stripMargin
        sb.compile(t)(json) should be("""My name is:
                                        |A
                                        |  B --
                                        |  Content here
                                        |
                                        |C
                                        |  Say it loud!""".stripMargin)
      }
      it("ws ctl before > close tag") {
        val t =
          """{{#* inline "nombre"}}
            |A
            |  B --
            |  {{@partial-block}}
            |C
            |{{~/inline}}
            |My name is:
            |{{#>nombre}}
            |Content here
            |{{/nombre}}
            |  Say it loud!""".stripMargin
        sb.compile(t)(json) should be("""My name is:
                                        |A
                                        |  B --
                                        |  Content here
                                        |
                                        |C  Say it loud!""".stripMargin)
      }
      it("ws ctl after > close tag") {
        val t =
          """{{#* inline "nombre"}}
            |A
            |  B --
            |  {{@partial-block}}
            |C
            |{{/inline~}}
            |My name is:
            |{{#>nombre}}
            |Content here
            |{{/nombre}}
            |  Say it loud!""".stripMargin
        sb.compile(t)(json) should be("""My name is:
                                        |A
                                        |  B --
                                        |  Content here
                                        |
                                        |C
                                        |  Say it loud!""".stripMargin)
      }
      it("ws ctl before @ tag") {
        val t =
          """{{#* inline "nombre"}}
            |A
            |  B --
            |  {{~@partial-block}}
            |C
            |{{/inline}}
            |My name is:
            |{{#>nombre}}
            |Content here
            |{{/nombre}}
            |  Say it loud!""".stripMargin
        sb.compile(t)(json) should be("""My name is:
                                        |A
                                        |  B --Content here
                                        |
                                        |C
                                        |  Say it loud!""".stripMargin)
      }
      it("ws ctl after @ tag") {
        val t =
          """{{#* inline "nombre"}}
            |A
            |  B --
            |  {{@partial-block~}}
            |C
            |{{/inline}}
            |My name is:
            |{{#>nombre}}
            |Content here
            |{{/nombre}}
            |  Say it loud!""".stripMargin
        sb.compile(t)(json) should be("""My name is:
                                        |A
                                        |  B --
                                        |  Content here
                                        |C
                                        |  Say it loud!""".stripMargin)
      }
      it("ws ctl before #> open tag") {
        val t =
          """{{#* inline "nombre"}}
            |A
            |  B --
            |  {{@partial-block}}
            |C
            |{{/inline}}
            |My name is:
            |{{~#>nombre}}
            |Content here
            |{{/nombre}}
            |  Say it loud!""".stripMargin
        sb.compile(t)(json) should be("""My name is:A
                                        |  B --
                                        |  Content here
                                        |
                                        |C
                                        |  Say it loud!""".stripMargin)
      }
      it("ws ctl after #> open tag") {
        val t =
          """{{#* inline "nombre"}}
            |A
            |  B --
            |  {{@partial-block}}
            |C
            |{{/inline}}
            |My name is:
            |{{#>nombre~}}
            |Content here
            |{{/nombre}}
            |  Say it loud!""".stripMargin
        sb.compile(t)(json) should be("""My name is:
                                        |A
                                        |  B --
                                        |  Content here
                                        |
                                        |C
                                        |  Say it loud!""".stripMargin)
      }
      it("ws ctl before #> close tag") {
        val t =
          """{{#* inline "nombre"}}
            |A
            |  B --
            |  {{@partial-block}}
            |C
            |{{/inline}}
            |My name is:
            |{{#>nombre}}
            |Content here
            |{{~/nombre}}
            |  Say it loud!""".stripMargin
        sb.compile(t)(json) should be("""My name is:
                                        |A
                                        |  B --
                                        |  Content here
                                        |C
                                        |  Say it loud!""".stripMargin)
      }
      it("ws ctl after #> close tag") {
        val t =
          """{{#* inline "nombre"}}
            |A
            |  B --
            |  {{@partial-block}}
            |C
            |{{/inline}}
            |My name is:
            |{{#>nombre}}
            |Content here
            |{{/nombre~}}
            |  Say it loud!""".stripMargin
        sb.compile(t)(json) should be("""My name is:
                                        |A
                                        |  B --
                                        |  Content here
                                        |
                                        |C
                                        |Say it loud!""".stripMargin)
      }
    }
  }

  describe("--------------------------------------\n:  Block Spacing (ignoreStandalone)  :\n--------------------------------------") {
    describe("Simple replacement") {
      it("Normal (open tag first)") {
        sb.compile(
          """{{# name}}
            |What'cha {{this}} doin?
            |{{/name}}
            |Done""".stripMargin,
          Map("ignoreStandalone" -> true)
        )(json) should be("""
                            |What'cha Greg doin?
                            |
                            |Done""".stripMargin)
      }
      it("Leading ws (no NL)") {
        // @formatter:off
        sb.compile(
          """   {{# name}}
          |What'cha {{this}} doin?
          |{{/name}}
          |Done""".stripMargin,
          Map("ignoreStandalone" -> true)
        )(json) should be("""   
                            |What'cha Greg doin?
                            |
                            |Done""".stripMargin)
        // @formatter:on
      }
      it("Leading ws (with NL)") {
        // @formatter:off
        sb.compile(
          """
          |  {{# name}}
          |What'cha {{this}} doin?
          |{{/name}}
          |Done""".stripMargin, Map("ignoreStandalone" -> true))(json) should be("""
                                                                                   |  
                                                                                   |What'cha Greg doin?
                                                                                   |
                                                                                   |Done""".stripMargin)
        // @formatter:on
      }
      it("Open tag not clear/alone on line") {
        sb.compile(
          """abc{{# name}}
            |What'cha {{this}} doin?
            |{{/name}}
            |Done""".stripMargin,
          Map("ignoreStandalone" -> true)
        )(json) should be("""abc
                            |What'cha Greg doin?
                            |
                            |Done""".stripMargin)
      }
      it("Trailing char on open tag line") {
        sb.compile(
          """{{# name}}x
            |What'cha {{this}} doin?
            |{{/name}}
            |Done""".stripMargin,
          Map("ignoreStandalone" -> true)
        )(json) should be("""x
                            |What'cha Greg doin?
                            |
                            |Done""".stripMargin)
      }
      it("Close tag has preceding char") {
        sb.compile(
          """{{# name}}
            |What'cha {{this}} doin?
            |x{{/name}}
            |Done""".stripMargin,
          Map("ignoreStandalone" -> true)
        )(json) should be("""
                            |What'cha Greg doin?
                            |x
                            |Done""".stripMargin)
      }
      it("Close tag has trailing char") {
        sb.compile(
          """{{# name}}
            |What'cha {{this}} doin?
            |{{/name}}x
            |Done""".stripMargin,
          Map("ignoreStandalone" -> true)
        )(json) should be("""
                            |What'cha Greg doin?
                            |x
                            |Done""".stripMargin)
      }
    }
    describe("Block Partial -- default block") {
      it("Normal") {
        val t =
          """{{#* inline "nombre"}}
            |A
            |  B -- {{name}}
            |C
            |{{/inline}}
            |My name is:
            |{{#>bogus}}
            |Content here
            |{{/bogus}}
            |  Say it loud!""".stripMargin
        sb.compile(t, Map("ignoreStandalone" -> true))(json) should be("""
                                                                         |My name is:
                                                                         |
                                                                         |Content here
                                                                         |
                                                                         |  Say it loud!""".stripMargin)
      }
      it("No whitespace at all in block") {
        val t =
          """{{#* inline "nombre"}}
            |A
            |  B -- {{name}}
            |C
            |{{/inline}}
            |My name is:
            |{{#>bogus}}Content here{{/bogus}}
            |  Say it loud!""".stripMargin
        sb.compile(t, Map("ignoreStandalone" -> true))(json) should be("""
                                                                         |My name is:
                                                                         |Content here
                                                                         |  Say it loud!""".stripMargin)
      }
      it("No whitespace before") {
        val t =
          """{{#* inline "nombre"}}
            |A
            |  B -- {{name}}
            |C
            |{{/inline}}
            |My name is:{{#>bogus}}
            |Content here
            |{{/bogus}}
            |  Say it loud!""".stripMargin
        sb.compile(t, Map("ignoreStandalone" -> true))(json) should be("""
                                                                         |My name is:
                                                                         |Content here
                                                                         |
                                                                         |  Say it loud!""".stripMargin)
      }
      it("No whitespace after") {
        val t =
          """{{#* inline "nombre"}}
            |A
            |  B -- {{name}}
            |C
            |{{/inline}}
            |My name is:
            |{{#>bogus}}
            |Content here
            |{{/bogus}}  Say it loud!""".stripMargin
        sb.compile(t, Map("ignoreStandalone" -> true))(json) should be("""
                                                                         |My name is:
                                                                         |
                                                                         |Content here
                                                                         |  Say it loud!""".stripMargin)
      }
      it("ws ctl before open") {
        val t =
          """{{#* inline "nombre"}}
            |A
            |  B -- {{name}}
            |C
            |{{/inline}}
            |My name is:
            |{{~#>bogus}}
            |Content here
            |{{/bogus}}
            |  Say it loud!""".stripMargin
        sb.compile(t, Map("ignoreStandalone" -> true))(json) should be("""
                                                                         |My name is:
                                                                         |Content here
                                                                         |
                                                                         |  Say it loud!""".stripMargin)
      }
      it("ws ctl after open") {
        val t =
          """{{#* inline "nombre"}}
            |A
            |  B -- {{name}}
            |C
            |{{/inline}}
            |My name is:
            |{{#>bogus~}}
            |Content here
            |{{/bogus}}
            |  Say it loud!""".stripMargin
        sb.compile(t, Map("ignoreStandalone" -> true))(json) should be("""
                                                                         |My name is:
                                                                         |Content here
                                                                         |
                                                                         |  Say it loud!""".stripMargin)
      }
      it("ws ctl before close") {
        val t =
          """{{#* inline "nombre"}}
            |A
            |  B -- {{name}}
            |C
            |{{/inline}}
            |My name is:
            |{{#>bogus}}
            |Content here
            |{{~/bogus}}
            |  Say it loud!""".stripMargin
        sb.compile(t, Map("ignoreStandalone" -> true))(json) should be("""
                                                                         |My name is:
                                                                         |
                                                                         |Content here
                                                                         |  Say it loud!""".stripMargin)
      }
      it("ws ctl after close") {
        val t =
          """{{#* inline "nombre"}}
            |A
            |  B -- {{name}}
            |C
            |{{/inline}}
            |My name is:
            |{{#>bogus}}
            |Content here
            |{{/bogus~}}
            |  Say it loud!""".stripMargin
        sb.compile(t, Map("ignoreStandalone" -> true))(json) should be("""
                                                                         |My name is:
                                                                         |
                                                                         |Content here
                                                                         |Say it loud!""".stripMargin)
      }
    }
    describe("Block Partial -- @partial-block") {
      it("normal") {
        val t =
          """{{#* inline "nombre"}}
            |A
            |  B --
            |  {{@partial-block}}
            |C
            |{{/inline}}
            |My name is:
            |{{#>nombre}}
            |Content here
            |{{/nombre}}
            |  Say it loud!""".stripMargin
        sb.compile(t, Map("ignoreStandalone" -> true))(json) should be("""
                                                                         |My name is:
                                                                         |
                                                                         |A
                                                                         |  B --
                                                                         |  
                                                                         |Content here
                                                                         |
                                                                         |C
                                                                         |
                                                                         |  Say it loud!""".stripMargin)
      }
      it("no ws before > open tag") {
        val t =
          """{{#* inline "nombre"}}
            |A
            |  B --
            |  {{@partial-block}}
            |C
            |{{/inline}}
            |My name is:{{#>nombre}}
            |Content here
            |{{/nombre}}
            |  Say it loud!""".stripMargin
        // @formatter:off
        sb.compile(t, Map("ignoreStandalone" -> true))(json) should be("""
                                                                         |My name is:
                                                                         |A
                                                                         |  B --
                                                                         |  
                                                                         |Content here
                                                                         |
                                                                         |C
                                                                         |
                                                                         |  Say it loud!""".stripMargin)
        // @formatter:on
      }
      it("no ws after > open tag") {
        val t =
          """{{#* inline "nombre"}}
            |A
            |  B --
            |  {{@partial-block}}
            |C
            |{{/inline}}
            |My name is:
            |{{#>nombre}}Content here
            |{{/nombre}}
            |  Say it loud!""".stripMargin
        sb.compile(t, Map("ignoreStandalone" -> true))(json) should be("""
                                                                         |My name is:
                                                                         |
                                                                         |A
                                                                         |  B --
                                                                         |  Content here
                                                                         |
                                                                         |C
                                                                         |
                                                                         |  Say it loud!""".stripMargin)
      }
      it("no ws before > close tag") {
        val t =
          """{{#* inline "nombre"}}
            |A
            |  B --
            |  {{@partial-block}}
            |C
            |{{/inline}}
            |My name is:
            |{{#>nombre}}
            |Content here{{/nombre}}
            |  Say it loud!""".stripMargin
        sb.compile(t, Map("ignoreStandalone" -> true))(json) should be("""
                                                                         |My name is:
                                                                         |
                                                                         |A
                                                                         |  B --
                                                                         |  
                                                                         |Content here
                                                                         |C
                                                                         |
                                                                         |  Say it loud!""".stripMargin)
      }
      it("no ws after > close tag") {
        val t =
          """{{#* inline "nombre"}}
            |A
            |  B --
            |  {{@partial-block}}
            |C
            |{{/inline}}
            |My name is:
            |{{#>nombre}}
            |Content here
            |{{/nombre}}Say it loud!""".stripMargin
        sb.compile(t, Map("ignoreStandalone" -> true))(json) should be("""
                                                                         |My name is:
                                                                         |
                                                                         |A
                                                                         |  B --
                                                                         |  
                                                                         |Content here
                                                                         |
                                                                         |C
                                                                         |Say it loud!""".stripMargin)
      }
      it("no ws before @ tag") {
        val t =
          """{{#* inline "nombre"}}
            |A
            |  B --{{@partial-block}}
            |C
            |{{/inline}}
            |My name is:
            |{{#>nombre}}
            |Content here
            |{{/nombre}}
            |  Say it loud!""".stripMargin
        sb.compile(t, Map("ignoreStandalone" -> true))(json) should be("""
                                                                         |My name is:
                                                                         |
                                                                         |A
                                                                         |  B --
                                                                         |Content here
                                                                         |
                                                                         |C
                                                                         |
                                                                         |  Say it loud!""".stripMargin)
      }
      it("no ws after @ tag") {
        val t = """{{#* inline "nombre"}}
                  |A
                  |  B --
                  |  {{@partial-block}}C
                  |{{/inline}}
                  |My name is:
                  |{{#>nombre}}
                  |Content here
                  |{{/nombre}}
                  |  Say it loud!""".stripMargin
        sb.compile(t, Map("ignoreStandalone" -> true))(json) should be("""
                                                                         |My name is:
                                                                         |
                                                                         |A
                                                                         |  B --
                                                                         |  
                                                                         |Content here
                                                                         |C
                                                                         |
                                                                         |  Say it loud!""".stripMargin)
      }
    }
  }
}

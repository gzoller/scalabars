package co.blocke.scalabars
package model

/**
 * A thing can be rendered to a String (output).  We return Options as well because it's possible render
 * can alter Options (known instance of this is Inline Partials add themselves to options.context).
 */
trait Renderable {
  def render(rc: RenderControl): RenderControl
}

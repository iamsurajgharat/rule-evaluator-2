package io.github.iamsurajgharat
package ruleevaluator
package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import org.antlr.v4.runtime.ANTLRInputStream
import io.github.iamsurajgharat.ruleevaluator.antlr4.ArrayInitLexer
import org.antlr.v4.runtime.CommonTokenStream
import io.github.iamsurajgharat.ruleevaluator.antlr4.ArrayInitParser

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Action { implicit request: Request[AnyContent] =>
    fun1()
    Ok(views.html.index())
  }

  def fun1():Unit = {
    val input = new ANTLRInputStream("{10,20,30,40}")
    val lexer = new ArrayInitLexer(input)
    val tokens = new CommonTokenStream(lexer)
    val parser = new ArrayInitParser(tokens)
    val tree = parser.init()
    println("TreeThings: "+tree.toStringTree(parser))
  }
}

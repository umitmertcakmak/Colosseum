package controllers

import play.api._
import play.api.mvc._
import play.api.libs.ws.WS
import play.api.Play.current
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import _root_.yahoofinance.YahooFinance
import models.Quote

import scala.concurrent.{Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import scala.util.Random

/**
  * Created by umitcakmak on 20/03/17.
  */
object Api extends Controller{

  // type class for Quote -> Json conversion
  implicit val writesQuote = new Writes[Quote] {
    def writes(quote:Quote) = Json.obj(
      "name" -> quote.name,
      "symbol" -> quote.symbol,
      "price" -> quote.price
    )
  }

  // type class for Google Json -> Quote conversion
  implicit val readsPriceFromYahoo:Reads[Quote] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "symbol").read[String] and
      (JsPath \ "price").read[BigDecimal]
    )(Quote.apply _)


  def print_result(quote:Quote) = {
    Json.toJson(quote)
  }

  def getQuote(stockSymbol:String) = Action.async { request =>

    val ticker = request.getQueryString("ticker") match {
      case Some(i) => i
      case None => "GOOG"
    }

    val f:Future[Quote] = Future {
      val stock = YahooFinance.get(ticker)
      val quote = stock.getQuote
      Quote(stock.getName, stockSymbol, quote.getPrice)
    }

    f.map { quote =>
      if(stockSymbol == "AAPL") {
        Ok(print_result(quote))
      } else {
        NotFound
      }
    }
  }

}

package appbuilder

import scala.concurrent._
import scala.concurrent.duration._
import akka.actor._
import akka.pattern._
import akka.util._

import scala.language.postfixOps

import scala.concurrent.ExecutionContext.Implicits.global

import sys.process._

import play.api.libs.json._

////////////////////////////////////////////////////////////////////

object MySys
{
	def Issue(command:String)
	{
		Future
		{
			println(command)
			command !!
		}
	}

	def TryJson
	{
		val json: JsValue = Json.parse("""
		{
		  "name" : "Watership Down",
		  "location" : {
		    "lat" : 51.235685,
		    "long" : -1.309197
		  },
		  "residents" : [ {
		    "name" : "Fiver",
		    "age" : 4,
		    "role" : null
		  }, {
		    "name" : "Bigwig",
		    "age" : 6,
		    "role" : "Owsla"
		  } ]
		}
		""")

		val lat:String = (json \ "location" \ "lat").toString
		val role = ((json \ "residents")(0) \ "role")

		println(lat,role,role==JsNull)
	}
}
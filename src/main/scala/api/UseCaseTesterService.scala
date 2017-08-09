package api

import java.io.{FileOutputStream, File}

import akka.actor.Actor
import plugin.SyntacticQualityPlugin
import pluginUtil.PluginManager
import spray.routing._
import spray.http._
import MediaTypes._

import spray.httpx.SprayJsonSupport
import spray.json._

import scala.xml._

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class UseCaseTesterActor extends Actor with UseCaseTesterService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute)
}


case class Test(testName: String, tipo: String, elements: Seq[(String,String)])

case class TestCollection(test:Seq[Test])

case class Response(status: String, status_message:String, data:Seq[TestCollection])




// this trait defines our service behavior independently from the service actor
trait UseCaseTesterService extends HttpService with SprayJsonSupport with DefaultJsonProtocol {

  implicit object AdressJsonFormat extends RootJsonFormat[(String,String)] {
    def write(dato: (String,String)) = JsObject(Map(
      "id" -> JsString(dato._1),
      "name" -> JsString(dato._2)
    ))
    def read(value: JsValue): (String,String) = ???
  }

  implicit val testFormat = jsonFormat3(Test)
  implicit val testcollectionFormat = jsonFormat1(TestCollection)
  implicit val responseFormat = jsonFormat3(Response)

  var ns = "";

  val myRoute =
    path("") {
      get {
        respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
          complete {
            <html>
              <body>
                <h1>Say hello to <i>spray-routing</i> on <i>spray-can</i>!</h1>
              </body>
            </html>
          }
        }
      }
    } ~
      path("uploadXMI"){
        get {
          respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
            complete {
              <html>
                <body>
                  <h1>asdasd</h1>
                </body>
              </html>
            }
          }
        } ~
          post {
            entity(as[MultipartFormData]) {
              formData => {
                val ftmp = File.createTempFile("upload", ".xmi", new File("/tmp"))
                val output = new FileOutputStream(ftmp)
                formData.fields.foreach(f => output.write (f.entity.data.toByteArray ) )
                output.close()
                complete("done, file in: " + ftmp.getName())
              }
            }
          }

      } ~
      path("SyntacticQualityTest"){
        get {
          respondWithMediaType(`application/json`) {
            parameters('fileId) { (fileId) =>
              val xmi = XML.loadFile(s"/tmp/$fileId.xmi")
              ns = xmi.getNamespace("xmi")
              val plugin = PluginManager.getPlugin("SyntacticQuality").asInstanceOf[SyntacticQualityPlugin]
              plugin.nameSpace(xmi.getNamespace("xmi"))
              val lista = List(
                  Test("UseCaseOutsideBoundarie", "wrong", plugin.useCaseOutSideBorder(xmi)),
                  Test("ActorInsideSystem", "wrong", plugin.actorWithinBorder(xmi)),
                  Test("ActorUnrelated", "wrong", plugin.unrelatedActors(xmi))
              )
              val testCollection = TestCollection(lista)
              val lista2 = List(testCollection)
              val response = Response("bad","File not found",lista2)
              complete(response)
            }
          }
        }

      }

}
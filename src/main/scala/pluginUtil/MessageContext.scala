package pluginUtil

/**
 * Created by fernando on 12/4/15.
 */
trait MessageContext {

  //def toXml(): Elem
  //def setPayload(payload:Payload): Unit
  //def getPayload(): Payload
  def addOrAppendElement(elementName: String, data: String)
  def addOrReplaceElement(elementName: String, elementData: String)
  def getElementData(elementName: String): String

}

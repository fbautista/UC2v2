package plugin

import com.sun.org.apache.xpath.internal.axes.NodeSequence
import pluginUtil.{BasePlugin, MessageContext}
import scala.xml._
/**
 * Created by fernando on 12/4/15.
 */
class SyntacticQualityPlugin extends BasePlugin {

  def name(): String = {
    "SyntacticQuality"
  }

  def performAction(context:MessageContext):Boolean = {
    val sleepTime = java.lang.Math.random() * 501
    //val payload = context.getPayload()
    println("")
    println("I am busy for " + sleepTime/1000 + " seconds")
    Thread.sleep(sleepTime.toLong)
    println("")
    return true
  }

  def result(): String = {
    "Hello"
  }

  var ns = "";

  def nameSpace(name: String): Unit = {
    ns=name
  }

  def packagedElements(nodes: NodeSeq): NodeSeq = nodes \\ "packagedElement"

  def packagedElementsByType(nodes: NodeSeq, elementType: String): NodeSeq = nodes \\ "packagedElement" filter (_ \@ ("{" + ns + "}type") == elementType)

  def idAndNameFromNode(node: Node): (String, String) =
    (node \@ ("{" + ns + "}id"), node \@ "name")

  def models(nodes: NodeSeq) = packagedElementsByType(nodes, "uml:Model") map idAndNameFromNode

  def packages(nodes: NodeSeq) = packagedElementsByType(nodes, "uml:Package") map idAndNameFromNode

    def useCases(nodes: NodeSeq) = packagedElementsByType(nodes, "uml:UseCase") map idAndNameFromNode

    def actors(nodes: NodeSeq) = packagedElementsByType(nodes, "uml:Actor") map idAndNameFromNode

    def associations(nodes: NodeSeq) = {
      val as = packagedElementsByType(nodes, "uml:Association")
      val ownedEnds = as map (_ \\ "ownedEnd")
      ownedEnds map (o => (o(0) \@ "type", o(1) \@ "type"))
    }

    def extendss(nodes: NodeSeq) = {
      val pe = packagedElements(nodes)
      for {
        as <- pe
        ex <- as \ "extend"
      } yield (as \@ ("{" + ns + "}id"), ex \@ "extendedCase")
    }

  def includes(nodes: NodeSeq) = {
    val pe = packagedElements(nodes)
    for {
      as <- pe
      in <- as \ "include"
    } yield (as \@ ("{" + ns + "}id"), in \@ "addition")
  }

  def in(nodes: NodeSeq) = {
    val pe = packagedElements(nodes)
    for {
      as <- pe
      spe <- as \ "packagedElement"
    } yield (spe \@ ("{" + ns + "}id"), as \@ ("{" + ns + "}id"))
  }


  def unrelatedActors(nodes: NodeSeq) = {
    val as = actors(nodes)
    val bs = associations(nodes)
    as filter { case (aid, _) => ! (bs exists { case (bid, _) => aid == bid }) }
  }


  def directConnectedUC(nodes: NodeSeq) = {
    val ac = actors(nodes)
    val bs = associations(nodes)
    val cu = useCases(nodes)
    cu filter { case (ucId, _) => (ac exists { case (acId,_) => (bs exists {case (ac2Id, uc2Id) => (acId==ac2Id && ucId==uc2Id) })})}

  }

  def isolatedUC(nodes: NodeSeq) = {
    val cu = useCases(nodes)
    val cc = connectedUC(nodes)
    cu filter {case (aid, _) => ! (cc exists { case (bid, _) => aid == bid})}
  }


  def actorWithinBorder(nodes: NodeSeq) = {
    val as = actors(nodes)
    val inn = in(nodes)
    as filter { case (aid,_) => (inn exists {case (bid,_) => aid == bid })}
  }

  def useCaseOutSideBorder(nodes: NodeSeq) = {
    val uc = useCases(nodes)
    val inn = in(nodes)
    uc filter { case (ucid,_) => ! (inn exists {case (bid,_) => ucid == bid })}
  }

}
package pluginUtil

import java.io.File

import com.typesafe.scalalogging.Logger
import org.clapper.classutil.ClassFinder
import org.slf4j.LoggerFactory

/**
 * Created by fernando on 12/4/15.
 */
object PluginManager {

  var pluginMap = Map[String, String]()
  //val logger = Logger(PluginManager.getClass())
  //val logger = Logger(LoggerFactory.getLogger(this.getClass))

  val logger = Logger(LoggerFactory.getLogger("name"))

  def init() {
    val classpath = List(".").map(new File(_))
    val finder = ClassFinder(classpath)
    val classes = finder.getClasses  // classes is an Iterator[ClassInfo]
    val classMap = ClassFinder.classInfoMap(classes.toIterator)

    val plugins = ClassFinder.concreteSubclasses("pluginUtil.BasePlugin", classMap)

    plugins.foreach {
      pluginString =>
        val plugin = Class.forName(pluginString.name).newInstance().asInstanceOf[BasePlugin]
        pluginMap += (plugin.name -> pluginString.name)
    }
  }

  def getPlugin(name: String): BasePlugin = {
    if (pluginMap.isEmpty) init
    logger.debug("Fetching plugin from non-persistent pool.")
    Class.forName(pluginMap(name)).newInstance().asInstanceOf[BasePlugin]
  }

}

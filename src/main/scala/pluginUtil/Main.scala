package pluginUtil

object HelloWorld {
  def main(args: Array[String]) {
    println("Hello, world!")
    val as = PluginManager.getPlugin("SyntacticQuality")
    println(as.name)

    println("Hello, world2!")

  }
}
package pluginUtil

/**
 * Created by fernando on 12/4/15.
 */
trait BasePlugin {
  def name:String
  def performAction(context:MessageContext):Boolean
  def result:String
}

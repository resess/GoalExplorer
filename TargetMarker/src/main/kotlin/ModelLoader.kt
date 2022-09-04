import android.goal.explorer.cmdline.GlobalConfig
import android.goal.explorer.model.App
import android.goal.explorer.model.stg.output.OutSTG
import android.goal.explorer.topology.TopologyExtractor
import android.hierarchy.MethodSignatures
import com.thoughtworks.xstream.XStream
import soot.Scene
import soot.SootMethod
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration
import soot.jimple.infoflow.android.SetupApplication
import soot.jimple.infoflow.android.callbacks.AndroidCallbackDefinition
import st.cs.uni.saarland.de.reachabilityAnalysis.UiElement
import java.io.File
import java.io.FileInputStream
import java.io.ObjectInputStream
import java.lang.IllegalArgumentException

class ModelLoader {
    companion object {
        lateinit var loadedAppModel: App

        lateinit var stg: OutSTG


        fun initializeModels(config: GlobalConfig, folderPath: String?, stgPath: String) {
            //load stg model
            stg = loadSTG(stgPath) //here get stg name
            //load app model
            loadedAppModel = loadAppModel(folderPath, config)
            //load callback provider

        }

        private fun loadSTG(xmlFilePath: String): OutSTG {
            var xStream = XStream()
            xStream.setMode(XStream.NO_REFERENCES)
            XStream.setupDefaultSecurity(xStream)
            xStream.allowTypesByWildcard(arrayOf("android.goal.explorer.model.stg.output.**"))
            xStream.processAnnotations(OutSTG::class.java)
            //xStream.autodetectAnnotations(true)
            val xmlFile = File(xmlFilePath)
            if(xmlFile.exists()) {
                return (xStream.fromXML(xmlFile) as OutSTG)
            }
            throw IllegalArgumentException("Valid stg file path required for target marking!")
        }


        private fun loadAppModel(appModelFile: String?, config: GlobalConfig): App {
            val flowDroidModel = SetupApplication(config.flowdroidConfig)
            flowDroidModel.constructCallgraph()
            if(appModelFile != null){
                val model = (ObjectInputStream(FileInputStream(appModelFile)).readObject() as App)
                model.initializeAppModel(flowDroidModel)
                TopologyExtractor(model, config.timeout, config.numThread).extractTopopology()
                return model
            }
            val appModel = App()
            appModel.initializeAppModel(flowDroidModel)
            TopologyExtractor(appModel, config.timeout, config.numThread).extractTopopology()

            return appModel

        }


        fun getCallbacksForActivity(activityName: String): Set<AndroidCallbackDefinition> =
            //loadedAppModel.getCallbacksInSootClass(Scene.v().getSootClass(activityName))
            loadedAppModel.getActivityByName(activityName).callbacks /*{
            if (loadedAppModel != null)
                return :loadedAppModel.getActivityByName(activityName).callbacks
            return flowDroidModel.callbackMethods.get(Scene.v().getSootClass(activityName))*/

        fun getCallbacksForService(serviceName: String): Set<AndroidCallbackDefinition> =
            loadedAppModel.getServiceByName(serviceName).callbacks

        fun getCallbacksForReceiver(receiverName: String): Set<AndroidCallbackDefinition> =
            loadedAppModel.getReceiverByName(receiverName).callbacks

        fun getMenuCallbacksForActivity(activityName: String): Set<SootMethod> =
            loadedAppModel.getActivityByName(activityName).menuCallbackMethods.map { it.method() }.toSet()


        fun getLifecycleMethodsForActivity(activityName: String): List<SootMethod> =
            loadedAppModel.getActivityByName(activityName).lifecycleMethodsPreRun.map { it.method() }

        fun getLifecycleMethodsForService(serviceName: String): List<SootMethod> =
            loadedAppModel.getServiceByName(serviceName).lifecycleMethods.map { it.method() }

        fun getLifecycleMethodsForReceiver(receiverName: String): List<SootMethod> =
            loadedAppModel.getReceiverByName(receiverName).lifecycleMethods.map { it.method() }

        fun getUiElementsForActivity(activityName: String): Collection<UiElement> =
            loadedAppModel.getActivityByName(activityName).uiElements


        /*{
            if(loadedAppModel != null)
                return loadedAppModel.getActivityByName(activityName).lifecycleMethodsPreRun.map { it.method() }
            val callbacks = getCallbacksForActivity(activityName)
            return launchLifecycleMethods.map { lifecycle -> callbacks.find { it.targetMethod.subSignature == lifecycle.sootMethodSubSignature }?.targetMethod
                ?: null }.filterNotNull().toList()

            }*/

        //Load the serialized app model + backstage model if available
    }

}
import android.goal.explorer.SaveData
import android.goal.explorer.cmdline.CmdLineParser
import android.goal.explorer.model.stg.output.OutSTG
import org.slf4j.LoggerFactory

import soot.Scene
import java.awt.event.MouseEvent

class Main {
    companion object {
        private val logger = LoggerFactory.getLogger(Main::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            logger.info("Running target marker ...")
            val config = CmdLineParser.parse(args)
            val settings = CmdLineParser.parseArgForBackstage(config)
            //Need to parse apk name, model folder, target type
            //val flowdroidConfig = config.flowdroidConfig
            val stgPath = config.precomputedSTG
            val modelFolder = config.precomputedModelFolder
            val targetList = config.targets
            val targetType = "API"
            val apiList = targetList

            if (targetType.equals("API") && apiList.isNotEmpty()) {
                logger.info("The model path is {}",modelFolder)
                ModelLoader.initializeModels(config, modelFolder, stgPath)
                val marks = TargetMarker.markScreenNodesIfRecursivelyCallsMethodName(apiList)
                val outSTG = OutSTG(ModelLoader.stg, marks)
                // print the results to XML file
                val saveData = SaveData(outSTG, config)
                saveData.saveSTG()

            }
            else{
            }
        }

    }
}
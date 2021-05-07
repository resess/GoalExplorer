import android.goal.explorer.SaveData
import android.goal.explorer.model.stg.output.OutSTG
import android.goal.explorer.model.stg.output.OutScreenNode
import android.model.Screen
import org.slf4j.LoggerFactory
import soot.SootMethod

class AnalysisRunner(private val preAnalysisRunner: PreAnalysisRunner, private val methods: List<SootMethod>): Runnable {
    override fun run() {
        // get the constructed STG
        val stg = preAnalysisRunner.stgExtractor.stg

        // mark the target screen nodes that can call the passed methods
//        val marks = TargetMarker.markScreenNodesIfRecursivelyCallsSootMethod(stg.allScreens, methods)
        val methodNames = methods.map { it.name }.distinct()
        val marks = TargetMarker.markScreenNodesIfRecursivelyCallsMethodName(stg.allScreens, methodNames)
        val outSTG = OutSTG(stg, marks)

        // print the results to XML file
        val saveData = SaveData(outSTG, preAnalysisRunner.config)
        saveData.saveSTG()
    }
}

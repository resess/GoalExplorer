import android.goal.explorer.SaveData
import android.goal.explorer.model.stg.output.OutSTG
import soot.SootMethod

class AnalysisRunner(private val preAnalysisRunner: PreAnalysisRunner, private val methods: List<SootMethod>): Runnable {
    override fun run() {
        // get the constructed STG
        val stg = preAnalysisRunner.stgExtractor.stg
        val app = preAnalysisRunner.stgExtractor.app

        //TODO retrieve active bodies

        // mark the target screen nodes that can call the passed methods
        val methodNames = methods.map { it.name }.distinct()
        val marks = TargetMarker.markSrcOrDestinationScreenNodesIfRecursivelyCallsMethodName(stg, methodNames)

        val outSTG = OutSTG(stg, marks)

        // print the results to XML file
        val saveData = SaveData(outSTG, preAnalysisRunner.config)
        saveData.saveSTG()
    }
}

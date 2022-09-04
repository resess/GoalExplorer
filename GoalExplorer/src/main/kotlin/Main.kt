import android.goal.explorer.cmdline.CmdLineParser
import soot.Scene

import org.slf4j.LoggerFactory

// entrypoint to make our testing simpler
class Main {
    //private val flowDroidAnalysis: SetupApplication = SetupApplication(android)
    companion object {
         private val logger = LoggerFactory.getLogger(Main::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            val config = CmdLineParser.parse(args)

            //TODO, if xml file given as parameter, preanalysis runner loads the outstg in memory
             //and initialize flowdroid + widget provider

             //then do the analysis with the stg

            val preRunner = PreAnalysisRunner(config, CmdLineParser.parseArgForBackstage(config))
            preRunner.run()
            //here should log end of stg construction for stats

            //val stgAnalysis = ScreenTransitionGraphAnalysis();

            // get the API to target mark
            //val urlClass = Scene.v().getSootClass("java.net.URL")
            val urlClasses = listOf(Scene.v().getSootClass("java.net.HttpURLConnection"), 
            Scene.v().getSootClass("java.net.URLConnection"),
            Scene.v().getSootClass("java.net.URL"))

            val methods = urlClasses.flatMap { it.methods.filter {it.name == "openConnection"} }
            logger.debug("Methods to check $methods")
            val runner = AnalysisRunner(preRunner, methods)
            runner.run()
        }
    }
}

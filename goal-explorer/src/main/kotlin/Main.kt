import android.goal.explorer.cmdline.CmdLineParser
import soot.Scene

// entrypoint to make our testing simpler
class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val config = CmdLineParser.parse(args)

            val preRunner = PreAnalysisRunner(config, CmdLineParser.parseArgForBackstage(config))
            preRunner.run()

            // get the API to target mark
            val urlClass = Scene.v().getSootClass("java.net.URL")
            val openConnectionMethods = urlClass.methods.filter { it.name == "openConnection" }

            val runner = AnalysisRunner(preRunner, openConnectionMethods)
            runner.run()
        }
    }
}

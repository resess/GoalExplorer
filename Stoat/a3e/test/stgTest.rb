require_relative '../bin/log'
require_relative '../bin/stg'

stg = STG.construct_stg("#{__dir__}/stg/aagtl-instrumented_stg.xml")
# paths = stg.all_paths(stg.node("com.asuc.asucmobile.main.MainActivity"), stg.node("com.asuc.asucmobile.main.CreditsDialog"))
Log.print("# of activities:#{stg.nodes.length}")
Log.print("Construction Successful")
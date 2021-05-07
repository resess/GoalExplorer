require_relative '../bin/bash'
require_relative '../bin/log'

apk_path = "#{__dir__}/apk/#{ARGV[0]}.apk"

Log.print("** STARTING STOAT SERVER FOR #{apk_path} **")
Dir.chdir("#{__dir__}/../../") do
  Bash.run("bash -x ./bin/analyzeAndroidApk.sh fsm_apk #{apk_path} apk #{apk_path} &> /dev/null &")
end

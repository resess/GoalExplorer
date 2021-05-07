#!/usr/bin/env ruby

# this script runs stoat to test android apps
# Preparation before testing: 1. open network, 2. disable keyborad in android (e.g., using nullkeyboard)
require 'optparse'

require_relative '../utils/bash_utils'
require_relative '../utils/avd_utils'

STOAT_ROOT_DIR = File.dirname(__dir__)
puts "ENV: Stoat tool dir:#{STOAT_ROOT_DIR}"

def cleanup(avd_serial)
  execute_shell_cmd("adb -s #{avd_serial} emu kill")
end

# install the app
def install_app(avd_serial, apk_path)
  res = execute_shell_cmd("adb -s #{avd_serial} install -r #{apk_path}")
  puts "Output: #{res}"

  unless res.include? 'Success'
    puts "Failed to install: #{res}"
    file = File.open("#{STOAT_ROOT_DIR}/logs/StoatLogs.txt", 'w')
    cleanup(avd_serial)
    exit(1)
  end
end

def uninstall_app(avd_serial, apk_path)
  puts 'Uninstalling the app'
  res = execute_shell_cmd("aapt dump badging #{apk_path} | grep package | awk '{print $2}' | sed s/name=//g | sed s/\\'//g")
  package_name = res.strip
  execute_shell_cmd("timeout 10s adb -s #{avd_serial} uninstall #{package_name}")
end

# construct fsm for open source app, given app's dir, emma-instrumented
def construct_fsm(apk_path, avd_serial, stoat_port, stg_file,
                  timeout, max_events, event_delay)

  # start the stoat server
  puts "** RUNNING STOAT FOR #{apk_path} **"
  Dir.chdir(STOAT_ROOT_DIR) do
    job2 = fork do
      cmd = "bash -x ./bin/analyzeAndroidApk.sh fsm_apk #{apk_path} apk #{apk_path} &> /dev/null &"
      puts "$ #{cmd}"
      exec cmd
    end
    Process.detach(job2)
  end

  # start the stoat client for gui exploration
  Dir.chdir("#{STOAT_ROOT_DIR}/a3e") do
    execute_shell_cmd_output("timeout #{timeout} ruby ./bin/rec.rb "\
      "--app #{apk_path} --apk #{apk_path} --dev #{avd_serial} "\
      "--port #{stoat_port} --no-rec -loop --search weighted "\
      "--events #{max_events} --event_delay #{event_delay} "\
      "--stg #{stg_file}")
  end

  puts '** FINISH STOAT FOR FSM BUILDING'
  cleanup(avd_serial)
end

#### CONSTANTS FOR RUNNING OF TOOL

# the default configuration when using emulators
avd_name = 'goalexplorer'
avd_serial = 'emulator-5554'
avd_port = 5554
# avd_sdk_version = 'android-30'

# the default configuration for the stoat server port
stoat_port = '2000'

# the traversal configurations timeouts for running
max_exploration_time = '90m'
max_event_number = 10

# the delay time between events (in milliseconds)
event_delay = 300

# mandatory options
apk_path = ''

### ADDITIONS FROM GOAL-EXPLORER
# steps to retrace before testing begins
steps_list = nil

# file to construct STG with
stg_file = nil

# clean up for ctrl+c
trap('INT') do
  puts 'Shutting down.'
  execute_shell_cmd("kill -9 `ps | grep dumpCoverage | awk '{print $1}'`")
  execute_shell_cmd("for pid in $(ps | grep ruby | awk '{print $1}'); do kill -9 $pid; done")
  execute_shell_cmd("adb kill-server")
  exit
end


OptionParser.new do |opts|
  opts.banner = "Usage: ruby #{__FILE__} [options]\n \tStoat can test open-source projects (Ant projects -- Emma, Gradle projects -- Jacoco) and closed-source projects (Ant/Gradle projects -- Ella), Developers: Ting Su (tsuletgo@gmail.com), Guozhu Meng, copyright reserved, 2015-2017"
  opts.on('--avd_name avd', 'your own Android Virtual Device') do |n|
    avd_name = n
  end
  opts.on('--avd_port port', 'the serial port number of Android Virtual Device, e.g., 5554') do |p|
    avd_port = p
    avd_serial = 'emulator-' + p
  end
  opts.on('--avd_sdk_version version', 'the sdk version of Android Virtual Device, e.g., android-18') do |v|
    avd_sdk_version = v
  end

  opts.on('--stoat_port port', "the communication port between Stoat's server and client side, e.g., 2000") do |c|
    stoat_port = c
  end

  opts.on('--apk_path path', 'the path of the apk to test (relative path or absolute path) ') do |a|
    apk_path = a
  end

  ### ADDED FROM GOAL-EXPLORER
  opts.on('--retrace_steps steps_file', 'the file with steps to run before testing begins') do |it|
    steps_list = it
    puts "steps_list: #{steps_list}"
  end
  opts.on('--stg file', 'XML file for STG') do |it|
    stg_file = it
    puts "stg_file: #{stg_file}"
  end
  opts.on_tail('-h', '--help', "show this message. Note before testing an app, please set \"hw.keyboard=yes\" in the emulator's config file \"~/.android/avd/testAVD_1.avd/config.ini\"  and open the wifi network. \n\n Examples: \n \t
	<Ant opens-soruce projects>\n \t ruby run_stoat_testing.rb --app_dir /home/suting/proj/mobile_app_benchmarks/test_apps/caldwell.ben.bites_4_src --avd_name testAVD_1 --avd_port 5554 --stoat_port 2000 --project_type ant \n \t
	<Gradle open-source projects>\n \t ruby run_stoat_testing.rb --app_dir /home/suting/proj/mobile_app_benchmarks/test_apps/tests/com.linuxcounter.lico_update_003_5_src.tar.gz --apk /home/suting/proj/mobile_app_benchmarks/test_apps/tests/com.linuxcounter.lico_update_003_5_src.tar.gz/app/build/outputs/apk/app-instrumented.apk --avd_name testAVD_1 --avd_port 5554 --stoat_port 2000 --project_type gradle \n \t
	<apk without instrumentation> \n\t Note this may mitigate Stoat's power due to lack of coverage info for test optimization. \n\t ruby run_stoat_testing.rb --app_dir /home/suting/proj/mobile_app_benchmarks/test_apps/Bites.apk --avd_name testAVD_1 --avd_port 5554 --stoat_port 2000 (the output will be under \"Bites-output\")\n \t
	<Use real device, ant projects>\n\t Please open wifi, and disable keyboard before do testing on real device! \n \t ruby run_stoat_testing.rb --app_dir /home/suting/proj/mobile_app_benchmarks/dyno-droid-fse13-apps/caldwell.ben.bites_4_src/ --real_device_serial cf00b9e6 --stoat_port 2000 --project_type ant \n\t
	<a set of apps> \n\t ruby run_stoat_testing.rb --apps_dir /home/suting/proj/mobile_app_benchmarks/test_apps/ --apps_list /home/suting/proj/mobile_app_benchmarks/test_apps/apps_list.txt --avd_name testAVD_1 --avd_port 5554 --stoat_port 2000 --force_restart \n\n Outputs: \n \t<stoat_fsm_building_output>: the outputs of model construction. \n\t\t crashes/ -- crash report (include crash stack, event trace, screen shots); \n\t\t ui/ -- ui xml files; \n\t\t coverage/ -- coverage files during model construction; \n\t\t FSM.txt/app.gv -- xdot model graph; \n\t\t fsm_building_process.txt/fsm_states_edges.txt -- the model building process, mainly the increasing coverage/#states/#edges \n\t\t CONF.txt -- configuration file \n \t<stoat_mcmc_sampling_output>: the outputs of mcmc sampling. \n\t\t crashes/ -- crash report (include crash stack, event trace, screen shots); \n\t\t MCMC_coverage/ -- the coverage data during mcmc sampling; \n\t\t mcmc_sampling_progress.txt/mcmc_data.txt -- mcmc sampling progress data; \n\t\t initial_markov_model.txt/optimal_markov_model.txt/mcmc_models.txt -- the initial/optimal/all mcmc sampling models; \n\t\t mcmc_all_history_testsuites.txt -- all executed test suites for mcmc sampling; \n\t\t test_suite_to_execute.txt -- the current test suite under execution;\n\t\t CONF.txt -- configuration file. \n\t <coverage>: the all coverage data during two phases") do
    puts opts
    exit
  end
end.parse!

# app_dir should be set if a single app was specified
if !apk_path.eql?('') && File.exist?(apk_path)
  # set up emulator
  # create_avd(avd_name, avd_sdk_version) if force_to_create
  start_avd(avd_name, avd_serial, avd_port)
  prepare_avd(avd_serial)
  install_app(avd_serial, apk_path)

  # actual logic of the script
  construct_fsm(apk_path, avd_serial, stoat_port, stg_file,
                max_exploration_time, max_event_number, event_delay)

  # cleanup
  uninstall_app(avd_serial, apk_path)
else
  puts 'please specify the app to test, or check the app path'
  exit
end

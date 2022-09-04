# frozen_string_literal: true

require_relative 'bash_utils'

STOAT_DIR = File.dirname(__dir__)

# Creates an AVD with the given name. Will recreate if the AVD name is already taken
# @param [String] avd_name the name that the AVD will be registered under
# @param [String] sdk_version the SDK version to use to create the AVD
# @return [nil]
def create_avd(avd_name, sdk_version)
  # if avd_name already exists we delete the old one
  unless execute_shell_cmd("avdmanager list avd | grep #{avd_name}").strip.eql?('')
    execute_shell_cmd_output("avdmanager delete avd -n #{avd_name}")
    sleep 1 # wait a while
  end

  # the no is piped to input to answer 'Do you wish to create a custom hardware profile?'
  execute_shell_cmd_output("echo no | avdmanager create avd --name #{avd_name} --package 'system-images;#{sdk_version};google_apis;x86' --abi google_apis/x86 --sdcard 512M --device 'Nexus 7'")
  sleep 2
  nil
end

# Start the AVD with the given name. Will restart if the AVD is already running
# @param [String] avd_name the name of the AVD to start
# @param [String] avd_port the port to start the emulator on
# @return [nil]
def start_avd(avd_name, avd_serial, avd_port)
  kill_avd(avd_port) unless execute_shell_cmd('ps | grep qemu-system').strip.eql?('')
  execute_shell_cmd('adb start-server')
  sleep 1

  puts "Starting emulator #{avd_name}"
  # start the emulator, http://stackoverflow.com/questions/2504445/spawn-a-background-process-in-ruby
  # start the emulator with skin
  job1 = fork do
    # -swipe-data: clean up the emulator
    emulator = execute_shell_cmd('which emulator').strip
    # puts "#{emulator} -avd #{avd_name} -port #{avd_port} -wipe-data -writable-system &"
    exec "#{emulator} -avd #{avd_name} -port #{avd_port} -no-window -wipe-data -writable-system -no-snapshot &"
    #exec "#{emulator} -avd #{avd_name} -port #{avd_port} -gpu guest -wipe-data -writable-system -no-snapshot &"
  end
  Process.detach(job1)

  puts 'spawn the emulator, wait for it to boot ...'
  sleep 30
  wait_avd(avd_serial)
  nil
end

# Blocks until the an emulator has been launched under the avd_serial
# @param [String] avd_serial the serial that the running AVD is under
# @return [nil]
def wait_avd(avd_serial)
  Dir.chdir(STOAT_DIR + '/bin/') do
    execute_shell_cmd_output("./waitForEmu.sh #{avd_serial}")
  end
  nil
end

# Prepares the AVD by pushing data files into the sdcard
# @param [String] avd_serial the serial that the running AVD is under
# @return [String] the message from running the command
def prepare_avd(avd_serial)
  Dir.chdir(STOAT_DIR + '/bin/') do
    execute_shell_cmd_output("./setupEmu.sh #{avd_serial}")
  end

  puts 'naturalize the screen to avoid side-effect from previous testing...'
  execute_shell_cmd("python3 #{STOAT_DIR}/a3e/bin/events/rotation_natural.py #{avd_serial}")
end

# Stops any emulator running on the provided port
# @param [String] avd_port the port of the running emulator to kill
# @return [String] the output of running the stop emulator command
def kill_avd(avd_serial)
  execute_shell_cmd("adb -s #{avd_serial} emu kill")
end

require_relative 'bash'
require_relative 'log'

# Gets the name of the activity currently in the foreground of the emulator
# @param emu_serial the serial name of the emulator
# @return the full activity class name
def get_activity_name(emu_serial)
  res = Bash.run("adb -s #{emu_serial} shell dumpsys activity activities | grep -m 1 mFocusedApp")
  raise RuntimeError.new("Activity could not be determined") if res.empty?

  # parsing the activity info
  res = res.strip.split(" ")
  activity = res[-2].tr("/", "")
  Log.print("Activity found to be: #{activity}")

  activity
end

# Gets the name of the current apk package running in the emulator
# @param emu_serial the serial name of the emulator
# @return the full package name
def get_package_name(emu_serial)
  res = Bash.run("adb -s #{emu_serial} shell dumpsys activity activities | grep -m 1 mCurrentFocus")
  raise RuntimeError.new("Package could not be determined") if res.empty?

  # parsing the package info
  res = res.strip.split(" ")
  res = res[-1]
  res = res.split("/")
  package = res[0]
  Log.print("Package found to be: #{package}")

  package
end

# Gets the resource name (i.e. app:id/android_name) of the widget with the given resource id shown on the emulator
# @param res_id the resource id as an integer number
# @return the resource name
def get_res_name(emu_serial, res_id)
  res = Bash.run("adb -s #{emu_serial} shell dumpsys activity top | grep -m 1 '##{res_id.to_i.to_s(16)}'")
  return nil if res.empty?

  # parsing the resource name
  res = res.strip.split(" ")
  res_name = res[-1].tr("}", "").strip

  # replace the prefix with the proper one
  if res_name.start_with?("app")
    pkg = get_package_name(emu_serial)
    res_name = res_name.sub("app", pkg)
  end

  Log.print("Found ResName=#{res_name} for ResId=#{res_id}")
  res_name
end

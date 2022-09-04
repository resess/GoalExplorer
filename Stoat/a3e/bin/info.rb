require_relative 'bash'

def get_activity_and_package_name(emu_serial)
  #res = Bash.run("adb -s #{emu_serial} shell dumpsys activity top | grep -m 1 ACTIVITY")
  #output= mFocusedApp=ActivityRecord{e04fc4a u0 bloodpressure.bloodpressureapp.bloodpressuretracker/com.google.android.gms.ads.AdActivity t93}
  res = Bash.run("adb -s #{emu_serial} shell dumpsys activity activities | grep -E \"mFocusedApp|mFocusedActivity\" -m 1 ")
  if res.nil? || res.empty? #attempt twice
    res =  Bash.run("adb -s #{emu_serial} shell dumpsys activity top | grep -m 1 ACTIVITY")
    Log.print("Activity/package could not be determined")
    Log.print("Returned by adb #{res}")
  end
  #here try to restart the tool ?
  STDERR.puts "Activity/package could not be determined"  if res.empty?
  raise RuntimeError.new("Activity could not be determined") if res.empty?

  # parsing the activity?package info
  #output: mFocusedActivity: ActivityRcord{b3fff u0 com.package/activity.name}
  #output: ACTIVITY com.package/activity.name 
  res = res.strip.split(" ")
  # activity = res[-2].tr("/", "")
  if res[0].eql?("ACTIVITY")
    res = res[1] 
  else 
    res = res[-2]#-2 for mFocusedActivity
  end
  res = res.split("/")
  package = res[0]
  activity = (res[1][0] == '.' ? res[0] : "") + res[1] 
  Log.print("Activity found to be: #{activity}")
  Log.print("Package found to be: #{package}")
  
  [activity, package]
end

# Gets the name of the activity currently in the foreground of the emulator
# @param emu_serial the serial name of the emulator
# @return the full activity class name
def get_activity_name(emu_serial)
  # res = Bash.run("adb -s #{emu_serial} shell dumpsys activity activities | grep -m 1 mFocusedApp")
  # keeps on crashing, maybe use activity top | grep -m ACTIVITY instead ?
  #res = Bash.run("adb -s #{emu_serial} shell dumpsys activity activities | grep -m 1 mFocusedActivity")
  res = Bash.run("adb -s #{emu_serial} shell dumpsys activity top | grep -m 1 ACTIVITY")
  if res.empty? #attempt twice
    res =  Bash.run("adb -s #{emu_serial} shell dumpsys activity top | grep -m 1 ACTIVITY")
    Log.print("Activity could not be determined")
    Log.print("Returned by adb #{res}")
  end
  #here try to restart the tool ?
  STDERR.puts "Activity could not be determined"  if res.empty?
  raise RuntimeError.new("Activity could not be determined") if res.empty?

  # parsing the activity info
  #output: mFocusedActivity: ActivityRcord{b3fff u0 com.package/activity.name}
  #output: ACTIVITY com.package/activity.name 
  res = res.strip.split(" ")
  # activity = res[-2].tr("/", "")
  res = res[1] #-2 for mFocusedActivity
  res = res.split("/")
  activity = (res[1][0] == '.' ? res[0] : "") + res[1] 
  Log.print("Activity found to be: #{activity}")

  activity
end
# Gets the name of the current apk package running in the emulator
# @param emu_serial the serial name of the emulator
# @return the full package name
def get_package_name(emu_serial)
  # res = Bash.run("adb -s #{emu_serial} shell dumpsys activity activities | grep -m 1 mCurrentFocus")
  #res = Bash.run("adb -s #{emu_serial} shell dumpsys activity activities | grep -m 1 mFocusedActivity")
  res = Bash.run("adb -s #{emu_serial} shell dumpsys activity top | grep -m 1 ACTIVITY")
  if res.empty?
    res =  Bash.run("adb -s #{emu_serial} shell dumpsys activity top | grep -m 1 ACTIVITY")
    Log.print("Package could not be determined")
    Log.print("Returned by adb #{res}")
  end


  STDERR.puts "Package could not be determined"  if res.empty?
  raise RuntimeError.new("Package could not be determined") if res.empty?

  Log.print("Returned by adb #{res}")
  res if res.empty?

  #e.g ACTIVITY com.google.android.apps.nexuslauncher/.NexusLauncherActivity 5f18adb pid=2469
  # parsing the package info
  res = res.strip.split(" ")
  # res = res[-1]
  res = res[1] #-2 for mFocusedActivity
  res = res.split("/")
  package = res[0]
  #TODO: double check this
  #if res[1][0] != '.' 
  #  index = package.rindex('.')
  #  if index!=nil and index > 0
  #    package = res[1][0 ..index]
  #  end
  #end
  Log.print("Package found to be: #{package}")

  package
end

#Gets the fragments of the current screen (visibility not accounted for yet)
#@param emu_serial the the serial name of the emulator

def get_active_fragments(emu_serial)
  res = Bash.run("adb -s #{emu_serial} shell dumpsys activity top | awk '/Active Fragments/ ,/Added Fragments/' | grep -E \"^ *#.*:")
  if res.empty?
    Log.print("No fragments identified for screen")
    STDERR.puts "No fragments identified for screen"
    return []
  end

  fragments = res.split("\n").reject {|line| line.contains("android.arch.lifecycle")}.map {|line| line[/:(.*?){/m, 1]}
  Log.print("The collected fragments #{fragments}")
  fragments
end

# Gets the resource name (i.e. app:id/android_name) of the widget with the given resource id shown on the emulator
# @param res_id the resource id as an integer number
# @return the resource name
def get_res_name(emu_serial, res_id)
  res = Bash.run("adb -s #{emu_serial} shell dumpsys activity top | grep -m 1 '##{res_id.to_i.to_s(16)}'") #don't think the # should be there
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

#need a function to get the ui hierarchy desired

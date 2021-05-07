require_relative 'bash'
require_relative 'python'

# dump the current ui hierarchy of emulator
# @param emu_serial serial of the emulator to dump hierarchy of
# @param file_path path to dump to
def dump_ui(emu_serial, file_path)
  Python.dump_verbose(emu_serial, file_path)
  Log.print "UI hierarchy dumped to #{file_path}"
end

# dump a screenshot of the emulator
# @param emu_serial serial of the emulator to dump hierarchy of
# @param file_path path to dump to
def dump_screenshot(emu_serial, file_path)
  Bash.run("timeout 5s adb -s #{emu_serial} shell screencap -p /sdcard/stoat_screen.png")
  Bash.run("adb -s #{emu_serial} pull /sdcard/stoat_screen.png #{file_path}")
  Bash.run("adb -s #{emu_serial} shell rm /sdcard/stoat_screen.png")

  Log.print "Screenshot of current screen saved to #{file_path}"
end

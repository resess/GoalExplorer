require_relative '../bin/dump'

dump_ui('emulator-5554', "#{__dir__}/output/ui.xml")
dump_screenshot('emulator-5554', "#{__dir__}/output/ss.png")
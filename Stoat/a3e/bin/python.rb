# Wrapper for all the python scripts
module Python
  def Python.dump_verbose(emu_serial, file_path)
    Bash.run("python3 #{__dir__}/events/dump_verbose.py #{emu_serial} #{file_path}")
  end

  def Python.click_by_resource_id(emu_serial, res_id)
    Bash.run("python3 #{__dir__}/events/click_by_resource_id.py #{emu_serial} #{res_id}")
  end
end

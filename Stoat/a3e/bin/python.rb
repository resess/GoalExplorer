# Wrapper for all the python scripts
module Python
  #to do all all calls to python to this script
  def Python.dump_verbose(emu_serial, file_path)
    Bash.run("python3 #{__dir__}/events/dump_verbose.py #{emu_serial} #{file_path}")
  end

  def Python.get_cmd(type)
    (!type.nil? && (type.start_with?("boolean onLongClick") || type.start_with?("onClicklong")))? "long_click" : "click"
  end

  def Python.click_by_resource_id(emu_serial, res_id, type="")
    cmd = get_cmd(type)
    cmd_out = Bash.run("python3 #{__dir__}/events/#{cmd}_by_resource_id.py #{emu_serial} #{res_id}")
   #Log.print "#{cmd_out}"
    if (!cmd_out.nil? && cmd_out.include?("Could not find instance"))
      false
    else
      true
    end
  end

  

  def Python.click_by_content_desc(emu_serial, res_content_desc, type="")
    cmd = get_cmd(type)
    cmd_out = Bash.run("python3 #{__dir__}/events/#{cmd}_by_content_desc.py #{emu_serial} '#{res_content_desc}'")
    Log.print "#{cmd_out}"
    if (!cmd_out.nil? && cmd_out.include?("Could not find instance"))
      false
    else 
      true
    end
  end

  def Python.click_by_class_name(emu_serial, res_class_name, type="")
    cmd = get_cmd(type)
    cmd_out = Bash.run("python3 #{__dir__}/events/#{cmd}_by_class_name_instance.py #{emu_serial} '#{res_class_name}'")
    Log.print "#{cmd_out}"
    if (!cmd_out.nil? && cmd_out.include?("Could not find instance"))
      false
    else 
      true
    end
  end

  def Python.click_by_text(emu_serial, res_text, type="")
    cmd = get_cmd(type)
    cmd_out = Bash.run("python3 #{__dir__}/events/#{cmd}_by_text.py #{emu_serial} '#{res_text}'")
    Log.print "#{cmd_out}"
    if (!cmd_out.nil? && cmd_out.include?("Could not find instance"))
      #attempt with upper case text (TODO update stg)
      res_text_uppercase = res_text.upcase
      Log.print "Attempting with uppercase"
      cmd_out = Bash.run("python3 #{__dir__}/events/#{cmd}_by_text.py #{emu_serial} '#{res_text_uppercase}'")
      if (!cmd_out.nil? && cmd_out.include?("Could not find instance"))
        false
      else
        true
      end
    else 
      true
    end
  end

  def Python.click_by_child_text(emu_serial, parent_class, res_text)
    cmd_out = Bash.run("python3 #{__dir__}/events/click_by_child_text.py #{emu_serial} '#{parent_class}' '#{res_text}'")
    Log.print "#{cmd_out}"
    if (!cmd_out.nil? && cmd_out.include?("Could not find instance"))
      false
    else 
      true
    end
  end

  def Python.click_by_parent_info(emu_serial, parent_id, parent_class, res_text, type="")
    cmd = get_cmd(type)
    cmd_out = Bash.run("python3 #{__dir__}/events/#{cmd}_by_parent_info.py #{emu_serial} '#{parent_id}' '#{parent_class}' '#{res_text}'")
    Log.print "#{cmd_out}"
    if (!cmd_out.nil? && cmd_out.include?("Could not find instance"))
      false
    else 
      true
    end
  end

end

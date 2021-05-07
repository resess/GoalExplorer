module UTIL
  require 'fileutils'
  require 'nokogiri'

  def UTIL.compare(file_name1, file_name2)
    is_same=FileUtils.compare_file(file_name1, file_name2);
  end

  def UTIL.extract_num(file_name)
    num=IO.readlines(file_name)
  end

  def UTIL.compare_output(file1, file2, file3)
    #add by ting
    file1_arr = IO.readlines(file1)
    file2_arr = IO.readlines(file2)
    print '[A3E] file name: ' + file1 + "\n"
    i = 0
    while i < file1_arr.size do
      print file1_arr[i] + "\n"
      i += 1
    end
    print '[A3E] file name: ' + file2 + "\n"
    i = 0
    while i < file2_arr.size do
      print file2_arr[i] + "\n"
      i += 1
    end
    f1 = IO.readlines(file1).map(&:chomp)
    f2 = IO.readlines(file2).map(&:chomp)
    f3= (f1-f2)
    print "*******\n"
    print f3
    print "\n*******\n"
    File.open(file3,'w'){ |f| f.write((f1-f2).join("\n")) }
  end

  def UTIL.read_file_as_string(filename)
    data = ''
    f = File.open(filename, 'r')
    f.each_line do |line|
      data += line
    end
    data
  end

# execute shell command
  def UTIL.execute_shell_cmd(cmd)
    puts "$ #{cmd}"
    begin
      `#{cmd}`
    rescue Errno::EPIPE
      puts 'Connection error. Retry...'
    end
  end

  def UTIL.execute_shell_cmd_with_output(cmd)
    puts "$ #{cmd}"
    IO.popen(cmd).each do |line|  # outputs the running info
      puts line
    end
  end

  def UTIL.get_package_name(apk)
    execute_shell_cmd("aapt dump badging #{apk} | grep package | awk '{print $2}' | sed s/name=//g | sed s/\\'//g").strip()
  end

  def UTIL.get_app_version(apk)
    execute_shell_cmd("aapt dump badging #{apk} | grep package | awk '{print $4}' | sed s/versionName=//g | sed s/\\'//g").strip()
  end

# check whether we need input login info
# def UTIL.need_login(avd_serial, screen_layout_file, app_name, username_list, password_list)
# 	# iterate through username array and enter the username if we found a match
# 	username_list.each do |id|
# 		if id.start_with?("#") then # if "#" is added before a widget id, this widget will be omitted
# 			next
# 		end
# 		res = execute_shell_cmd("grep #{id} #{screen_layout_file}").strip()
# 		if (not res.eql?("")) then
# 			puts "Username Widget: username required..."
# 			UTIL.login(avd_serial, id, true)
# 		end
# 	end
#
# 	# iterate through password array and enter the password if we found a match
# 	password_list.each do |id|
# 		if id.start_with?("#") then # if "#" is added before a widget id, this widget will be omitted
# 			next
# 		end
# 		res = execute_shell_cmd("grep #{id} #{screen_layout_file}").strip()
# 		if (not res.eql?("")) then
# 			puts "Password Widget: password required..."
# 			UTIL.login(avd_serial, id, false)
# 		end
# 	end
# end

  # naive method
  def UTIL.get_login_info(screen_layout_file, actions)

    # read the screen xml file
    if File.exist?(screen_layout_file)
      xml_data = Nokogiri::XML(File.open(screen_layout_file))

      widget_hash = {}

      widgets = xml_data.xpath('//node')
      widgets.each do |node|
        id = node.attr('resource-id').to_s
        text = node.attr('text').to_s
        if id =~(/(user|account|client|phone|card)[\s\_\-]*(name|id|number|#)/im) || id =~(/(log|sign)[\s\_\-]*in[\s\_\-]*(name|id|)/im) || id =~(/[e]mail/im)
          action = find_action_by_text_resid(actions, text, id)
          unless action.nil?
            if get_action_type(action).eql? 'edit'
              Log.print 'Username widget detected...'
              widget_hash[:username] = [get_action_text(action), id]
            elsif get_action_type(action).eql? 'click'
              Log.print 'Login button detected...'
              widget_hash[:login] = [get_action_text(action), id]
            end
            # UTIL.login(avd_serial, id, true)
          end
        end

        if id =~ /(pass|pin)[\s\_\-]*(word|code|)/im
          Log.print 'Password widget detected...'
          action = find_action_by_text_resid(actions, text, id)
          unless action.nil?
            if get_action_type(action).eql? 'edit'
              widget_hash[:password] = [get_action_text(action), id]
              # UTIL.login(avd_serial, id, false)
            end
          end
        end
      end
      widget_hash
    else
      {}
    end
  end

  def UTIL.login(avd_serial, id, usr_name)
    puts 'Enter login info ...'
    if usr_name
      execute_shell_cmd("gtimeout 2s python ./bin/events/edit_by_resource_id_with_specified_content.py #{avd_serial} #{id} 'recessubc@gmail.com'")
    else
      execute_shell_cmd("gtimeout 2s python ./bin/events/edit_by_resource_id_with_specified_content.py #{avd_serial} #{id} 'Kaiser4095'")
    end
    sleep 2
    # execute_shell_cmd("gtimeout 2s python ./bin/events/edit_by_resource_id_with_specified_content.py #{avd_serial} 'com.fsck.k9:id/account_password' 'Kaiser4095'")
    # sleep 2
    # execute_shell_cmd("gtimeout 2s python ./bin/events/click_by_resource_id.py #{avd_serial} 'com.fsck.k9:id/next'")
    # sleep 5
  end

  def UTIL.get_action_type(action)
    _, action_cmd = parseActionString(action)
    if action_cmd.include?('edit(')
      return 'edit'
    elsif action_cmd.include?('click(')
      return 'click'
    end
    'other'
  end

  def UTIL.get_action_text(action)
    _, _, _, view_text = parseActionString(action)
    view_text = view_text.gsub!(/\A"|"\Z/, '')
    if view_text.nil?
      return nil
    else
      return view_text.rstrip
    end
  end

  def UTIL.get_action_resid(action)
    _, action_cmd = parseActionString(action)
    if action_cmd.include?('(resource-id=')
      first_quote_index = action_cmd.index("\'")  # get the first occurrence of '
      last_quote_index = action_cmd.rindex("\'")  # get the last occurrence of '
      # Note we should include the quotes to avoid the existence of whitespaces in the action_param_value
      return action_cmd[first_quote_index + 1..last_quote_index - 1]
    end
    nil
  end

  def UTIL.find_action_by_text_resid(actions, text, id)
    actions.each do |action|
      view_text = get_action_text(action)
      res_id = get_action_resid(action)
      unless view_text.nil?
        return action if !view_text.empty? && view_text.eql?(text)
      end
      unless res_id.nil?
        return action if !res_id.empty? && res_id.eql?(id)
      end
    end
    nil
  end

  def UTIL.change_cmd_to_resid_text(action, action_cmd)
    _, _, _, view_text = parseActionString(action)
    if action_cmd.include?('className=') && action_cmd.include?('instance=')
      if !view_text.nil? && !view_text.empty?
        first_quote_index = action_cmd.index('(')  # get the first occurrence of (
        last_quote_index = action_cmd.index(')')  # get the first occurrence of )
        view_text = view_text.gsub!(/\A"|"\Z/, '').rstrip
        if view_text != '' && !view_text.nil? && !view_text.empty?
          action_cmd[first_quote_index..last_quote_index] = "(text='#{view_text}')"
        end
      end
    end
    action_cmd
  end

end


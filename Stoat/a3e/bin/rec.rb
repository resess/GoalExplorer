#! /usr/bin/env ruby

## Copyright (c) 2011-2012,
##  Jinseong Jeon <jsjeon@cs.umd.edu>
##  Tanzirul Azim <mazim002@cs.ucr.edu>
##  Jeff Foster   <jfoster@cs.umd.edu>
## All rights reserved.

## Copyright (c) 2015-2017,
## Ting Su <tsuletgo@gmail.com>
## All rights reserved.

##
## Redistribution and use in source and binary forms, with or without
## modification, are permitted provided that the following conditions are met:
##
## 1. Redistributions of source code must retain the above copyright notice,
## this list of conditions and the following disclaimer.
##
## 2. Redistributions in binary form must reproduce the above copyright notice,
## this list of conditions and the following disclaimer in the documentation
## and/or other materials provided with the distribution.
##
## 3. The names of the contributors may not be used to endorse or promote
## products derived from this software without specific prior written
## permission.
##
## THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
## AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
## IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
## ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
## LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
## CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
## SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
## INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
## CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
## ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
## POSSIBILITY OF SUCH DAMAGE.

require 'io/console'
require 'rubygems'
require 'optparse'
require 'socket'
require 'time'
require 'logger'

REC = File.dirname(__FILE__)
PARENT = File.expand_path(File.dirname(__FILE__)) + '/../temp'

require_relative 'avd'
require_relative 'adb'
require_relative 'aapt'
require_relative 'uid'
require_relative 'act'
require_relative 'util'
require_relative 'picker'
require_relative 'conf'
require_relative 'coverage'
require_relative 'log'
require_relative 'util'
require_relative 'crash_reporter'
require_relative 'stg'
require_relative 'info'
require_relative 'dump'

#include Commands

# strip away debug info to get the activity name
def parse_act_names act_name
  last_act = ''
   act_name.each_line do |line|
     print 'I: [A3E] parse activity name: ' + line + "\n"
      last_act = line.split(/:/).last.strip!
      print 'I: [A3E] the activity name: ' + last_act + "\n"
   end
   last_act = last_act
end

# modification to stoat, we no longer cap iterations and take screenshots every iteration
# due to the length of time that it may run (several hours)
$default_A3E_iteration = 0
$default_iteration_per_target = 0

# instead we allow for one hour of exploration, resetting whenever a new target has been reached
$last_target_found_time = Time.now.to_i #maybe we should set it somewhere else? like at the beginning of domainjob

##################################################################

##### MCMC-Droid #####

# notify the server
def notify_server(ui_file_name, package_name, activity_name)
  client_session = TCPSocket.new('localhost', $g_port)
  Log.print 'Starting connection with the server'

  Log.print '------[Info to notify the server]------'
  Log.print "Ui file name: #{ui_file_name}"
  Log.print "Package name: #{package_name}"
  Log.print "Activity name: #{activity_name}"
  Log.print '---------------------------------------'

  # send the app state to the server
  client_session.puts ui_file_name
  client_session.puts 'UI_FILE_NAME_EOM' # end of message

  client_session.puts package_name
  client_session.puts 'PACKAGE_NAME_EOM' # end of message

  client_session.puts activity_name
  client_session.puts 'ACTIVITY_NAME_EOM' # end of message

  # send the executed action id
  client_session.puts $executed_action_list.last
  client_session.puts 'ACTION_EOM' # end of message
  client_session.puts 'FINISH_EOM' # end of client message

  Log.print 'Notify server succeed!'

  # the fire-able actions from the current app state
  fireable_actions = ''

  while !client_session.closed? && (server_message = client_session.gets)
    # if one of the messages contains 'Goodbye' we'll disconnect
    ## we disconnect by 'closing' the session.
    if server_message.include?('Goodbye')
      Log.print 'The [A3E] client received the *Goodbye* message from the server'
      Log.print 'Closing connection with Stoat server'

      client_session.close

      # write into the command file
      file_dir = "#{$myConf.get_fsm_building_dir}/#{activity_name}_command.txt"
      Log.print "Storing all the received *fireable actions* into #{file_dir}"

      File.open(file_dir, 'w') do |f|
        f.puts fireable_actions
      end

      Log.print '---------------Actions-----------------'
      system "cat #{file_dir}"
      Log.print '---------------------------------------'
    else
      # accept the message from server
      Log.print "Received the fire-able actions #{server_message} from the server"
      fireable_actions += server_message
    end
  end
end

# report crawler state
def report_crawler_state (crawler_state, activity)
  Log.print 'Starting to report crawler state'
  # try to connect with the server
  while true
    begin
      # create the socket connection
      clientSession = TCPSocket.new('localhost', $g_port)
      unless clientSession.eql?(nil)
        Log.print '[A3E] I: the connection is set up. '
        break
      end
      rescue
        Log.print "[A3E] is waiting the server connection at port #{$g_port}... "
        sleep(5)
    end
  end

  if crawler_state.eql?('READY')
    clientSession.puts crawler_state
    clientSession.puts 'AVD_STATE_EOM' # end of message
    clientSession.puts activity
    clientSession.puts 'ENTRY_ACTIVITY_EOM'
  elsif crawler_state.eql?('STOP')
    clientSession.puts crawler_state
    clientSession.puts 'AVD_STATE_EOM' # end of message
  end

  while !clientSession.closed? &&
      # "gets" gets a line at one time
        (serverMessage = clientSession.gets)
    ## lets output our server messages
    Log.print serverMessage

    #if one of the messages contains 'Goodbye' we'll disconnect
    ## we disconnect by 'closing' the session.
    if serverMessage.include?('Goodbye')
      Log.print 'log: closing the connection with the server'
      clientSession.close
    end
  end
end


#######
# cleanup
def cleanup_adb_services
  # clean up adb in the localhost only for the target device
  puts "Cleaning up the adb running services"
  logcat_pids = `ps -ux | grep "adb -s #{$emulator_serial} logcat" | awk '{print $2}'`
  logcat_pids_list = logcat_pids.gsub("\n", " ")
  puts "#{logcat_pids_list}"
  kill_adb_cmd = "kill -9 #{logcat_pids_list}"  # kill the adb logcat process
  puts "$ #{kill_adb_cmd}"
  `#{kill_adb_cmd}`
end


#########
# global vars
$background_job = nil
# record the executed action ids
$executed_action_list = []
# the action picker
$picker = ActionPicker.new()
# the coverage monitor
$coverager = Coverage.new()
# the type of screen matching
$deep_screen_matching = false #should be provided as an external input
# the maximum number of UI events to be executed
$g_maximum_events = 0
# the maximum line coverage
$g_maximum_line_coverage = 0
# the number of executed events when reaching the maximum line coverage
$g_maximum_line_coverage_events = 0
# the coverage txt file name
$g_coverage_txt = ''
#########
#
$recovery_keyevent_back = 0
$emulator_name = ''
$emulator_serial = ''

# $timeout_cmd = 'gtimeout'
$timeout_cmd = 'timeout'
$login_success = false
$auto_login = false
$naive_login = false

$login_attempts = 0

# dump the executed $action into the $activity's history command list
def dump_executed_actions (activity, action)
  unless activity.nil? || activity.empty? 
    open($myConf.get_fsm_building_dir + '/' + activity + '_command_history.txt', 'a') do |f| 
      f.puts action
    end
  end

  # dump the whole action execution history
  open($myConf.get_fsm_building_dir + '/' + 'all_action_execution_history.txt', 'a') do |f|
    f.puts action
  end
end

def get_ui_dump_path
  "#{$myConf.get_ui_files_dir}/S_#{$default_A3E_iteration}.xml"
end


#NOTES
# Open drawer, close drawer enough to distinguish (if content desc set), otherwise need to store items
#More options when menu is closed, when open, nothing just the items

#For now, we can do, if the current screen has "More options" (default for options menu), then the menu is closed, otherwise the menu is open
#For drawer, we should probably add the content desc to the information
#For now, check if there's open or close in the name

def get_current_base_screen(stg)
  activity,_ = get_activity_and_package_name($emulator_serial)
  #get base screen with node
  screen_nodes = stg.contains_node_with_name(activity) ? stg.base_node_by_name(activity) : nil
  return nil if screen_nodes.nil? || screen_nodes.empty?

  if screen_nodes.length() == 1
    return screen_nodes[0]
  end

  #TODO returns all the possible matching screens and try them one by one I guess?
  Log.print "Multiple matching base screens #{screen_nodes}, shouldn't occur, picking the first one"
  screen_node = nil
  if !$deep_screen_matching
    screen_node = screen_nodes.select {|screen_node| (screen_node.dialogs.nil? || screen_node.dialogs.empty?)}[0]
    #screen_node = screen_nodes.sort_by(:&.fragments.length)
  else
    stg.get_closest_match(screen_nodes, get_active_fragments($emulator_serial)) #todo: double check for dialogs
  end
   if screen_node.nil?
     screen_nodes[0]
   else
     screen_node #todo: same for tabs
   end
end


def get_current_screen_node(stg)
  activity,_ = get_activity_and_package_name($emulator_serial)
  screen_nodes = stg.contains_node_with_name(activity)? stg.nodes_by_name(activity) : nil
  return nil if screen_nodes.nil? || screen_nodes.empty?

  #the distinction is only necessary for the starting screen ?
  #or at least for the screen from which paths are computed

  if screen_nodes.length() == 1
    return screen_nodes[0]
  end

  screen_node = nil
  Log.print "Multiple matching screens #{screen_nodes}, dumping UI hierarchy"
  ui_file_path = get_ui_dump_path
  dump_ui($emulator_serial, ui_file_path)
  
  entities = UTIL.get_menu_drawer_info(ui_file_path)
  found_open_menu = nil
  found_open_drawer = false
  unless entities.nil? || entities.empty?
    #for menu, there's no indication that a menu is open, only closed
    #TODO: differentiate between open menu and no menu at all
    found_open_menu = entities[:menu].nil? ? nil : entities[:menu][0] #if menu icon was not found, menu is already open or there's no menu at all
    found_open_drawer = entities[:drawer].nil? ? false : entities[:drawer][0]
  end

  #TODO deal with context menus
  screen_nodes = screen_nodes.select {|node| (node.has_drawer() == found_open_drawer)}
  if screen_nodes.nil? || screen_nodes.empty?
    Log.print("Issue identifying current screen, mismatch #{screen_nodes}")
  elsif screen_nodes.length() == 1 #only one screen with no menu at all
    screen_node = screen_nodes[0]
  elsif screen_nodes.length() > 1 #screen with menu (one closed, one open) or something else
    #todo add tab and dialogs
    screen_nodes = screen_nodes.select {|node| (!found_open_menu.nil? && !found_open_menu && !node.has_menu()) || (node.has_menu() && found_open_menu.nil?)}
    if screen_nodes.nil?
      Log.print "No menu for this activity, selecting any"
      screen_node = screen_nodes[0]
    elsif screen_nodes.length() != 1
      Log.print("Issue identifying current screen, mismatch #{screen_nodes}")
      screen_node = stg.get_closest_match(screen_nodes, get_active_fragments($emulator_serial))
      Log.print("Issue identifying current screen, mismatch #{screen_nodes}")
    else
      screen_node = screen_nodes[0]
    end
  end
  screen_node
end

def update_found_target(stg, screen_node)
  #need to delete all targets by name
  action = stg.target_action(screen_node)
  if !action.nil?
    Log.print "Need to perform final action to reach target #{action}"
    execute_edge_action(action, $emulator_serial)
    #need a function to perform event (should be the same for random and edges?)
    #think about how to validate the action was acutally performed
  end
  screen_nodes_to_delete = []
  if $deep_screen_matching
    screen_nodes_to_delete << screen_node
  else
    screen_nodes_to_delete = stg.nodes_by_name(screen_node.name)#get all by name
  end
  screen_nodes_to_delete.map { |node| stg.delete_target(node, found=true) }
    #Log.print("Adding #{screen_node} to reached targets list")
  File.open("#{$myConf.get_fsm_building_dir}/reached_activity.txt", 'a') do |f|
    f.puts("#{screen_node.name} #{Time.now.to_i-$last_target_found_time}")
  end
end


def recover_package_under_test(current_package, package_name_under_test, force_recover=false)
  if !still_on_package_under_test(current_package, package_name_under_test, force_recover)
    if $recovery_keyevent_back == 0
      $recovery_keyevent_back = $recovery_keyevent_back + 1
      #press back button
      #   # construct the back cmd
      back_cmd = "python3 ./bin/events/back.py #{$emulator_serial}"
      puts "$ #{back_cmd}"
      `#{back_cmd}` # back two times to ensure we escape from the error location, oops...
      `#{back_cmd}`
    else
      reset_app($myConf.get_instrumented_apk)
      $recovery_keyevent_back = 0
    end
  else
    $recovery_keyevent_back = 0
  end
end


#todo some screen require waiting (for e.g google login stuff?)
def still_on_package_under_test(current_package, package_name_under_test, ignore_whitelist=false)
  whitelist = ["com.google.android.gms", "com.facebook"] #todo deal with permission screen
  #here check for auth api and same for fb
  if current_package == package_name_under_test
    $recovery_keyevent_back = 0
    true
  elsif current_package != package_name_under_test

    if !ignore_whitelist && whitelist.any? {|package| current_package.start_with?(package)}
      Log.print("Found whitelisted package #{current_package}")
      Log.print("Not recovering")
      true
    elsif $recovery_keyevent_back == 0
      # we lost the focus of the app ?
      Log.print 'Current package does not match the package under testing'
      Log.print "Current package: #{current_package}"
      Log.print "Package under testing: #{package_name_under_test}"
      false
    elsif $recovery_keyevent_back == 1
      # fail to recover the app by the "back" event?
      Log.print 'Failed to recover the app to testing state, reset the app'
      false
    end
  end
end
    

def record_activity_coverage(activity)
  # record the activity coverage
  Log.print "Recording activity coverage for #{activity}"
  open("#{$myConf.get_fsm_building_dir}/explored_activity_list.txt", 'a') do |f|
    f.puts activity
  end
end


# get executable UI events from the current UI after a previous event has been executed
def get_executable_ui_events(package_name_under_test, current_activity = nil, stg, tgt)
  Log.print 'Started dump of the the current UI hierarchy'
  ui_file_path = get_ui_dump_path
  dump_ui($emulator_serial, ui_file_path)

  #Here handle no dumped ui or empty ui file

  # # if screenshot is enabled, always dump the screenshot after the layout xml
  dump_screenshot if $g_enable_screenshot

  # check if the ui is empty ?

  # A workaround to handle the failure of dumping ui xml
  unless File.exist?(ui_file_path)

    # Unsure how often this actually happens
    raise RuntimeError.new("UI Dump failed: #{package_name_under_test}")

    # # if fail to dump the ui xml 
    # current_package = get_package_name($emulator_serial)
    # if current_package == package_name_under_test
    #   # if we are under the target package, execute "back" to get back from the view which we fail to dump ui xml
    #   # construct the back cmd
    #   back_cmd = "python ./bin/events/back.py #{$emulator_serial}"
    #   puts "$ #{back_cmd}"
    #   `#{back_cmd}` # back two times to ensure we escape from the error location, oops...
    #   `#{back_cmd}`
    #   ui_file_name = dump_ui_xml
    
    #   dump_screenshot if $g_enable_screenshot
    
    #   unless File.exist?(ui_file_name)
    #     Log.print '[E]: Failed to dump UI xml'
    #     Log.print '[E]: Shutting down'
    #     exit 0
    #   end
    # end
  end

  # get the current package name to make sure we haven't transitioned to a different apk
  current_activity,current_package = get_activity_and_package_name($emulator_serial)
  #to do: if permissions then need to interact with it I guess ?
  Log.print("The current package: <#{current_package}> and activity : <#{current_activity}")

  if still_on_package_under_test(current_package, package_name_under_test)
    $recovery_keyevent_back = 0
    record_activity_coverage(current_activity)
  elsif $recovery_keyevent_back == 0
    $recovery_keyevent_back = $recovery_keyevent_back + 1
    #if $recovery_keyevent_back == 2 then # TODO Note this allows the app under test to step forward 2 steps, uncomment it when you do not need it
    ui_file_path = '/EMPTY_APP_STATE.xml'; # return an empty state
    #  $recovery_keyevent_back = 3
    #end
  elsif $recovery_keyevent_back == 1
    ui_file_path = '/RESET_APP_STATE.xml'; # return a reset state
    $recovery_keyevent_back = 0
  end

  # upload the app state to the server
  Log.print '[A3E] I: uploading the app state to server'
  notify_server(ui_file_path, current_package, current_activity)
  activity_file = "#{$myConf.get_fsm_building_dir}/#{current_activity}_command.txt"
  if File.exist?(activity_file)
    actions = ACT.extract_act(activity_file)
  else
    Log.print '[A3E] I: Activity file generated from Stoat server not found?'
    exit 0
  end

  Log.print 'Checking if a login widgets is present at current screen'
  #here check if login_required (i.e not already logged in)
  login_widget_hash = UTIL.get_login_info(ui_file_path, actions, $myConf.get_login_type)

  # explicit return the currently executable events, the current focused package and activity
  [actions, current_package, current_activity, login_widget_hash]
end


def reset_app(apk)
  # Resetting the weights
  # Assume the emulator is fine, we need to re-start the app
  #puts '[A3E] I: clear app cache'
  #clear_app_cache(apk) #TO COMMENT OUT
  #UTIL.execute_shell_cmd("sleep #{$g_app_start_wait_time}")
  puts '[A3E] I: restart the app'
  start_app(apk)
  UTIL.execute_shell_cmd("sleep #{$g_app_start_wait_time}") # Note this time is set to ensure the app can enter into a stable state
end

def start_app(apk)
  pkg = $aapt.pkg apk
  act = $aapt.launcher apk #todo replace with launchers.first
  # Here we add an additional option "-S", so that we will force stop the target app before starting the activity.
  # Without "-S", we cannot restart the app, since the app is already there, e.g., we will get this message
  # "Activity not started, its current task has been brought to the front"
  if !act.nil?
    UTIL.execute_shell_cmd('adb -s ' + $emulator_serial + ' shell am start -S -n ' + pkg + '/' + act)
  else
    UTIL.execute_shell_cmd('adb -s ' + $emulator_serial + ' shell monkey -p ' + pkg + ' -c android.intent.category.LAUNCHER 1')
  end
end

def clear_app_cache(apk)
  pkg = $aapt.pkg apk
  UTIL.execute_shell_cmd('adb -s ' + $emulator_serial + ' shell pm clear ' + pkg)
end


# execute the event by parsing the cmd
def execute_event(action_cmd)
  action_type = ''
  action_param = ''
  edit_input_value = ''

  if action_cmd.eql?("reset\n")
    reset_app($myConf.get_instrumented_apk)
    return
  end

  if action_cmd.start_with?('adb')
    cmd = action_cmd.sub('adb', "adb -s #{$emulator_serial}")
    Log.print "$ #{cmd}"
    `#{cmd}`
    return
  end

  if action_cmd.eql?("menu\n") # "menu"
    # construct the python cmd
    menu_cmd = "python3 #{__dir__}/events/menu.py #{$emulator_serial}"
    Log.print "$ #{menu_cmd}"
    `#{menu_cmd}`
    return
  end

  if action_cmd.eql?("back\n") || action_cmd.eql?("keyevent_back\n") # "back"
    # construct the python cmd
    back_cmd = "python3 #{__dir__}/events/back.py #{$emulator_serial}"
    Log.print "$ #{back_cmd}"
    `#{back_cmd}`
    return
  end

  if action_cmd.include?('click(') # get the action type, "click", "long click"
    action_type = 'click'
  elsif action_cmd.include?('clickLong(')
    action_type = 'long_click'
  elsif action_cmd.include?('edit(')
    action_type = 'edit'
  elsif action_cmd.include?('scroll(')
    action_type = 'scroll'
  end

  if action_cmd.include?('(text=') # get the action param, "text", "content_desc", "resource_id"
    action_param = '_by_text'
  elsif action_cmd.include?('(content-desc=')
    action_param = '_by_content_desc'
  elsif action_cmd.include?('(resource-id=')
    action_param = '_by_resource_id'
  elsif action_cmd.include?('direction=')
    action_param = '_by_direction'
  elsif action_cmd.include?('className=') && action_cmd.include?('instance=')
    action_param = '_by_classname_instance'
  end

  if action_param.eql?('_by_classname_instance')
    # execute action by classname and instance
    class_name_pattr = /className=\'(.*)\',/
    class_name = (action_cmd.match class_name_pattr)[1]
    instance_pattr = /instance=\'(.*)\'/
    instance = (action_cmd.match instance_pattr)[1]
    Log.print "[D]: #{action_type}, #{action_param}, #{class_name} #{instance}"

    # construct the python cmd
    event_cmd = "#{$timeout_cmd} 60s python3 #{__dir__}/events/#{action_type}#{action_param}.py #{$emulator_serial} #{class_name} #{instance} >> /data/faridah/events.log"
    Log.print "$ #{event_cmd}"
    `#{event_cmd}`
  else
    # get the action param value
    first_quote_index = action_cmd.index("\'")  # get the first occurrence of '
    last_quote_index = action_cmd.rindex("\'")  # get the last occurrence of '
    # Note we should include the quotes to avoid the existence of whitespaces in the action_param_value
    action_param_value = action_cmd[first_quote_index..last_quote_index]
    Log.print "[D]: #{action_type}, #{action_param}, #{action_param_value}"

    # construct the python cmd
    event_cmd = "#{$timeout_cmd} 60s python3 #{__dir__}/events/#{action_type}#{action_param}.py #{$emulator_serial} #{action_param_value}"
    Log.print "$ #{event_cmd}"
    `#{event_cmd}`
  end
end



#todo refactor and reuse below

#4@click(resource-id='com.android.systemui:id/back'):android.widget.ImageView@""
def execute_edge_action(action, emu_serial)
  res_name = get_res_name(emu_serial, action.resource_id)
  content_desc = action.content_desc
  text = action.text
  ui_type = action.ui_type
  handler = action.handler
  parent_id = action.parent_id

  #want to 
  #get the handler --> map the handler to an action file

  if res_name.nil?
    if (!ui_type.nil? && ui_type.end_with?("item") && !ui_type.eql?("item"))
      #for list view, same with res_name
      #for spinner (for now), parent.click() then child.click
      #we should get the parent id if available
      Log.print("A list view item #{ui_type}")
      
      parent_res_name = get_res_name(emu_serial, parent_id)
      parent_class = "android.widget.ListView"
      parent_class = "android.widget.Spinner" if ui_type.eql?("spinneritem")
      parent_class = "android.support.v7.widget.RecyclerView" if ui_type.eql?("recyclerviewitem")
      if (!Python.click_by_parent_info(emu_serial, parent_res_name, parent_class, text))
        Log.print("Cannot find action matching edge with text #{text} for parent #{parent_res_name} ... moving on")
        # could not perform transition after max attempts
        #try anything I guess
        Log.print "Failed to perform transition\n #{action}"
        false
      else
        Log.print("Found and executed action by parent info #{parent_class} #{parent_res_name}")
        true
      end
    elsif (content_desc.nil? || content_desc.empty? || !Python.click_by_content_desc(emu_serial, content_desc, handler))
      Log.print("Cannot find action matching edge with content desc #{content_desc}")
      if (!ui_type.nil? && ui_type.eql?("menu"))
        if(!content_desc.nil? && content_desc.eql?("Menu"))
          content_desc = 'More options'
        else
          content_desc = 'Menu'
        end
        Log.print("Attempting with default content desc #{content_desc} for menu")
        if(!Python.click_by_content_desc(emu_serial, content_desc, handler))
          Log.print("Cannot find action matching edge with content desc #{content_desc}")
          false
        else
          Log.print("Found and executed action by content desc")
          true
        end
      elsif (text.nil? || text.empty? || !Python.click_by_text(emu_serial, text, handler))
      Log.print("Cannot find action matching edge with text #{text} ... moving on")
      # could not perform transition after max attempts
      Log.print "Failed to perform action\n #{action}"
      false
      else
        Log.print("Found and executed action by text")
        #edge.to.name == cur_activity
        true
      end
    else
      Log.print("Found and executed action by content desc")
      #edge.to.name == cur_activity
      true
    end
  else
    Log.print("Actuating #{res_name}")
    if(!ui_type.nil? && (ui_type.eql?("listview") || ui_type.eql?("ListView"))) #we want to perform an action on the first child of the list view
      Log.print("A list view #{ui_type}")
    end
    if Python.click_by_resource_id(emu_serial, res_name, handler) #should check if succeeded?
      Log.print("Found and executed action by res_id #{res_name}")
      #edge.to.name == cur_activity
      true
    else
      Log.print "Failed to perform action\n #{action}"
      false
    end
  end
end


#todo -- the path from the main screen to the target requires opening a menu
#there should be no transition from a screen with closed menu to the target since, there can only be a menu item click (onOptionsItemsELECTED) IN THE STG
#if the menu is already open so MainActivity --> ScreenA(closedMenu) --> ScreenA(openMENU) --onOptionsItemSelected->Target 
#so what we need is, if we see onIconClick as the transition, if we can't find the resId, we need to try with the default com.android.ui/id:menu instead
#and we only need precise screen matching after a random action, to figure out where we are?

#how would the precise matching work? technically it's more about the available actions then the screen descriptioN
#we could get all the next transiitons from the screen with the same name (on the path to the target) or just the current path, then check if the available actions are on the edge
#TO THINK about


def do_main_job(package_name_under_test, stg)
  # pick a target
  Log.print("All targets #{stg.targets.length}")
  tgt = stg.get_next_target
    
  # if no more targets we still continue with exploration until the time is hit
  if tgt.nil?
    # Log.print("No targets: continuing with random exploration")
    # perform_random_action(activity, actions, login_widget_hash)
    Log.print("All targets have been reached, exiting")
    #$default_iteration_per_target = 0 #measure time per target instead
    return
  end

  Log.print("The current target #{tgt}")


  activity,package = get_activity_and_package_name($emulator_serial)

  # print the screen details
  Log.print("The current starting activity: <#{activity}>")

  #here need to check if there is a login before recovering the app package under test

  #ensure are still within the right app
  recover_package_under_test(package, package_name_under_test, false) 

   #record activity coverage
  record_activity_coverage(activity)

 # map the current screen to a ScreenNode if present
  screen_node = get_current_base_screen(stg)#get_current_screen_node(stg)
  #screen_node = stg.contains_node(activity) ? stg.node(activity) : nil

  #reset values
  $login_attempts = 0

  if $default_iteration_per_target >= $g_maximum_events.to_i
    puts "[D] we have reached the maximum #{$g_maximum_events} UI events for the current target #{tgt}"
    puts "[D] moving on to next target"

    stg.delete_target(tgt, found=false) #should I delete all versions of this target?
    $last_target_found_time = Time.now.to_i
    $default_iteration_per_target = 0
    $picker.resetActionsWeight

    #stg.reset_all_paths(paths_to_target)
    reset_app($myConf.get_instrumented_apk)
    return
  end
  

  # need to check for targets while we run the path and only reset at the end of the path
  unless screen_node.nil?
    Log.print("ScreenNode in STG found  #{screen_node}")
    Log.print "Edges: #{stg.edges(screen_node).length}"
    #stg.edges(screen_node).each { |edge| Log.print("#{edge.from}->#{edge.to}:#{edge.action.resource_id}") }
    if stg.is_target(screen_node)
      Log.print("Target node has been found #{screen_node}")

      update_found_target(stg, screen_node)
      $last_target_found_time = Time.now.to_i
      $picker.resetActionsWeight
      reset_app($myConf.get_instrumented_apk)

      return
    end
  end

  if screen_node.nil?
    Log.print("FAILURE: Cannot find screen_node for #{activity}") #why do this if you only have on possible action from the server ?
    #we can try to reset the app maybe?
    #perform_random_action(activity, actions, login_widget_hash)
    #$default_iteration_per_target = $default_iteration_per_target + 1
    return
  end

  Log.print("Looking for path from #{screen_node} to #{tgt}")
  #here need to check the type and differentiate

  #TODO sort the paths by completeness first, and then by length?
  unless explore(screen_node, tgt, stg, package_name_under_test)

    #Here we should, check if the stg is updatable, in which case we don't want to delete it, we just wanna store it away I guess?
    # unreached_targets << tgt
    Log.print("Attempting random")
    perform_random_with_update(package_name_under_test, stg, screen_node, tgt)
    $default_iteration_per_target = $default_iteration_per_target + 1
  
    #perform_random_action(activity, actions, login_widget_hash) #MAYBE HERE I should start with the random for now
    Log.print "Path unsuccessful"
    return
  end
  Log.print("Target node has been found #{tgt}, resetting the app and continuing")
  # need to check we reached the taret index
  if stg.is_target(tgt) #just in case deleted already, double check
    update_found_target(stg, tgt)
    $last_target_found_time = Time.now.to_i
    $picker.resetActionsWeight
    #stg.reset_all_paths(paths_to_target)
    reset_app($myConf.get_instrumented_apk)
  end
end

def perform_random_with_update(package_name_under_test, stg, screen_node, tgt)
  Log.print "Attempting stoat random action"
  actions, _, activity, login_widget_hash = get_executable_ui_events(package_name_under_test, stg, tgt) #to rewrite
  action = perform_random_action(activity, actions, login_widget_hash)
  #TODO only perform edge if we're not trying to login?
  #TODO store node reached after performing random action for stg updates here
  #success = perform_edge_transition(stg, tgt, $emulator_serial, edge)
  #IF WE RETURN nil, no action was taken, we need to update the stg
  sleep $g_event_delay #to do (should be abstracted away in execute action or smth)
  #here add screen_node as the previous node? (if no edge action, good, if edge action but failure, then should be from screen_node to current_node with random action?? if success then from )
  #success, screen_node = update_stg(stg, nil, nil, screen_node, action)
  if !action.nil?
    current_screen_node = get_current_base_screen(stg)
    if !current_screen_node.nil?
      Log.print "Updating stg with new edge"
      stg.add_edge_with_action_string(screen_node, current_screen_node, action) #STOPPING CONDITION,
    end
  end
end

#TODO sort the
  

  #If targets remaining
    # Evaluate current screen
    # Map screen to stg node
    # If screen in targets, remove from targets
      #log and restart
    # Pick target
    # Compute paths
      # If no path, drop target
    # Pick shortest path
    # Attempt path
    # If path done
      # If target reached
       #Record
    # try next path

    

def compute_paths(screen_node, tgt, stg)
  if tgt.class == "ScreenNodeEdge"
    paths_to_target = screen_node.nil? ? nil : stg.all_paths(screen_node, tgt.to).select{|path| stg.filter_by_action(path, tgt.action) }.sort_by(&:length)
  else
    paths_to_target = screen_node.nil? ? nil : stg.all_paths(screen_node, tgt).sort_by(&:length)
  end
  paths_to_target
end


def explore(screen_node, tgt, stg, package_name_under_test)
  paths = compute_paths(screen_node, tgt, stg)

  if paths.nil? || paths.empty?
    # if we cannot find node or there are no paths to explore, we allow Stoat to try and transition to a different state
    Log.print("FAILURE: No path from #{screen_node} to #{tgt} found")
=begin    Log.print("Attempting random")
    perform_random_with_update(package_name_under_test, stg, screen_node, tgt)
    $default_iteration_per_target = $default_iteration_per_target + 1
=end
    #Log.print("Moving on to next target")
    #perform_random_with_update(package_name_under_test, stg, screen_node, tgt)

    #Log.print("Attempting Stoat weighted exploration")
    #actions, _, activity, login_widget_hash = get_executable_ui_events(package_name_under_test, stg, tgt) #to rewrite
    #action = perform_random_action(activity, actions, login_widget_hash)
    #TODO only perform edge if we're not trying to login?
    #TODO store node reached after performing random action for stg updates here
    #success = perform_edge_transition(stg, tgt, $emulator_serial, edge)
    #IF WE RETURN nil, no action was taken, we need to update the stg
    #sleep $g_event_delay #to do (should be abstracted away in execute action or smth)
    #here add screen_node as the previous node? (if no edge action, good, if edge action but failure, then should be from screen_node to current_node with random action?? if success then from )
    #success, screen_node = update_stg(stg, nil, nil, screen_node, action)
    #if !action.nil?
    #  current_screen_node = get_current_base_screen(stg)
    #  if !current_screen_node.nil?
    #    stg.add_edge_with_action_string(screen_node, current_screen_node, action) #STOPPING CONDITION,
    #    return explore(current_screen_node, tgt, stg, package_name_under_test)
    #  end
    #end
    return false
    #perform_random_action()
    #recurse so explore again I guess?
    
  else
    # otherwise we try all paths to the target until successful
    Log.print("SUCCESS: #{paths.length()} Paths from #{screen_node} to #{tgt} found")
    paths.each_with_index do |path,index |
      Log.print "Attempting path #{index}"
      path.each { |p| Log.print p.to_s}

      if stg.is_path_obsolete(path)
        Log.print "Obsolete path, dropping ..."
        next
      else
        Log.print "Not obsolete"
      end

      success = attempt_path(path, tgt, stg, package_name_under_test)
      return true unless success.nil?

      if(exploration_time_exceeded)
        Log.print("Time exceeded for current target")
        Log.print("Moving on to next target")
        return false
      end
      activity, package = get_activity_and_package_name($emulator_serial)

      Log.print 'Backtracking before next path'
      recover_package_under_test(package, package_name_under_test)
      backtrack()
      return explore(screen_node, tgt, stg, package_name_under_test)
    end
  end
  false
end


#Need to deal with edges as target too
#two types of targets, screens and edges
#if edge, then search for a path to the src screen and then try to execute the last action on the edge ?
#maybe option argument to path, target type or smth
#or instead when the destination screen is marked, record the previous action ?
#NAH need to mark the entire edge as a target
# @todo need to store all dropped targets and check again when updating the stg is anything new is happening


def attempt_path(path, tgt, stg, package_name_under_test)
  screen_node = nil
  path.each do |edge|
    success = perform_edge_transition(stg, tgt, $emulator_serial, edge)
    sleep $g_event_delay
    #we assume we can't have success.nil here
    success,screen_node = update_stg(stg, edge, success, nil, nil)
    #TODO, if we could successfully trigger the transition but didn't get to what we want? should still try random actions
    while !success && (edge.attempts < $g_maximum_events.to_i) && !exploration_time_exceeded
      actions, _, activity, login_widget_hash = get_executable_ui_events(package_name_under_test, stg, tgt) #to rewrite
      action = perform_random_action(activity, actions, login_widget_hash)
      #TODO only perform edge if we're not trying to login?
      #TODO store node reached after performing random action for stg updates here
      success = perform_edge_transition(stg, tgt, $emulator_serial, edge)
      #IF WE RETURN nil, no action was taken, we need to update the stg

      if !action.nil? #in case we are still attempting to log in, we don't want to increase the number of actions
        #num_rand_events += 1
        edge.inc_attempts()
      end
      sleep $g_event_delay #to do (should be abstracted away in execute action or smth)
      #here add screen_node as the previous node? (if no edge action, good, if edge action but failure, then should be from screen_node to current_node with random action?? if success then from )
      success, screen_node = update_stg(stg, edge, success, screen_node, action)

      if(screen_node.nil?) #todo deal with this case, should we exit?
        Log.print "Unexpected behavior, current screen does not map to a screen"
        #deal specially with the permission case
        #if is_permission_screen(current_activity)
            #not return
        #return nil
      end
      if(success)
        Log.print "Succeeded to trigger edge and reach #{edge.to}"
      end
      if (!success  && !screen_node.nil? && !screen_node.name.eql?(edge.from.name)) #we moved to a different screen
        Log.print "Reached a different screen #{screen_node.name}, computing new path to #{edge.to.name}"
        if (stg.is_target(screen_node))
          Log.print("Target node has been found #{screen_node}, while looking for #{tgt}")
          update_found_target(stg, screen_node)
        end
        new_paths_to_target = compute_paths(screen_node, tgt, stg)
        unless new_paths_to_target.nil? || new_paths_to_target.empty?
          #attempt the new paths
          # No difference between trying only the first path and attempting all_paths since backtracking takes back to the first node in any case
          return attempt_path(new_paths_to_target[0], tgt, stg, package_name_under_test)
        end
        #todo here, deal with the case of permission screens separately
        return nil
      end
      if !success && actions.empty?
        print "No available random actions on current screen, skipping"
        edge.mark_dead()
        return nil
      end
    end
    if exploration_time_exceeded
      return nil
    end
    if !success #for now, we get a success even if we went to a different screen (future work, update), not success basically means we couldn't trigger the edge even after a given number of actions
      edge.mark_dead()
      return nil
    end

    #screen_node = get_current_base_screen(stg)
    # todo here should be precise get_current_screen_node
    Log.print("The current screen node <#{screen_node}>")
    # we check if we reached a target while following a path (shallow matching)
    # TODO do we want to mark any target we reach? even through random exploration?
    if (!screen_node.nil? && screen_node != tgt && edge.to.same_name(screen_node) && stg.is_target(screen_node))
      Log.print("Target node has been found #{screen_node}, while looking for #{tgt}")
      update_found_target(stg, screen_node)
    end
  end
  #Get the current node
  cur_activity, _ = get_activity_and_package_name($emulator_serial)
  record_activity_coverage(cur_activity)

  #screen_node = get_current_base_screen(stg)
  if screen_node.nil? || !screen_node.same_name(tgt) #should we only look by name ?
    Log.print("Wrong path in stg, does not lead to target #{path}")
    STDERR.puts "Wrong path in stg, does not lead to target #{path}"
    #Log.print("Stg to be updated in future work")
    return nil
  end
  true
end





# Attempts to perform the action indicated by the edge of the stg
# @return true if the action was successfully performed, false otherwise, nil if no action taken
def perform_edge_transition(stg, tgt, emu_serial, edge, attempts=3)
  Log.print("Performing edge transition \n")
  #attempts.times do
  cur_activity, package = get_activity_and_package_name(emu_serial)
  #record activity coverage
  record_activity_coverage(cur_activity)

  #screen_node = get_current_screen_node(stg)
  Log.print("The current activity <#{cur_activity}>")

  if cur_activity != edge.from.name
    Log.print("Diverged from path in stg #{cur_activity} instead of #{edge.from.name}")
    STDERR.puts "Diverged from path in stg #{cur_activity} instead of #{edge.from.name}"
    return nil
  end

  res_name = (!edge.action.res_name.nil?) ? edge.action.res_name : get_res_name(emu_serial, edge.action.resource_id)
  content_desc = edge.action.content_desc
  text = edge.action.text
  ui_type = edge.action.ui_type
  parent_id = edge.action.parent_id
  handler = edge.action.handler

  
  if res_name.nil? 
    Log.print("Cannot find action matching edge with resource id #{edge.action.resource_id}") #try with resource type and content desc and text instead ?
    if (!ui_type.nil? && ui_type.end_with?("item") && !ui_type.eql?("item"))
      #for list view, same with res_name
      #for spinner (for now), parent.click() then child.click
      #we should get the parent id if available
      Log.print("A list view item #{ui_type}")
      
      parent_res_name = get_res_name(emu_serial, parent_id)
      parent_class = "android.widget.ListView"
      parent_class = "android.widget.Spinner" if ui_type.eql?("spinneritem")
      parent_class = "android.support.v7.widget.RecyclerView" if ui_type.eql?("recyclerviewitem")
      if (!Python.click_by_parent_info(emu_serial, parent_res_name, parent_class, text))
        Log.print("Cannot find action matching edge with text #{text} for parent #{parent_res_name} ... moving on")
        # could not perform transition after max attempts
        #try anything I guess
        Log.print "Failed to perform transition\n #{edge}"
        false
      else
        Log.print("Found and executed action by parent info #{parent_class} #{parent_res_name}")
        true
      end
    elsif (content_desc.nil? || content_desc.empty? || !Python.click_by_content_desc(emu_serial, content_desc, handler))
      Log.print("Cannot find action matching edge with content desc #{content_desc}")
      if (!ui_type.nil? && ui_type.eql?("menu"))
        if(!content_desc.nil? && content_desc.eql?("Menu"))
          content_desc = 'More options'
        else
          content_desc = 'Menu'
        end
        Log.print("Attempting with default content desc #{content_desc} for menu")
        if(!Python.click_by_content_desc(emu_serial, content_desc, handler))
          Log.print("Cannot find action matching edge with content desc #{content_desc}")
          false
        else
          Log.print("Found and executed action by content desc")
          true
        end
      elsif (text.nil? || text.empty? || !Python.click_by_text(emu_serial, text, handler))
      Log.print("Cannot find action matching edge with text #{text} ... moving on")
      # could not perform transition after max attempts
      Log.print "Failed to perform transition\n #{edge}"
      false
      else
        Log.print("Found and executed action by text")
        #edge.to.name == cur_activity
        true
      end
    else
      Log.print("Found and executed action by content desc")
      #edge.to.name == cur_activity
      true
    end
  else 
    Log.print("Found res_id for action #{res_name}")
    if(!ui_type.nil? && (ui_type.eql?("listview") || ui_type.eql?("ListView"))) #we want to perform an action on the first child of the list view
      Log.print("A list view #{ui_type}")
    end
    if Python.click_by_resource_id(emu_serial, res_name, handler) #should check if succeeded?
      Log.print("Found and executed action by res_id #{res_name}")
      #edge.to.name == cur_activity
      true
    else
      Log.print "Failed to perform transition\n #{edge}"
      false
    end
  end
end


# Updates the STG with with newly uncovered transitions
# @return current_screen_node, success to trigger edge
def update_stg(stg, edge, success, previous_node, actionString)
  #TODO debug get_current_screen_node
  #current_screen_node = get_current_screen_node(stg)
  current_screen_node = get_current_base_screen(stg) #once since we only deal with ICC for now
  if(current_screen_node.nil?) #not part of the current package
    [false, current_screen_node]
  else
    Log.print "Checking for STG updates\n"
    if(success.nil?) #edge not exercised (not on src)
      if(!actionString.nil? && !previous_node.nil?) #DEAL with leaving the path I guess?
        stg.add_edge_with_action_string(edge.from, current_screen_node, actionString)
      end
      [edge.to.name.eql?(current_screen_node.name), current_screen_node]
    else
      #if trigger not exercised (so we're still on source?)
      # todo remove edge and update, for now nothing
      if(success) #if edge trigger was exercised
        success =  current_screen_node.name.eql?(edge.to.name)
        # if trigger exercise, but tgt not reached
        if(!success && !current_screen_node.name.eql?(edge.from.name))
          #TODO should be transition from node after random action (variation of src)
          stg.add_edge_with_action(edge.from, current_screen_node, edge.action)
        end
        #todo, if trigger exercised and target reached, should be from screen after random action to here
      end
      [success, current_screen_node]
    end
  end
end

def backtrack()
  #for now just restart the app
  #need to make sure we end up in the same starting state
  #go back then restart
  reset_app($myConf.get_instrumented_apk)
  $recovery_keyevent_back = 0

end
  


#here need to add the login type
#need to store the selected login type
#need to update the weights so it doesn't try with the rest again, 
#maybe only keep one login widget at once, if it's tried, move on to the next, but how ?
#need to check somehow if it failed before
#here, we shouldn't execute the action again if it's not on the screen, the weights shouldn't be updated

#what about if it's not in this order?
def attempt_login(login_widget_hash, activity, actions, attempts)
  Log.print("Attempting login")
  success = false
  #why do we need to loop ?
  #attempts.times do
  # perform the login action - username
  unless login_widget_hash[:username].nil?
    Log.print("Entering the username")
    action = UTIL.find_action_by_text_resid(actions, login_widget_hash[:username][0], login_widget_hash[:username][1])
    _, action_cmd = parseActionString(action)
    unless UTIL.change_cmd_to_resid_text(action, action_cmd).nil?
      action_cmd = UTIL.change_cmd_to_resid_text(action, action_cmd)
    end
    Log.print("Selected Action <#{action.strip}>")
    enterUsername(actions, login_widget_hash[:username][0], login_widget_hash[:username][1])
    update_action_execution_state(activity, action, action_cmd)
    # should it be set to nil afterwards ?
    #success =  
  end

  # perform the login action - password
  unless login_widget_hash[:password].nil?
    Log.print("Entering the password")
    action = UTIL.find_action_by_text_resid(actions, login_widget_hash[:password][0], login_widget_hash[:password][1])
    _, action_cmd = parseActionString(action)
    unless UTIL.change_cmd_to_resid_text(action, action_cmd).nil?
      action_cmd = UTIL.change_cmd_to_resid_text(action, action_cmd)
    end
    Log.print("Selected Action <#{action.strip}>")
    enterPassword(actions, login_widget_hash[:password][0], login_widget_hash[:password][1])
    update_action_execution_state(activity, action, action_cmd)
    #should there be a sleep ?
    #success only if the username was set as well ?
    success = true
  end

  # perform the login action - login
  unless login_widget_hash[:login].nil?
    Log.print("Clicking on the login widget ?")
    action = UTIL.find_action_by_text_resid(actions,
                                            login_widget_hash[:login][0],
                                            login_widget_hash[:login][1])
    _, action_cmd = parseActionString(action)
    unless UTIL.change_cmd_to_resid_text(action, action_cmd).nil?
      action_cmd = UTIL.change_cmd_to_resid_text(action, action_cmd)
    end
    #execute_event(action_cmd) #switch to execute action
    Log.print("Selected Action <#{action.strip}>")
    execute_action(activity, action) #need to only update the weights if successful tho
    success = true #should we break here ?
    Log.print("Adding 3s extra delay for login (network) ...")
    sleep $g_event_delay * 9
    Log.print("Done")
    #break
  end
  #end
  Log.print("Done attempting login")
  success
end


def update_crash_reporter(action_cmd, action_view_text)
  # record the execution info.
  $g_crash_reporter.log_test_execution_info(action_cmd, action_view_text, $default_A3E_iteration)
  # check whether some crash happens after the event is executed
  if $g_crash_reporter.has_crash
    # record the crash
    $g_crash_reporter.dump_crash_report_for_model_construction(10)
    # exit and restart the crash reporter
    # the exit logging call is paired with the start logging call before the event-triggering loop
    # when the ripping process ends, the start logging call is paired with the end logging call outside of the event-triggering loop
    $g_crash_reporter.exit_logging
    $g_crash_reporter.start_logging
  end
end

def update_action_execution_state(activity, action, action_cmd)
  action_id, action_cmd, action_view_type, action_view_text = parseActionString(action)

  $executed_action_list.push(action_id)
  $picker.updateActionExecutionTimes(action)
  $picker.updateActionsWeight
  dump_executed_actions(activity, action)
end

def execute_action(activity, action)
  Log.print "Executing action: #{action}"
  action_id, action_cmd, action_view_type, action_view_text = parseActionString(action)
  action_cmd = UTIL.change_cmd_to_resid_text(action, action_cmd)

  execute_event(action_cmd)
  update_action_execution_state(activity, action, action_cmd)

  update_crash_reporter(action_cmd, action_view_text) unless $g_disable_crash_report
  sleep $g_event_delay
  action
end

def perform_random_action(activity, actions, login_widget_hash)
  
  Log.print("Performing random action from #{actions.length} #{actions}")
  $picker.putActions(actions, activity)
  $picker.dumpExecutedActions # displays all executable actions
  Log.print("Before attempting login (after dump), collected #{actions.length} actions and #{login_widget_hash}")

  # try logins first before choosing action
  #try login first, otherwise pick action (screen state can change)

  #need to perform the action to move to next login step
  #what if the actions are different after attempting login ? NEED TO handle that case
  # if the login succeeded (a step of it), we don't want to update the weights 
  #will need to integrate better in weight updating logic
  need_to_perform_action = true
  unless $login_attempts >= 3 || login_widget_hash.empty?
    attempt_login(login_widget_hash, activity, actions, 3) 
    $login_attempts += 1
    current_activity,_ = get_activity_and_package_name($emulator_serial)
    need_to_perform_action = false unless current_activity.eql?(activity)
  end

  #if we haven't changed screen (check activity or screen node ?) or we never logged in
  if need_to_perform_action 
    if login_widget_hash.empty?
      Log.print("No login widget in current screen")
    else
      Log.print("No login or current activity #{current_activity} unchanged from #{activity}")
    end
  #if login_widget_hash.empty? or !attempt_login(login_widget_hash, activity, actions, 3)
    if actions.length > 0
        Log.print("Available actions")
        action = $picker.selectNextAction(actions)
        Log.print("Selected Action <#{action.strip}>")
        execute_action(activity, action)
        return action
      Log.print("Done performing random action")
    else 
      return false
    end
  else
     nil
  end
end



def enterUsername(actions, view_text, id)
  action = UTIL.find_action_by_text_resid(actions, view_text, id)
  _, action_cmd = parseActionString(action)
  execute_edit_action(action_cmd,checkActionParam(action),$myConf.get_username)
end

def enterPassword(actions, view_text, id)
  action = UTIL.find_action_by_text_resid(actions, view_text, id)
  _, action_cmd = parseActionString(action)
  execute_edit_action(action_cmd,checkActionParam(action),$myConf.get_password)
end

def enterUsernamePwd(package_name_under_test)
  # get executable actions and current activity
  actions, current_package, current_activity = get_executable_ui_events(package_name_under_test) #this serves to extract the ui with the usrname? But shouldn't it have been extracted already ?

  # we give up after three tries
  login_attempt = 0
  until $login_success || login_attempt > 2
    edit_text_actions = []
    button_actions = []
    click_actions = []
    actions.each do |action|
      action_id, action_cmd, action_view_type, action_view_text = parseActionString(action)
      puts "[ALODE] Evaluating action: #{action_cmd} with type #{action_view_type}"
      if action_cmd.eql?("back\n") || action_cmd.eql?("keyevent_back\n") # "back"
        next
      elsif action_cmd.include?('edit(')
        if edit_text_actions.length <= 2
          puts "[ALODE] Found EditText: #{action_cmd}"
          edit_text_actions << action_cmd
        else
          puts '[ALODE] - Warning - Found more than 2 EditText, there might be an error!'
        end
      elsif action_cmd.include?('click(')
        if action_view_type.include?('Button')
          button_actions << action_cmd
        else
          click_actions << action_cmd
        end
      end
    end

    # If we have two EditText
    if edit_text_actions.length.eql? 2
      puts '[ALODE] 2 EditText detected: infering username and password'
      # username
      action_param_username = checkActionParam(edit_text_actions[0])
      execute_edit_action(edit_text_actions[0], action_param_username, $myConf.get_username)
      # delay the next event
      sleep $g_event_delay
      # password
      action_param_password = checkActionParam(edit_text_actions[1])
      execute_edit_action(edit_text_actions[1], action_param_password, $myConf.get_password)
      # delay the next event
      sleep $g_event_delay
      if button_actions.any?
        execute_event(button_actions[0])
      elsif click_actions.any?
        execute_event(click_actions[0])
      else
        puts "[ALODE]- ERROR - No clickable actions at activity: #{current_activity}"
      end
    elsif edit_text_actions.length.eql? 1
      puts '[ALODE] Only 1 EditText detected: infering username; password appear afterwards'
      action_param_username = checkActionParam(edit_text_actions[0])
      execute_edit_action(edit_text_actions[0], action_param_username, $myConf.get_username)
      execute_event(click_actions[0])
      action_param_password = checkActionParam(edit_text_actions[0])
      execute_edit_action(edit_text_actions[0], action_param_password, $myConf.get_password)
      actions = get_executable_ui_events(package_name_under_test)
      new_button_actions = []
      new_click_actions = []
      actions.each do |action|
        action_id, action_cmd, action_view_type, action_view_text = parseActionString(action)
        if action_cmd.eql?("back\n") || action_cmd.eql?("keyevent_back\n") # "back"
          next
        elsif action_cmd.include?('click(')
          if action_view_type.include?('Button')
            new_button_actions << action_cmd
          else
            new_click_actions << action_cmd
          end
        end
      end
      if button_actions.any?
        execute_event(button_actions[0])
      elsif click_actions.any?
        execute_event(click_actions[0])
      else
        puts "[ALODE]- ERROR - No clickable actions at activity: #{current_activity}"
      end
    else
      if button_actions.any?
        execute_event(button_actions[0])
      elsif click_actions.any?
        execute_event(click_actions[0])
      else
        puts "[ALODE]- ERROR - No clickable actions at activity: #{current_activity}"
      end
    end
    last_activity = current_activity
    actions, current_package, current_acitivity = get_executable_ui_events(package_name_under_test)
    if !current_acitivity.eql? last_activity
      puts '[ALODE] We have logged in ...'
      $login_success = true
    else
      puts '[ALODE] We cannot login, trying again ...'
      $login_success = false
    end
    login_attempt += 1
  end
end


#need to update the weights
def execute_edit_action(action_cmd, action_param, text)
  if action_param.eql?('_by_classname_instance')
    # execute action by classname and instance
    class_name_pattr = /className=\'(.*)\',/
    class_name = (action_cmd.match class_name_pattr)[1]
    instance_pattr = /instance=\'(.*)\'/
    instance = (action_cmd.match instance_pattr)[1]
    puts "[ALODE]: #{action_param}, #{class_name} #{instance}"

    # construct the python cmd
    event_cmd = "#{$timeout_cmd} 60s python3 ./bin/events/login#{action_param}.py #{$emulator_serial} #{class_name} #{instance} #{text}"
    puts "$ #{event_cmd}"
    `#{event_cmd}`
  else
    # get the action param value
    first_quote_index = action_cmd.index("\'")  # get the first occurrence of '
    last_quote_index = action_cmd.rindex("\'")  # get the last occurrence of '
    # Note we should include the quotes to avoid the existence of whitespaces in the action_param_value
    action_param_value = action_cmd[first_quote_index..last_quote_index]
    puts "[D]: #{action_param}, #{action_param_value}"

    # construct the python cmd
    event_cmd = "#{$timeout_cmd} 60s python3 ./bin/events/login#{action_param}.py #{$emulator_serial} #{action_param_value} #{text}"
    puts "$ #{event_cmd}"
    `#{event_cmd}`
  end
end

def performLoginOnCommandFile(cmdFile)
  puts 'Not supported'
end

def checkActionParam(action_cmd)
  if action_cmd.include?('(text=') # get the action param, "text", "content_desc", "resource_id"
    action_param = '_by_text'
  elsif action_cmd.include?('(content-desc=')
    action_param = '_by_content_desc'
  elsif action_cmd.include?('(resource-id=')
    action_param = '_by_resource_id'
  elsif action_cmd.include?('direction=')
    action_param = '_by_direction'
  elsif action_cmd.include?('className=') && action_cmd.include?('instance=')
    action_param = '_by_classname_instance'
  end
  action_param
end

def loadExecutionPathFile(package_name_under_test)

  model_file_path = $myConf.get_stoat_tool_dir + '/login_models/' + package_name_under_test + '_model.txt'
  puts "[ALODE] Loading model file at: #{model_file_path}"
  execution_path = IO.readlines(model_file_path)

  activity_path = []
  activity_widget_map = {}
  execution_path.each do |line|
    acitivity = line.split(',')[0]
    widget = line.split(',')[1]
    puts "[ALODE] Load activity: #{acitivity} with Widget: #{widget}"
    activity_path << acitivity
    activity_widget_map[acitivity] = widget
  end
  [activity_path, activity_widget_map]
end

def retrace_steps(steps_file, delay_between_events)
  File.readlines(steps_file).each do |line|
    _, action_cmd = parseActionString(line)
    execute_event action_cmd
    sleep delay_between_events
  end
end

def reset_log_files
  # create new log files
  File.open($myConf.get_fsm_building_dir + '/' + 'fsm_building_progress.txt', 'w') do |f|
    f.puts '#executed_events #covered_lines #line_coverage_percentage(%) #total_exec_time (min)'
  end
  File.open($myConf.get_fsm_building_dir + '/' + 'tabu_action_list.txt', 'w') do |f|
    f.puts '#tabu action id listed in execution order'
  end
  File.delete('coverage.txt') if File.exist?('coverage.txt')
  if File.exist?($myConf.get_fsm_building_dir + '/' + 'a3e_runtime_log.txt')
    File.delete($myConf.get_fsm_building_dir + '/' + 'a3e_runtime_log.txt')
  end
  File.open("#{$myConf.get_fsm_building_dir}/reached_activity.txt", 'a') do |f|
    f.puts 'reached_screen_node  #time_to_reach (sec)'
  end
  File.open("#{$myConf.get_app_output_dir}/reached_connection.txt", 'a') do |f|
    f.puts 'reached_open_conn  #id #time_to_reach (sec)'
  end
end

def calc_avg_times
  sum = 0
  count = 0
  File.open("#{$myConf.get_fsm_building_dir}/reached_activity.txt", 'r').each_line do |f|
    if $. != 1
      sum += f.split(" ")[-1].to_f
      count += 1
    end
  end
  if count > 0
    File.open("#{$myConf.get_fsm_building_dir}/reached_activity.txt", 'a') do |f|
      f.puts "Avg time to reach one screen node: #{sum/count}"
    end
  end

  sum = 0
  count = 0
  Log.print "Waiting for background job to finish"
  puts "Waiting for background job to finish"
  pid =  Process.waitpid($background_job)
  Log.print "Background job finished  #{pid}"
  File.open("#{$myConf.get_app_output_dir}/reached_connection.txt", 'r').each_line do |f| #need to use the id as well
    if $. != 1
      sum += f.split(" ")[-1].to_f
      count += 1
    end
  end
  if count > 0
    File.open("#{$myConf.get_app_output_dir}/reached_connection.txt", 'a') do |f|
      f.puts "Avg time to reach one open connection: #{sum/count}"
    end
  end
end

def exploration_time_exceeded()
  (Time.now.to_i - $last_target_found_time) > 3600
end

# rip an app under test
def ripping_app(package_name_under_test, entry_activity_under_test, startr, noloop, stg)
  Log.print 'Started ripping app information'
  reset_log_files

  total_exec_time = 0.0
  $login_attempts = 0
  success = false
  unless $g_disable_crash_report
    # start crash logging
    $g_crash_reporter.start_logging
  end
  # from here just use the get views command and save it to a file
  # now get input from a file and do as described
  unless noloop
    # if $auto_login or $naive_login then
    #     # Load the execution path files
    #     activityPath, activityWidgetMap = loadExecutionPathFile(package_name_under_test)
    #     login_activity = activityPath[-1]
    # end

    # [LoginDetector] Here we perform the login
    # if $auto_login
    #   unless $login_success
    #     sleep 5
    #     performAutoLogin(package_name_under_test, activityPath, activityWidgetMap)
    #   end
    # end

    $default_A3E_iteration = 1

    # the main working loop
    while true

      Log.print "-------[Iteration: #{$default_A3E_iteration}]----------"

      start_time = Time.now

      # do main job --> drive the app to execute
      # #maybe we can store the current launcher in the conf?
      do_main_job(package_name_under_test, stg) #what if I somehow get stuck inside do_main_job?
      if stg.get_next_target.nil?
        $aapt.update_launcher
        if ($aapt.has_more_launchers)
          stg.reset_tries
          start_app $myConf.get_instrumented_apk
        else
            break
        end
        #set current launcher I guess?
        #I guess here we can check if we have remaining in store, we just set the launcher and do not restart?
        #here check if there are more launchers or not, if yes, need to start over? of should we store targets we couldn't reach?
      end

      end_time = Time.now
      # We profile the execution time here. Note the execution time is not very precise, since when we rip the app, we
      # may set some "sleep" interval during the execution to wait
      elapsed_time = (end_time - start_time).to_f / 60
      total_exec_time += elapsed_time

      puts "-----------------\n\n\n"


      # dump coverage files every five iterations, we do not dump coverage for closed-source apps
      if $closed_source_apk == false && $default_A3E_iteration % 100 == 0 && !$g_disable_coverage_report

        # dump code coverage
        # Note we use timeout to solve non-responding cases when dumping code coverage
        UTIL.execute_shell_cmd("#{$timeout_cmd} 2s adb -s #{$emulator_serial} shell am broadcast -a edu.gatech.m3.emma.COLLECT_COVERAGE")
        puts 'I: the code coverage is dumped. '

        # pull out the coverage file
        coverage_ec = $myConf.get_coverage_files_dir() + '/' + 'coverage_' + $default_A3E_iteration.to_s + '.ec'
        $adb.pullCov coverage_ec
        $adb.rmCov

        lineCov = 0
        lineCovPercentage = 0

        if File.exist?(coverage_ec)

          # if the project was compiled by ant, get code coverage by emma
          if $g_project_type.eql?('ant')

            coverage_files = `find #{$myConf.get_coverage_files_dir()} -name "*.ec"`
            puts coverage_files.to_s
            str_coverage_files = ''
            coverage_files.each_line do |line|
              str_coverage_files += line.strip + ','
            end
            # get the coverage em file
            coverage_em = $myConf.get_em_coverage_file()
            $g_coverage_txt = $myConf.get_coverage_files_dir() + '/' + 'coverage.txt'
            merge_cmd = 'java -cp ' + $myConf.get_emma_jar() + ' emma report -r txt -in ' + str_coverage_files + coverage_em + ' -Dreport.txt.out.file=' + $g_coverage_txt
            puts "$#{merge_cmd}"
            # execute the shell cmd
            `#{merge_cmd}`

            # parse the coverage file
            $coverager.parse_emma_coverage_report $g_coverage_txt
            # get the coverage info
            lineCov = $coverager.getLineCoverage
            lineCovPercentage = $coverager.getLineCoveragePercentage

          else # if the project was compiled by gradle, get code coverage by Jacoco

                    # coverage info format: "#covered_lines #line_coverage_percentage"
                    #cmd = "python #{$myConf.get_stoat_tool_dir()}/android_instrument/dump_coverage.py #{$myConf.get_app_absolute_dir_path()} fsm"
                    #puts "$ #{cmd}"
                    #coverage_info = `#{cmd}`
                    #puts "coverage_info: #{coverage_info}"

                    #coverage_data = coverage_info.split(' ')
                    #lineCov = coverage_data[0].to_f
                    #lineCovPercentage = coverage_data[1].to_f*100
            lineCov = 0
                    lineCovPercentage = 0

                  puts "lineCov = #{lineCov}"
                  puts "lineCovPercentage = #{lineCovPercentage}"

          end

          # record the maximum line coverage and the #events to reach this peak coverage
          if lineCov.to_i > $g_maximum_line_coverage
            $g_maximum_line_coverage = lineCov
            $g_maximum_line_coverage_events = $default_A3E_iteration
          end

        else
          lineCov = 0
          lineCovPercentage = 0
        end


        open($myConf.get_fsm_building_dir + '/' + 'fsm_building_progress.txt', 'a') do |f|
          f.puts "#{$default_A3E_iteration} #{lineCov} #{lineCovPercentage} #{total_exec_time}"
          puts "[A3E] Iteration: #{$default_A3E_iteration} lineCov: #{lineCov} lineCovPer: #{lineCovPercentage} totalTime(min): #{total_exec_time}"
        end

      # when it is a closed-source apk, we calculate method coverage
      elsif $closed_source_apk == true && $default_A3E_iteration % 5 == 0 && !$g_disable_coverage_report

        dump_coverage_cmd = "python3 #{$myConf.get_ella_tool_dir()}/coverage_fsm.py #{$myConf.get_app_dir_loc()}"
        puts "$ #{dump_coverage_cmd}"
        res = `#{dump_coverage_cmd}`
        method_coverage = res.strip

        open($myConf.get_fsm_building_dir + '/' + 'fsm_building_progress.txt', 'a') do |f|
          f.puts "#{$default_A3E_iteration} #{method_coverage} #{total_exec_time}"
          puts "[A3E] Iteration: #{$default_A3E_iteration} Method Coverage: #{method_coverage} totalTime(min): #{total_exec_time}"
        end
      end

      $default_A3E_iteration = $default_A3E_iteration + 1

      # if !$closed_source_apk && $default_A3E_iteration >= $g_maximum_events.to_i
      if !$closed_source_apk && exploration_time_exceeded
        # puts "[D] we have reached the maximum #{$g_maximum_events} UI events"
        puts '[D] an hour has elapsed since last reached target'
        puts '[D] exit A3E'
        puts '[D] moving on ...'

        unless $g_disable_coverage_report

          open($myConf.get_stoat_tool_dir() + '/fsm_building_results.csv', 'a') do |f|

            if $g_project_type.eql?('ant')
              # parse the coverage
              $coverager.parse_emma_coverage_report $g_coverage_txt
              covered_lines = $coverager.getLineCoverage()
              total_executable_lines = $coverager.getTotalExecutableLine()
              line_coverage_percentage = $coverager.getLineCoveragePercentage()

              covered_methods = $coverager.getMethodCoverage()
              total_methods = $coverager.getTotalMethods()
              method_coverage_percentage = $coverager.getMethodCoveragePercentage()

              total_classes = $coverager.getTotalClasses()
              app_dir_name = $myConf.get_app_dir_name()

              output = "#{app_dir_name},#{total_classes},#{total_methods},#{covered_methods},#{method_coverage_percentage}," +
                      "#{total_executable_lines},#{covered_lines},#{line_coverage_percentage},#{$g_maximum_line_coverage_events}"
              f.puts output
            #else
              # TODO do nothing for gradle project
            end

          end
        end

        # stop the ripping
        break

      # when it is a closed-source apk
      # elsif $closed_source_apk && $default_A3E_iteration >= $g_maximum_events.to_i
      elsif $closed_source_apk && exploration_time_exceeded
        # puts "[D] we have reached the maximum #{$g_maximum_events} UI events"
        puts '[D] an hour has elapsed since last reached target'
        puts '[D] exit A3E'
        puts '[D] moving on ...'

        unless $g_disable_coverage_report
          dump_coverage_cmd = "python3 #{$myConf.get_ella_tool_dir()}/coverage_fsm.py #{$myConf.get_app_dir_loc()}"
          puts "$ #{dump_coverage_cmd}"
          res = `#{dump_coverage_cmd}`
          method_coverage = res.strip

          open($myConf.get_stoat_tool_dir() + '/fsm_building_results.csv', 'a') do |f|

            app_dir_name = $myConf.get_app_dir_loc()

            output = "#{app_dir_name},#{method_coverage}"

            f.puts output

          end
        end

        # stop the ripping
        break
      end
    end
  end
  success = true
  Log.print('Done ripping the app')
ensure
  #always executed
  yield
  calc_avg_times if success
end

def pull_log_file()

  # pull log file
  puts '[A3E] pull the adb logcat file from sdcard to local location. '
  pull_log_cmd = "adb -s #{$emulator_serial} pull /sdcard/adb_logcat.log #{$myConf.get_fsm_building_dir}/adb_logcat_#{$default_A3E_iteration}.log"
  `#{pull_log_cmd}`

end

# install the app under test
def install_app(apk)
  $adb.install apk
  sleep 1
end

# uninstall the app under test
def uninstall_app(apk)
  pkg = $aapt.pkg apk
  uninstall_cmd = "adb -s #{$emulator_serial} uninstall #{pkg}"
  puts "$ #{uninstall_cmd}"
  `#{uninstall_cmd}`
  sleep 1
end

def install_troyd_and_app (apk)

  puts '[D] install troyd and the app under test ...'

  # get the package name of the APK
  pkg = $aapt.pkg apk

  ## build Troyd
  Troyd.setenv
  Troyd.rebuild_without_install(pkg)

  # install re-compiled troyd
  $adb.uninstall
  $adb.install $myConf.get_troyd_apk()

  # resign and install target app
  $adb.uninstall pkg
  shareduid = pkg + '.shareduid.apk'
  Uid.change_uid(apk, shareduid)
  resigned = pkg + '.resigned.apk'
  Resign.resign(shareduid, resigned)
  # system("rm -f #{shareduid}")
  $adb.install resigned

end

def write_conf(communication_port)
  output = "PORT = #{communication_port}"
  conf_file = "#{$myConf.get_fsm_building_dir()}/CONF.txt"
  open(conf_file, 'a') do |f|
    f.puts output
  end
  Log.print "Wrote port number #{communication_port} into file #{conf_file}"
end

def prepare_env()

  # uncomment the followings to enable offline logcat recording
  # clear the logcat buffer
  #  clear_log_buffer_cmd = "adb -s #{$emulator_serial} logcat -c"
  #  puts "$ #{clear_log_buffer_cmd}"
  #  `#{clear_log_buffer_cmd}`

  # start adb logcat filter, we focus on runtime errors, fatal errors, and ANR errors
  # see https://developer.android.com/studio/command-line/logcat.html
  #  error_log_file_name = $myConf.get_fsm_building_dir + "/" + "error_log.txt"
  #  error_log_cmd = "adb -s #{$emulator_serial} logcat *:E > #{error_log_file_name} &"
  #  puts "$ #{error_log_cmd}"
  #  `#{error_log_cmd}`

  # copy the config
  conf_file = $myConf.get_stoat_tool_dir + '/' + 'CONF.txt'
  out_dir = $myConf.get_fsm_building_dir

  Log.print "Copying A3E CONF file from #{conf_file} to #{out_dir}"
  `cp #{conf_file} #{out_dir}`

  # get the idle port to communicate with the server
  #$g_port = identifyIdlePorts($g_port)

  # write the configuration for the app under test
  Log.print 'Writing CONF file for the app under testing'
  write_conf($g_port)

  # copy coverage.em
  #em_file = $myConf.get_em_coverage_file()
  #`cp #{em_file} #{$myConf.get_fsm_building_dir()}`
end

def identifyIdlePorts(start_port_number)
  # always scan ports from start_port_number, until we find enough ports
  port_number = start_port_number
  while true
    cmd = "nc -z 127.0.0.1 #{port_number}; echo $?"
    output = `#{cmd}`
    puts "output=#{output}"
    if output.eql?("1\n")
      puts "I: Port #{port_number} is idle"
      return port_number
    else
      # increase the port number, try again !
      puts "I: Port #{port_number} is busy, try others ... "
      port_number += 1
      sleep 5
    end
  end
end

avd_name = ''
dev_name = ''
avd_opt = '' # e.g. "-no-window"
record = true
pkg_file_exists = false
activities_file_name = ''
noloop = true
apk_path = ''
steps_file = nil
stg_file = nil
strategy = ''
updatable = false

$closed_source_apk = false
$ella_coverage = false
$g_port = 2008
$g_crash_reporter = nil
$g_event_delay = 0 # the delay time between events
# the project type: "ant" or "gradle", default set as "ant"
$g_project_type = 'ant'
$g_disable_crash_report = true
$g_disable_coverage_report = false
$g_enable_screenshot = false
$g_app_start_wait_time = 60

# Don't know what this is for
# Dir.foreach(PARENT) {|f| fn = File.join(PARENT, f); File.delete(fn) if f != '.' && f != '..'}

OptionParser.new do |opts|
  opts.banner = "Usage: ruby #{__FILE__} target.apk [options]"
  opts.on('--avd avd', 'Name of the emulator in AVD Manager') do |n|
    avd_name = n
  end
  opts.on('--dev serial', 'AVD serial of the running emulator') do |s|
    dev_name = s
  end
  opts.on('--apk apk', 'The apk to test') do |l|
    apk_path = l
  end
  opts.on('--opt opt', 'avd options') do |o|
    avd_opt = o
  end
  opts.on('--search search', 'the search strategy to execute actions') do |h|
    Log.print "Search strategy #{h}"
    $picker.setStrategy(h)
  end
  opts.on('--events events', 'the maximum ui events to be executed') do |e|
    $g_maximum_events = e
    Log.print "Maximum ui events to be executed: #{$g_maximum_events}"
  end
  opts.on('--event_delay events', 'the delay time between events') do |y|
    $g_event_delay = y.to_i * 1.0 / 1000.0
    Log.print "Delay time between events: #{$g_event_delay}"
  end
  opts.on('--project_type type', 'the project type: ant or gradle') do |t|
    $g_project_type = t
    Log.print "Project type: #{$g_project_type}"
  end
  opts.on('--port port', 'the communication port') do |p|
    $g_port = p
    Log.print "Communication port: #{$g_port}"
  end
  opts.on('--disable_crash_report', 'disable crash report during execution') do
    $g_disable_crash_report = true
  end
  opts.on('--disable_coverage_report', 'disable coverage report during execution') do
    $g_disable_coverage_report = true
  end
  opts.on('--enable_dump_screenshot', 'enable dumping screenshot') do
    $g_enable_screenshot = true
  end
  opts.on('--no-rec', 'do not record commands') do
    record = false
  end
  opts.on('-loop', 'run a3e mode') do
    noloop = false
  end
  opts.on('-noloop', 'run a3e mode') do
    noloop = true
  end
  opts.on('--retrace_steps file', 'retrace steps before running') do |it|
    steps_file = it
  end
  opts.on('--stg file', 'XML file for STG') do |it|
    stg_file = it
  end
  opts.on('--updatable', 'update stg at runtime') do |it|
    updatable = true
  end
  opts.on('--screen_matching_strategy strategy', "the strategy for matching screen, i.e shallow (name only) or deep") do |m|
    strategy = m
    $deep_screen_matching = true if strategy.eql?('deep')
    puts "screen matching strategy: #{strategy}"
  end
  opts.on_tail('-h', '--help', 'show this message') do
    puts opts
    exit
  end
end.parse!

### Load System Configurations ###
$myConf = CONF.new()
config_file = File.expand_path(File.dirname(__FILE__)) + '/../../CONF.txt'
$myConf.read_config(config_file)

# get the wait time for app start/restart
$g_app_start_wait_time = $myConf.get_app_start_wait_time()

# we are testing an apk
$closed_source_apk = true
unless $g_disable_coverage_report
  $ella_coverage = true
end

# create the fsm building dir
$myConf.set_project_type($g_project_type)
$myConf.create_fsm_building_dir(apk_path, apk_path, $closed_source_apk)
apk_name = $myConf.get_instrumented_apk
####

# get the emulator name and serial
$emulator_name = avd_name
$emulator_serial = dev_name
Log.print "Emulator AVD name: #{$emulator_name}"
Log.print "Emulator serial: #{$emulator_serial}"


$avd = AVD.new(avd_name, avd_opt)
# create "adb" instance
$adb = ADB.new()
# init. the emulator serial
$adb.device $emulator_serial
$aapt = AAPT.new

unless $g_disable_crash_report
  # create the crash reporter
  Log.print("Crash reporter: #{g_disable_crash_report}")
  crash_report_dir = $myConf.get_fsm_building_dir()
  $g_crash_reporter = CrashReporter.new(apk_name,$emulator_serial,crash_report_dir)
end

# prepare the env
prepare_env


#########

#start reached connection counter in background

#todo add test setup option

$background_job = fork do
  reached_connection_cmd = "bash -x ../bin/reachedConnection.sh #{$emulator_serial} #{$myConf.get_app_output_dir} &"
  #cmd = "bash -x ./bin/analyzeAndroidApk.sh fsm_apk #{apk_path} apk #{apk_path} >> /data/faridah/menu-runs/server.log.new 2>&1 "
  puts "$ #{reached_connection_cmd}"
  exec reached_connection_cmd
end
#Process.detach(job)

##########


Log.print("Stoat mode, maximum allowed #events: #{$g_maximum_events}")
Log.print("Apk Name: #{apk_name}")

pkg = $aapt.pkg apk_name
#$launchers = $aapt.launchers apk_name
#launchers = $aapt.launchers apk_name
#@todo we need to store the targets that we could not reach and iterate through all the launchers with the remaining list of targets?
# launchers.each {|act|
# put everything here (or maybe not, check there won't be any issues with coverage or concurrency)
# }
act = $aapt.launcher apk_name
stg = STG.construct_stg(stg_file, strategy, updatable)

start_app_cmd = ''
if !act.nil?
  start_app_cmd =  'adb -s ' + $emulator_serial + ' shell am start -S -n ' + pkg + '/' + act
else
  start_app_cmd =  'adb -s ' + $emulator_serial + ' shell monkey -p ' + pkg + ' -c android.intent.category.LAUNCHER 1'
  act = 'stoat-fake-entry-activity' # when the app does not has an explicit launchable activity, we use the monkey approach to start it
end


# start the app
UTIL.execute_shell_cmd(start_app_cmd)
UTIL.execute_shell_cmd("sleep #{$g_app_start_wait_time}")

# report A3E state and wait for the server
report_crawler_state('READY', act)

# retrace steps before beginning to rip app
retrace_steps(steps_file, $g_event_delay) if steps_file

# rip the app
ripping_app(pkg, act, 1, noloop, stg) {
  # stop the server
  report_crawler_state('STOP','')
  # stop the coverage recording of Ella
  if $ella_coverage == true
    if File.file?("#{$myConf.get_ella_tool_dir()}/ella.sh")
      clear_ella_coverage_cmd = "#{$myConf.get_ella_tool_dir()}/ella.sh e #{$emulator_serial}"
      puts "$ #{clear_ella_coverage_cmd}"
      `#{clear_ella_coverage_cmd}`
    end
  end


  cleanup_adb_services()

  unless $g_disable_crash_report
    # exit crash logging
    $g_crash_reporter.exit_logging()
  end
}

# clean up adb in the localhost only for the target device
#logcat_pids = `ps -aux | grep "adb -s #{$emulator_serial} logcat" | awk '{print $2}'`
#puts "#{logcat_pids}"
#logcat_pids_list = logcat_pids.sub!("\n", ' ')
#kill_adb_cmd = "kill -9 #{logcat_pids_list}"  # kill the adb logcat process
#puts "$ #{kill_adb_cmd}"
#`#{kill_adb_cmd}`

exit

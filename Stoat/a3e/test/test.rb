# require_relative '../bin/info'
# require_relative '../bin/python'
# require_relative '../bin/stg'
require 'time'

# def attempt_transition(edge)
#   max_attempts = 10
#   max_attempts.times do
#     cur_activity = get_activity_name('emulator-5554')
#
#     # ends if we are no longer on the src activity
#     if cur_activity != edge.from.name
#       return cur_activity == edge.to.name
#     end
#
#     res_name = get_res_name('emulator-5554', '2131296402')
#     if res_name.nil?
#       # perform random action if the targeted action cannot be identified
#       # hope to unlock action or reach target action
#       Log.print('Cannot find action matching edge, performing random action')
#       # action_history.push(perform_random_action(activity, actions, login_widget_hash))
#     else
#       Log.print("Found and executing action")
#       Python.click_by_resource_id('emulator-5554', res_name)
#     end
#   end
#   # could not perform transition after max attempts
#   Log.print "Failed to perform transition\n #{edge}"
#   nil
# end
#
# puts attempt_transition(
#   STG::ScreenNodeEdge.new(
#     STG::ScreenNode.new("com.asuc.asucmobile.main.MainActivity"),
#     STG::ScreenNode.new("com.asuc.asucmobile.main.CreditsDialog"),
#     STG::StaticAction.new("a","b", '2131296402')
#   )
# )
#
#
puts Time.now.to_i
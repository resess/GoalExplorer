# frozen_string_literal: true

# Executes the bash command
# @param [String] cmd the command to run
# @return [String] the run output of the command
def execute_shell_cmd(cmd)
  puts "$ #{cmd}"
  `#{cmd}`
end

# execute the cmd and outputs the running info to console
# @param [String] cmd the command to run
# @return [nil]
def execute_shell_cmd_output(cmd)
  puts "$ #{cmd}"
  IO.popen(cmd).each do |line|
    puts line
  end
  nil
end

require_relative 'log'

module Bash
  def Bash.run(cmd)
    Log.print "$ #{cmd}"
    res = `#{cmd}`
    res
  end
end
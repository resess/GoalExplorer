module ACT
	def ACT.extract_act(file_name)
		acts=IO.readlines(file_name, chomp: true).select{|act| 
			!(act.nil? || act.strip.empty?)
		}
        
# 		act2=acts[counter..acts.length]
	end
end
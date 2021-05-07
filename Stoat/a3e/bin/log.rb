# frozen_string_literal: true

# module to provide cleaner logging
module Log
  # Gives detailed logging information
  # @param out the message to output
  # @param show_full_path whether to display file names with the full file directory
  def self.print(out, show_full_path = false)
    caller_locations(1, 1)&.first.tap do |file|
      if show_full_path
        puts "[#{file.path}:#{file.lineno}][#{file.base_label}] #{out}"
      else
        puts "[#{File.basename(file.path)}:#{file.lineno}][#{file.base_label}] #{out}"
      end
    end
  end
end

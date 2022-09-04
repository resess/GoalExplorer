class CustomError < StandardError
    def initialize(msg="Something went wrong")
        super
    end
end
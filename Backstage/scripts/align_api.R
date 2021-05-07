#align_dataset.R
require(argparse)
source("utils.R")
parser = ArgumentParser()
parser$add_argument("-u", required=TRUE,dest="ui.file",help="Path to input file with words")
parser$add_argument("-a", required=TRUE, dest="api.file",help="")
parser$add_argument("-oa", required=TRUE, dest="out.api",help="")
args = parser$parse_args()
    #c("-u","1","-a","2","-o","3"))

ui.file = args$ui.file
api.file = args$api.file
out.api = args$out.api
ui=load_data(ui.file)
print("ui loaded")
print(dim(ui))
api=load_data(api.file)
print("api loaded")
print(dim(api))
api=api[api$id %in% ui$id,]
print(dim(api))
save_data(api,out.api)
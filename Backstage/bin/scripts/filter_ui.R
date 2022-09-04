require(argparse)
source("utils.R")
parser = ArgumentParser()
parser$add_argument("-u", required=TRUE, dest="ui.file",help="in ui file")
parser$add_argument("-o", required=TRUE, dest="out.file",help="out")
parser$add_argument("-f", nargs='+', dest="filter.type",help="filter type: buttons,like_buttons,callback")
parser$add_argument("-c", dest="with.context",action="store_true",help="take only with context")
#args = parser$parse_args(c("-u","ui","-o","ui_filtr","-d",12,"-f","buttons","callback"))
aa= c("-u","data/ui_lemm.txt","-o","ui_filtr.rds")
args = parser$parse_args()

filter.type.list=c("buttons","like_buttons","callback")
filter.type=args$filter.type

ui.file=args$ui.file
out.file=args$out.file
if (grepl("rds$",ui.file)){
    ui=readRDS(ui.file)
}else{
    ui=read.table(ui.file,head=T,sep=";")
}

print("ui dim")
print(dim(ui))
if (length(filter.type)>0){
    stopifnot(filter.type %in% filter.type.list)
    if (filter.type=="like_buttons"){
        ui_buttons=ui[grep("button",ui$type,ignore.case=T),]
        ui_imageview=ui[grep("imageview",ui$type,ignore.case=T),]
        ui_textview=ui[grep("textview",ui$type,ignore.case=T),]
        print("ui buttons + custom buttons only")
        print(dim(ui))
        #ui=rbind(ui_buttons,ui_imageview,ui_textview)
	ui=ui_buttons
    }
    if (filter.type=="buttons"){
        ui=ui[ui$type=="Button",]
        print("ui buttons only")
        print(dim(ui))
    }
}
print("ui filtred")
print(dim(ui))
ui=ui[ui$label!="",]
print("ui non empty label")
print(dim(ui))
if (args$with.context){
    ui=ui[ui$context!="",]
    print("ui non empty context")
    print(dim(ui))
}
ui=ui[!grepl("#",ui$label),]
print("no doubles with #")
print(dim(ui))
ui=ui[!is.na(ui$id),]
print("no na")
print(dim(ui))

save_data(ui,out.file)



require(argparse)
source("utils.R")
parser = ArgumentParser()
parser$add_argument("-u", required=TRUE,dest="ui.file",help="Path to input file with words")
parser$add_argument("-a", required=TRUE, dest="api.file",help="")
parser$add_argument("-ou", required=TRUE, dest="out.ui",help="")
parser$add_argument("-oa", required=TRUE, dest="out.api",help="")
aa=c("-a","data/api_5cparamr_filtered.rds","-u","data/ui_unique.rds","-oa","data/new_api.rds","-ou","data/new_ui.rds")
args = parser$parse_args()
    #c("-u","1","-a","2","-o","3"))

ui.file = args$ui.file
api.file = args$api.file
if (grepl("rds$",ui.file)){
    ui=readRDS(ui.file)
}else{
    ui=read.table(ui.file,head=T,sep=";")
}
print("ui loaded")
print(dim(ui))
#ui=ui[,names(ui)!="rawtext"]
if (grepl("rds$",api.file)){
    api=readRDS(api.file)
}else{
    api=read.table(api.file,head=T,sep=";")
}
print("api loaded")
api.apk=unique(paste0(api$apk,api$uid))
ui.apk=unique(paste0(ui$apk,ui$uid))
print(length(api.apk))
print("common")
print(length(intersect(ui.apk,api.apk)))
#df=setdiff(api.apk,ui.apk)
#head(api[paste0(api$apk,api$uid)%in%df,],50)

api$merge<-paste0(api$apk,".",api$uid,".",api$declaringClass)
ui$merge<-paste0(ui$apk,".",ui$uid,".",ui$declaringClass)
###MERGE
api.merge.columns=c("callback","api","category","merge","sensitive","apitype")
#api.id.columns=c("apk","id")
ui.api.class=merge(ui, api[,api.merge.columns], by="merge")
print("have class")
print(length(unique(ui.api.class$id)))

api.rest=api[!api$merge %in% ui.api.class$merge,]
api.class=api.rest[api.rest$declaringClass!="default_value",]

####api.class.rest=
#api.class$class<-"default_value_but"

api.class$merge<-paste0(api.class$apk,".",api.class$uid,".","default_value")
api.class.ui.def=merge(ui,api.class[,api.merge.columns], by="merge")
print("api class")
print(length(unique(api.class.ui.def$id)))

#FIXME
#api.default=api.rest[api.rest$class=="default_value",]
#api.default$class<-"default_value_api"
#api.default$merge<-paste0(api.default$apk,".",api.default$id,".",api.default$class)

ui.rest=ui[!ui$merge %in% ui.api.class$merge,]
ui.class=ui.rest[ui.rest$declaringClass!="default_value",]
#ui.class$class<-"default_value_api"
ui.class$merge<-paste0(ui.class$apk,".",ui.class$uid,".","default_value")
api.def.ui.class=merge(ui.class, api[,api.merge.columns], by="merge")
print("ui class")
print(length(unique(api.def.ui.class$id)))

#FIXME
#ui.default=ui_rest[ui_rest$class=="default_value",]
#ui.default$class<-"default_value_but"
#ui.default$merge<-paste0(ui_default$apk,".",ui_default$id,".",ui_default$class)


#############
# require(data.table)
# dt_default=data.table(ui_default)
# setkey(dt_default,merge)
# dt_class=data.table(ui_class)
# setkey(dt_class,merge)
# dt.api_class=data.table(api_class[,api_merge_columns])
# setkey(dt.api_class,merge)
# dt.api_default=data.table(api_default[,api_merge_columns])
# setkey(dt.api_default,merge)

# dt.api_class_ui_def=dt_default[dt.api_class, nomatch=0]
# dt.api_def_ui_class=dt_class[dt.api_default, nomatch=0]
# api_class_ui_def=data.frame(dt.api_class_ui_def)
# api_def_ui_class=data.frame(dt.api_def_ui_class)


ui.api=rbind(ui.api.class,api.class.ui.def,api.def.ui.class)

new.ui=ui[ui$id %in% ui.api$id,]
new.ui=unique(new.ui)
print("size of ui")
print(nrow(new.ui))
save_data(new.ui,args$out.ui)
    #paste0(data_dir,"corpus.rds"))

save_data(ui.api,args$out.api)
    #paste0(data_dir,"/api_list.rds"))
require(skmeans)
require(argparse)
require(cluster)
require(clue)
require(parallel)
source("utils.R")
extend_dt<-function(dt){
    x=dt$context
    x <- strsplit(x, "\\.")
    DT <- dt[rep(sequence(nrow(dt)), vapply(x, length, 1L))]
    DT[, phrases := unlist(x, use.names = FALSE)]
    return(DT)
}

parser = ArgumentParser()
parser$add_argument("-u", required=TRUE,dest="ui.file",help="Path to ui")
parser$add_argument("-l", required=TRUE, dest="labels.file",help="out labels")
parser$add_argument("-c", required=TRUE, dest="context.file",help="out context")
parser$add_argument("-m", required=TRUE, dest="model.file",help="model")
parser$add_argument("-v", dest="vec.file",help="in vec file")
parser$add_argument("-f", dest="feature.file",help="features file")
aa=c("-u","data/ui_tag.txt","-v","data/label_vec.txt","-c","data/cluster100_context.rds","-l","data/cluster100_labels.rds","-m","data/clust100.rds")
args = parser$parse_args()

ui=load_data(args$ui.file)
clust=load_data(args$model.file)

print("ui loaded")
context=unique(ui$context)
print("context")
print(length(context))

vec=read.table(file=args$vec.file,quote="",head=T, sep=";")
print("vec loaded")
print(dim(vec))
rownames(vec)<-vec[,1]
print("start inferring")

# phrases.clust.list=mclapply(context[1:5],mc.cores=detectCores(), function(x){
#     phrases=unlist(strsplit(x,"\\."))
#     phrases.vec=vec[rownames(vec) %in% phrases,]
#     phrases.vec=as.matrix(phrases.vec[,-1])
#     membership=cl_predict(clust,phrases.vec,type="membership")
#     phrases.clust=do.call(pmax, split(membership, seq(nrow(membership))))
#     return(phrases.clust)
# })
# phrases.clust.m=do.call(rbind,phrases.clust.list)
# rownames(phrases.clust.m)<-context
# colnames(phrases.clust.m)<-1:ncol(phrases.clust.m)
#phrases.clust.df=as.data.frame.matrix(phrases.clust.m)
require(data.table)
dt=data.table(cbind(did=1:length(context),context=context))
dt.ex=extend_dt(dt)
all=cl_predict(clust,as.matrix(vec[,-1]),type="membership")
cm=as.data.frame.matrix(clust$membership)
n=ncol(cm)
all=as.data.frame.matrix(all)
all$phrases<-vec[,1]
dt.ex.m=merge(dt.ex,all,by="phrases")
dt.ex.vec=dt.ex.m[,lapply(.SD,max),by=context,.SDcols = paste0("V",1:n)]
phrases.clust.m=as.data.frame(dt.ex.vec)
rownames(phrases.clust.m)<-phrases.clust.m$context
phrases.clust.m=phrases.clust.m[,names(phrases.clust.m)!='context']


print("saving data")
dim(phrases.clust.m)
save_data(phrases.clust.m,args$context.file)
cm=as.data.frame.matrix(clust$membership)
rownames(cm)<-rownames(clust$membership)
save_data(cm,args$labels.file)
dim(cm)
ui.label=merge(ui[,c("id","label","context")],cm,by.x="label",by.y="row.names")
ui.label.context=merge(ui.label,phrases.clust.m,by.x="context",by.y="row.names")
ui.label.context=ui.label.context[,!names(ui.label.context)%in%c("label","context")]
save_data(ui.label.context,args$feature.file)
# clust$membership 

####
# require(data.table)
# dt=data.table(cbind(did=1:length(context),context=context))
# dt.ex=extend_dt(dt)
# vec.dt=data.table(vec)
# names(vec.dt)[1]<-"phrases"
# setkey(vec.dt,phrases)
# dt.ex.m=merge(dt.ex,vec.dt,by="phrases")
# dt.ex.vec=dt.ex.m[,lapply(.SD,max),by=context,.SDcols = paste0("X",0:299)]



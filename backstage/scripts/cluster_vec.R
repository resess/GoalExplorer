require(skmeans)
require(argparse)
require(cluster)
source("utils.R")
parser = ArgumentParser()
parser$add_argument("-u", required=TRUE,dest="ui.file",help="Path to icons")
parser$add_argument("-o", required=TRUE, dest="out.file",help="Path to bin data folder")
parser$add_argument("-m", required=TRUE, dest="model.file",help="Path to bin data folder")
parser$add_argument("-v", dest="vec.file",help="vec file")
parser$add_argument("-n", type="integer", dest="n.clust",help="is features or dist")
parser$add_argument("-cm", type="double", dest="m",help="flex param")
aa=c('-u','data/ui_tag.txt','-v','data/label_vec.txt','-o','data/cluster100_words.txt','-m','data/clust100.rds','-n',100)
args = parser$parse_args()

ui=load_data(args$ui.file)
print("ui loaded")
labels=unique(ui$label)
print("labels")
print(length(labels))
k=args$n.clust

vec=read.table(file=args$vec.file,quote="",head=T, sep=";")
print("vec loaded")
print(dim(vec))
rownames(vec)<-vec[,1]
#vec.matrix=as.matrix(vec[,-1])

labels.vec=vec[rownames(vec) %in% labels,]
labels.vec=as.matrix(labels.vec[,-1])
print(dim(labels.vec))
print("start clustering")
clust=skmeans(labels.vec, k, m = args$m, method="pclust")
print("saving data")
save_data(clust,args$model.file)
#clust=skmeans(icons.matrix, k, m = 1.1, method="pclust")
#with m>1 get fuzzy clusters
# clust$membership 
out.clust.file=args$out.file
write("",file=out.clust.file)
for(i in unique(clust$cluster)){
    write(i,file=out.clust.file,append=T)
    write(paste(names(clust$cluster[clust$cluster==i]),collapse=", "),file=out.clust.file,append=T)
}
######
labels.freq=as.data.frame(table(labels))
names(labels.freq)<-c("label","Freq")
#labels.freq=as.data.frame()
top.n=7
#most frequent
out.clust.file=paste0(out.clust.file,"_freq.txt")
write("",file=out.clust.file)
for(i in unique(clust$cluster)){
    write(i,file=out.clust.file,append=T)
    c.labels=names(clust$cluster[clust$cluster==i])
    c.freq=labels.freq[labels.freq$label %in% c.labels,]
    c.freq=c.freq[order(c.freq$Freq,decreasing=T),]
    c.freq.write=head(c.freq,top.n)
    write(paste(c.freq.write$label,collapse=", "),file=out.clust.file,append=T)
}
membership=as.matrix(clust$membership)
top.terms=apply(membership,2,function(x){
    x=(sort(x,decreasing=T))
    return(names(x)[1:top.n])
})

out.clust.file=paste0(args$out.file,"_close.txt")
write("",file=out.clust.file)
for(i in unique(clust$cluster)){
    write(i,file=out.clust.file,append=T)
    write(paste(top.terms[,i],collapse=", "),file=out.clust.file,append=T)
}

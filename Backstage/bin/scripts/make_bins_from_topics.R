#make non exclusive bins
#can be clusters after topics, api categories
source("utils.R")
require(argparse)
parser = ArgumentParser()
parser$add_argument("-i", required=TRUE,dest="in.file",help="Path to input file with words")
parser$add_argument("-o", required=TRUE, dest="out.dir",help="")
parser$add_argument("-m", required=TRUE, dest="mut.file",help="")
parser$add_argument("-t", dest="threshold",default=0.2,type="double",help="threshold for topics")
args = parser$parse_args()

#topic.col.prefix=args$topic.col.prefix
threshold=args$threshold
in.file = args$in.file
out.dir = args$out.dir
dir.create(out.dir, showWarnings = FALSE)

normal=load_data(in.file)
print(dim(normal))
mutants=load_data(args$mut.file)
print(dim(mutants))
data=rbind(normal, mutants)
data.no.id=data[,names(data)!="id"]
data$id<-rownames(data)
bin.names=grep("id",names(data),invert=T,value=T)
all_items=c()
max.values=apply(data.no.id, 1, function(x) max(as.numeric(x)))

solid.items=data[max.values>threshold,"id"]
######
sorted.topics=as.data.frame(t(apply(data[,bin.names], 1, function(x)names(sort(x,decreasing=T)))))
top.topics=sorted.topics[,1:2]
top.topics$id<-data$id
require(reshape)
top.topics.list=melt(top.topics,id=c("id"))
top.topics.list=top.topics.list[!top.topics.list$id %in% solid.items,]
######
bins=unique(top.topics.list$value)
for(bin in bins){
    print(bin)
    bin.data=top.topics.list[top.topics.list$value==bin,"id",drop=F]
    bin.data.solid=data[data[,bin]>threshold,"id",drop=F]
    bin.data=rbind(bin.data,bin.data.solid)
    all_items=rbind(bin.data,all_items)
    write.table(bin.data,file=paste0(out.dir,"/",bin,"_bin_id.txt"),row.names=F)
}

# for(bin in bin.names){
#     print(bin)
#     bin.data=data[data[,bin]>threshold,]
#     all_items=rbind(bin.data,all_items)
#     write.table(bin.data[,"id",drop=F],file=paste0(out.dir,"/",bin,"_bin_id.txt"),row.names=F)
# }
all_id=unique(all_items$id)
print(length(all_id))
print(dim(mutants[mutants$id %in% all_id,]))
print(dim(normal[normal$id %in% all_id,]))
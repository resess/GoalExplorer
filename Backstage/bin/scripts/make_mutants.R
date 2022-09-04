#make mutants v1.1
set.seed(111)
library(argparse)
require(plyr)
require(data.table)
source("utils.R")

parser = ArgumentParser()
parser$add_argument("-u", required=T,dest="data.file",help="ui csv file (or rds)")
parser$add_argument("-o", required=T,dest="output.file",help="")
parser$add_argument("-n", type="integer", dest="n.mutants",help="number of mutants")
parser$add_argument("-t", required=T,dest="mutant.type",help="mutant type (dist, rand, cross)")
parser$add_argument("-d", dest="l.dist", help="distances")

aa=c("-u","data/ui_tag.txt","-o","data/label_mutants_features.rds","-n",1000, "-d","data/label_dist.rds")
args = parser$parse_args()
n.mutants=(args$n.mutants)#100
#switch buttons within activity
data.file=args$data.file
data=load_data(data.file)
l.dist=load_data(args$l.dist)
l.dist=l.dist[,colnames(l.dist)%in%data$label]
l.dist=l.dist[rownames(l.dist)%in%data$label,]


if (args$mutant.type=="dist" || args$mutant.type=="rand"){
    if(args$mutant.type=="dist"){
        threshold=0.2
    }else{
        threshold=2
    }
    mutant.id=sample(data$id, n.mutants)
    print("mutant")
    print(head(mutant.id))
    data=data[data$id %in% mutant.id,]
    print(dim(data))
    mutants=c()
    for (app in mutant.id){
        candidate=data[data$id==app,]
        text=candidate$label
        c.dist=which(l.dist[rownames(l.dist)==text,]<threshold)
        new.text=names(c.dist[sample(length(c.dist),1)])
        m1=candidate
        m1$oldlabel<-m1$label
        m1$label<-new.text
        mutants=rbind(m1,mutants)
    }
}else if (args$mutant.type=="cross"){
    count.apk=count(data,"apk")
    apk=unique(count.apk[count.apk$freq>1,]$apk)
    mutant.apk=sample(apk, n.mutants/2)
    data=data[data$apk %in% mutant.apk,]
    mutants=c()

    for (app in mutant.apk){
        candidates=data[data$apk==app,]
        candidate=candidates[sample(nrow(candidates),1),]
        text=candidate$label
        text1=gsub(" ","|",text)
        m=candidates[grep(text1,candidates$label,invert=T),]
        if (nrow(m)>0){
            m1=m[1,]
            m2=candidate
            l1=m1$label
            l2=candidate$label
            m1$oldlabel<-l1
            m2$oldlabel<-l2
            m1$label<-l2
            m2$label<-l1
            #i1=m1$id
            #i2=m2$id
            #m1$id<-i2
            #m2$id<-i1
            mutants=rbind(m1,m2,mutants)
        }
    }
}else
{
    stop("Unknown mutant type")
}
out.file=args$output.file
save_data(mutants,out.file)


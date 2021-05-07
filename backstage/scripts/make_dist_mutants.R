#make mutants v1.1
set.seed(111)
library(argparse)
require(plyr)
require(data.table)
source("utils.R")

parser = ArgumentParser()
parser$add_argument("-u", required=T,dest="data.file",help="ui csv file (or rds)")
parser$add_argument("-o", dest="output.file",help="")
parser$add_argument("-n", type="integer", dest="n.mutants",help="number of mutants")
parser$add_argument("-t", dest="mutant.type",help="mutant type (dist, rand, cross)")
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

count.apk=count(data,"apk")
#apk=unique(count.apk[count.apk$freq>0,]$apk)
#mutant.apk=sample(apk, n.mutants)
mutant.id=sample(data$id, n.mutants)
print("mutant")
print(head(mutant.id))
data=data[data$id %in% mutant.id,]
print(dim(data))
mutants=c()

for (app in mutant.id){
    candidate=data[data$id==app,]
    #candidate=candidates[sample(nrow(candidates),1),]
    text=candidate$label
    c.dist=which(l.dist[rownames(l.dist)==text,]<0.2)
    new.text=names(c.dist[sample(length(c.dist),1)])
    m1=candidate
    m1$oldlabel<-m1$label
    m1$label<-new.text
    mutants=rbind(m1,mutants)
    
}
out.file=args$output.file
save_data(mutants,out.file)

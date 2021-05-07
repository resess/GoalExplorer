#make mutants v1.1
set.seed(1500)
library(argparse)
require(plyr)
require(data.table)
source("utils.R")

parser = ArgumentParser()
parser$add_argument("-u", required=T,dest="data.file",help="ui csv file (or rds)")
parser$add_argument("-o", dest="output.file",help="Path to store results")
parser$add_argument("-n", type="integer", dest="n.mutants",help="Path to store results")
aa=c("-u","data/ui_tag.txt","-o","data/label_mutants_features.rds","-n",1000, "-cv","data/cluster100_context.rds","-lv","data/cluster100_labels.rds","-l","send")
args = parser$parse_args()
n.mutants=(args$n.mutants)#100
#switch buttons within activity
data.file=args$data.file
data=load_data(data.file)
stopifnot("id" %in% names(data))

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
        #l1=m1$label
        #l2=candidate$label
        #m1$oldlabel<-l1
        #m2$oldlabel<-l2
        #m1$label<-l2
        #m2$label<-l1
	i1=m1$id
	i2=m2$id
	m1$id<-i2
	m2$id<-i1     
        mutants=rbind(m1,m2,mutants)
    }
}

out.file=args$output.file
save_data(mutants,out.file)

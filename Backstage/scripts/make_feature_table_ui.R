#ui_train_test.R
require(argparse)
source("utils.R")

set.seed(42)
library(argparse)
require(plyr)
require(data.table)
source("utils.R")
join.factor=0

join.factor=0.8
threshold=0.5
join_vec<-function(l.vec,c.vec) {
    rownames(l.vec)<-l.vec$id
    rownames(c.vec)<-c.vec$id
    l.vec=l.vec[,!names(l.vec)%in%c("id","label","context")]
    c.vec=c.vec[,!names(c.vec)%in%c("id","label","context")]
    if (max(l.vec)<threshold){
        vec=l.vec+c.vec*join.factor
    }else{
        vec=l.vec
    }
    vec$id<-rownames(vec)
    return(vec)
}

parser = ArgumentParser()
parser$add_argument("-u", required=T,dest="ui.file",help="")
parser$add_argument("-m", required=TRUE,dest="mutant.file",help="")
parser$add_argument("-lv", dest="label.vec",help="label for mutants")
parser$add_argument("-cv", dest="context.vec",help="label for mutants")

parser$add_argument("-ou", dest="ui.output.file",help="Path to store results")
parser$add_argument("-om", dest="mutant.output.file",help="Path to store results")
#parser$add_argument("-o", dest="all.output.file",help="Path to store results")

aa=c("-u","data/ui_tag.txt","-m","data/l_mutants.rds","-cv","data/cluster100_context.rds","-lv","data/cluster100_labels.rds","-ou","data/ui_features.rds","-om","data/mutant_features.rds")
args = parser$parse_args()

ui=load_data(args$ui.file)
label.vec=load_data(args$label.vec)
context.vec=load_data(args$context.vec)
ui=ui[,c("id","label","context")]
print("ui vec")

ui.l.vec=merge(ui,label.vec,by.x="label",by.y="row.names")
print(dim(ui.l.vec))
ui.c.vec=merge(ui,context.vec,by.x="context",by.y="row.names")
print(dim(ui.c.vec))
ui.vec=join_vec(ui.l.vec,ui.c.vec)

mutants=load_data(args$mutant.file)
print("mutant vec")
mutants=mutants[,c("id","label","context")]
mutants.l.vec=merge(mutants,label.vec,by.x="label",by.y="row.names")
print(dim(mutants.l.vec))
mutants.c.vec=merge(mutants,context.vec,by.x="context",by.y="row.names")
print(dim(mutants.c.vec))
mutants.vec=join_vec(mutants.l.vec,mutants.c.vec)
ui.feature.table=ui.vec[!ui.vec$id %in% mutants.vec$id,]
print(dim(ui.feature.table))
save_data(ui.feature.table,args$ui.output.file)

mut.feature.table=mutants.vec
print(dim(mut.feature.table))
save_data(mut.feature.table,args$mutant.output.file)

#save_data(rbind(ui.feature.table,mut.feature.table),args$all.output.file)

#ui_train_test.R
require(argparse)
source("utils.R")

set.seed(42)
library(argparse)
require(plyr)
require(data.table)
source("utils.R")

parser = ArgumentParser()
parser$add_argument("-a", required=T,dest="api.file",help="")
parser$add_argument("-m", required=TRUE,dest="mutant.file",help="")

parser$add_argument("-oa", dest="api.output.file",help="Path to store results")
parser$add_argument("-om", dest="mutant.output.file",help="Path to store results")

#args = parser$parse_args(c("-i","/Users/kuznetsov/LAB/workspace/backstage/Backstage_MSR/results15.01/parallel/benign/data.csv","-o","/Users/kuznetsov/LAB/workspace/backstage/Backstage_MSR/results15.01/wordclouds_event"))
aa=c("-a","data/aligned_api.rds","-m","data/label_mutants.rds","-cv","data/cluster100_context.rds","-lv","data/cluster100_labels.rds","-o","data/label_features.rds")
args = parser$parse_args()

api=load_data(args$api.file)
print(dim(api))
api=api[,c("id","api")]
print("api")

mutants=load_data(args$mutant.file)
print("mutant")
print(dim(mutants))
mutants=mutants[,"id",drop=F]

api.feature.table=as.data.frame.matrix(sign(table(api$id,api$api)))
api.feature.table$id<-rownames(api.feature.table)

mut.feature.table=api.feature.table[api.feature.table$id %in% mutants$id,]
print(dim(mut.feature.table))
save_data(mut.feature.table,args$mutant.output.file)

api.feature=api.feature.table[!api.feature.table$id %in% mutants$id,]
print(dim(api.feature))
save_data(api.feature,args$api.output.file)


# calculate_dist.R
# calculate cosine distances
library(argparse)
source("utils.R")
require(proxy)

parser = ArgumentParser()
parser$add_argument("-lv", required=T,dest="label.file",help="ui csv file (or rds)")
parser$add_argument("-o", dest="output.file",help="Path to store results")
aa=c("-lv","data/label_vec.txt","-o","data/label_dist.rds")
args = parser$parse_args()
#switch buttons within activity

data=vec=read.table(file=args$label.file,quote="",head=T, sep=";")
print("loaded")
print(dim(data))
print(head(colnames(data)))
rownames(data)<-data[,1]
l.sim=simil(data[,-1],method="cosine")
save_data(as.matrix(l.sim),args$output.file)
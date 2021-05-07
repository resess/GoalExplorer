#make test
set.seed(43)
library(argparse)
source("utils.R")

parser = ArgumentParser()
parser$add_argument("-u", required=T, dest="data.file",help="ui csv file (or rds)")
parser$add_argument("-m", dest="mutants.file",help="")
parser$add_argument("-o", required=T, dest="output.file",help="Path to store results")
parser$add_argument("-n", type="integer", dest="n.test",help="")

args = parser$parse_args()
n.test=(args$n.test)#100
#switch buttons within activity
data.file=args$data.file
data=load_data(data.file)
mutants.file=args$mutants.file
mutants=load_data(mutants.file)

data.no.mutants=data[!data$id %in%mutants$id,]
test.data.id=sample(data.no.mutants$id, n.test)
test.data=data[data$id %in% test.data.id,]

out.file=args$output.file
if (grepl("rds$",out.file)){
    data=saveRDS(test.data,file=out.file)
}else{
    write.table(test.data,file=out.file,sep=";",row.names=F)
}

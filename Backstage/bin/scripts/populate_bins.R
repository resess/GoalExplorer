#populate bins
#data must contain id column (or id row names) and features only
require(argparse)
source("utils.R")
parser = ArgumentParser()
parser$add_argument("-b", required=TRUE,dest="bin.dir",help="Path to bin folder")
parser$add_argument("-o", required=TRUE, dest="out.dir",help="Path to bin data folder")
parser$add_argument("-f", required=TRUE,dest="data.file",help="Path to data file")
parser$add_argument("-m", required=TRUE,dest="mutants.file",help="")
parser$add_argument("-t", required=TRUE,dest="test.file",help="")
parser$add_argument("-s", action="store_true",dest="susi",help="")
aa=c("-b","data/susi_label_bin_id/","-o","data/susi_label_bin/","-f","data/label_features.rds","-m","data/label_mutants.rds","-t","data/test_data.rds")
args = parser$parse_args()

data.file = args$data.file
bin.id.dir = args$bin.dir
out.dir = args$out.dir
mutants.file = args$mutants.file
test.file = args$test.file
dir.create(out.dir, showWarnings = FALSE)
print("loading")
data=load_data(data.file)
print(dim(data))
mutants=load_data(mutants.file)
print(dim(mutants))
test=load_data(test.file)
#if("id" %in% names(data)){
    #rownames(data)<-data$id
    # data=data[,-grep("id",names(data)]
#}

bin.id.files=list.files(path=bin.id.dir,pattern=".*_bin_id.txt",full.names=T)
for (bin.id.file in bin.id.files){
    file.name = basename(bin.id.file)
    bin.name=sub("_bin_id.txt","",file.name)
    if (args$susi && bin.name != toupper(bin.name)) {
        next
    }
    print(file.name)
    bin.id = load_data(bin.id.file)
    ids=as.character(bin.id$id) #???
    bin.data=data[data$id %in% ids,,drop=F]
    if (nrow(bin.data)==0){
        next
    }
    print(dim(bin.data))
    mutant.bin.data=mutants[mutants$id %in% ids,,drop=F]
    print(dim(mutant.bin.data))
    if (nrow(mutant.bin.data)==0){
        next
    }
    mutant.bin.data$mutant<-1
    test.bin.data=bin.data[bin.data$id %in% test$id,,drop=F]
    print(dim(test.bin.data))
    if (nrow(test.bin.data)!=0){
        #next
    
    test.bin.data$mutant<-0
    }
    test.bin.data=rbind(test.bin.data,mutant.bin.data)
    train.bin.data=bin.data[!bin.data$id %in% test.bin.data$id,]
    train.bin.data$mutant<-2
    print(dim(test.bin.data))
    print(dim(train.bin.data))

    join.bin.data=rbind(test.bin.data,train.bin.data)
    join.bin.data.s=join.bin.data[,!names(join.bin.data)%in%c("id","mutant")]
    join.bin.data=join.bin.data.s[,colSums(join.bin.data.s)>0]
    fields=names(join.bin.data)
    train.bin.data=cbind(id=train.bin.data$id,m=train.bin.data$mutant,train.bin.data[,fields,drop=F])
    test.bin.data=cbind(id=test.bin.data$id,m=test.bin.data$mutant,test.bin.data[,fields,drop=F])
    write.table(train.bin.data,file=paste0(out.dir,"/",bin.name,"_train_bin.txt"),col.names=F,row.names=F,quote=F)
    write.table(test.bin.data,file=paste0(out.dir,"/",bin.name,"_test_bin.txt"),col.names=F,row.names=F,quote=F)

    meta = c("id:ignore.","m:ignore.")
    features = sapply(fields, function(x)paste0(gsub('[\\.: ,\\?\r\n=+&]', '_', x),':',"continuous."))# column names

    write.table(c(meta,features),file=paste0(out.dir,"/",bin.name,"_bin.fields"),row.names=F,col.names=F,quote=F)
}

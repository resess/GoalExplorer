#utils.R
options(stringsAsFactors=FALSE)
load_data<-function(data.file) {
    if (grepl("rds$",data.file)){
        data=readRDS(data.file)
    }else{
        data=read.table(data.file,head=T,sep=";", comment.char="")
    }
    return(data)
}
save_data<-function(data, data.file) {
    if (grepl("rds$",data.file)){
        saveRDS(data,file=data.file)
    }else{
        write.table(data,file=data.file,sep=";",row.names=F)
    }
}
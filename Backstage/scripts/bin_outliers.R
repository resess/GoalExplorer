#
# run orca inside bins
library("logging")
library("stringr")
require(argparse)
source("utils.R")
parser = ArgumentParser()
parser$add_argument("-b", required=TRUE,dest="bin.dir",help="")
parser$add_argument("-o", dest="out.dir",help="")
parser$add_argument("-tpref", dest="topic.col.prefix",default="topic",help="column for bins")
parser$add_argument("-t", dest="threshold",default="0.2",help="threshold for topics")
parser$add_argument("-s", nargs='+', dest="stages",help="stages 1-4")
args = parser$parse_args()
bin.dir = normalizePath(args$bin.dir)
root.dir=getwd()
dir.create(paste0(bin.dir,"/binary"))
orca.dir=paste0(root.dir,"/orca/")
orca.scale="-snone"#-snone sstd
orca.col.type = "continuous."
orca_disttype="-jaccard"
orca_knn=5
n_cores=5
n.folds=1
stage=as.numeric(args$stages)#c(4)
print(stage)
orca_AVERAGE=T
toPROBABILITY=F
doScale=F
nu=0.2
if(orca_AVERAGE){
        dtype="-avg"#FIXME: move to config
    }else{
        dtype="-kth"
    }
orca_use_weights ="-woff"
basicConfig()

#write(get_orca_dir(),file=paste0(orca_conf))
if (Sys.info()[["sysname"]]=="Darwin"){
    orca_exec= 'orca-mac'
    dprep_exec = 'dprep-mac'
}else if(Sys.info()[["sysname"]]=="Linux"){
    orca_exec='orca-linux'
    dprep_exec='dprep-linux'
}else{
    stop("Unsupported OS")
}
erf_ <- function(x){
    2 * pnorm(x * sqrt(2)) - 1
}

gaussian_cutoff <- function(data, base_data){
  o_mean = mean(base_data$score)
  o_sd = sd(base_data$score) 
  prob = sapply(data$score,function(x)max(0,erf_((x-o_mean)/(o_sd*sqrt(2)))))
  return(prob)
}

split_data_features_metadata <- function(data){
  meta_features_col = c(grep("^name|^m|^train|^id", names(data),fixed = F))
  data_is_metadata <- 1:ncol(data)
  features <- (data[, -meta_features_col])
  metadata <- data[, meta_features_col]
  return(list(features=features,metadata=metadata))
} 

parse_orca_output<-function(data, refdata, category, getweight=F, traindata=NA) {
    record_id="Record:"
    records=grep(record_id,data,value=T)
    #f_records=str_match_all(records,".*Record:\\s+(\\d+)\\s+Score:\\s+([0-9\\.]+)\\s+Neighbors:\\s((?:[0-9]+\\s*)+)")#(?:\\+e)?
    f_records=str_match_all(records,".*Record:\\s+(\\d+)\\s+Score:\\s+([0-9\\.]+)")
    #items=regmatches(t,gregexpr("[0-9]*\\.?[0-9]+(?:\\+e)*",t))
    records_frame=do.call(rbind.data.frame, f_records)
    records_frame=records_frame[,-1]
    if(nrow(records_frame)==0){
        records_frame=data.frame(matrix(nrow=0,ncol=2))
    }
    colnames(records_frame)<-c("sid","score")
    records_frame$score<-as.numeric(records_frame$score)
    scores=merge(records_frame,refdata,by.x="sid",by.y="row.names", all.y=T)    
    if (any(is.na(scores$id))){
        logerror("merge of orca scores and ref data was wrong")
    }
    
    #add missing values
    scores$score[is.na(scores$score)]=0
    if (any(scores$score>10000)){
        logwarn("smth wrong with orca, we have scores out of range OR it's euclidean")
        scores$score[scores$score>10000]<-orca_knn
    }
    #if (orca_AVERAGE){
        #scores$score<-scores$score/orca_knn# orca outputs sum of distances instead of average
        #scores$score<-scores$score#^(1/orca_knn)
    #}
    # if (toPROBABILITY){
    #     if (traindata==NA) traindata=scores$score
    #     scores$score<-gaussian_cutoff(scores$score,traindata)
    # }
    scores=cbind(scores,category=rep(category,nrow(scores)))
    scores=scores[order(scores$score,decreasing = T),]
    #barplot(sort(scores$score),main=category,ylim=c(0,1)) #if we want to plot scores
    if (getweight){
        res=scores[,c("id","category","score","m")]
        svar=var(scores$score)
        smean=mean(scores$score)
        weight=exp(1/smean)
        #auc=trapz(1:nrow(refdata),sort(bn[bn$malicious==0,]$score))/nrow(bn[bn$malicious==0,])
        attr(res,which="weight")<-weight
        return(res)
    } else{
        return(scores[,c("id","category","score","m")])
    }
}

########################

# if(!file.exists(base_dir)) {dir.create(base_dir,recursive=TRUE)}


library(doParallel)
cl = makeCluster(n_cores)
registerDoParallel(cl)

#orca stage
# curr_wd=getwd()
# tmp_dir=paste0(base_dir,"/tmp")
# dir.create(tmp_dir)
# launch dprep
if (1 %in% stage){
    print("*dprep*")
        command=paste0(orca.dir,dprep_exec)
        out_all=list()
        for(txt.file in dir(path=bin.dir,pattern="bin.txt$",full.names=T)){
            print(txt.file)
            fields.file=sub("txt$","fields",txt.file)
            fields.file=sub("((_train)|(_test))","",fields.file)
            data.file=txt.file
            # bin=sub("_train_bin.txt","",basename(data_file))
            binary.dir=paste0(bin.dir,"/binary/")
            binary.file=paste0(binary.dir,sub("txt$","bin",basename(txt.file)))
            orca.args=c("cd",orca.dir,"&&",command,data.file,fields.file,binary.file, orca.scale)
            out=system(paste0(orca.args,collapse=" "),ignore.stdout=F,intern=T,wait=T)
            weights.file=sub("((_train)|(_test))","",paste0(binary.dir,sub("txt$","weights",basename(txt.file))))
            file.rename(from=paste0(orca.dir,"/weights"),to=weights.file)
        }
}

if (2 %in% stage){
    cat("*orca*\n")
    maliciogram_dir=paste0(bin.dir,"/maliciogram/")
    dir.create(maliciogram_dir)
    
#foreach(fold=1:n.folds,.packages=c("stringr"))%:%{
    weights=list()
    #cat(paste0(orca_dir,"bin"))
    #maliciogram_list=
    # foreach(fold=1:n.folds,.packages=c("stringr"))%:%
    # foreach(train_file = dir(path=paste0(get_orca_dir(paste0(base_dir,fold)),"bin"),pattern="train\\.bin$",full.names=T),.packages=c("stringr"))%dopar%{
    #foreach(train_file = dir(path=paste0(bin.dir,"/binary/"),pattern="train_bin.bin$",full.names=T),.packages=c("stringr"))%do%{
        for(train_file in dir(path=paste0(bin.dir,"/binary/"),pattern="train_bin.bin$",full.names=T)){
            print(train_file)
        maliciogram_dir=paste0(bin.dir,"/maliciogram/")
        maliciogram_file=paste0(maliciogram_dir,"maliciogram.csv")
        command=paste0(orca.dir,orca_exec)
        scores_all=data.frame(matrix(nrow=0,ncol=4))            
        test_file=sub("train","test",train_file)
        weights_file=sub("bin$","weights",sub("_train","",train_file))
        category=sub("_train_bin.bin","",basename(train_file))
        #get train data scores
        ref_train_file=sub("/binary/","",sub("bin$","txt",train_file))
        ref_train=read.table(file=ref_train_file,head=F,sep=" ")
        ref_train=ref_train[,1:2]
        names(ref_train)<-c("id","m")
        orca_args=c(command,train_file,train_file,weights_file,orca_disttype,"-k",orca_knn,"-n",nrow(ref_train),dtype,orca_use_weights)
            #orca_args=c("cd",orca_dir,"&&",command,train_file,train_file,weights_file,orca_disttype,"-k",orca_knn-1,"-n",nrow(ref_train),dtype,orca_use_weights)
            #print(orca_args)        
        out=system(paste0(orca_args,collapse=" "),intern=T,wait=T)
        train_scores=parse_orca_output(out,ref_train,category,getweight=T)
        weights[[category]]=attr(train_scores,"weight")
        scores_all=setNames(rbind(scores_all,train_scores),c("id","category","score","m"))
            #cat(category,"=1\n")
        ref_test_file=sub("_train","_test",sub("/binary/","",sub("bin$","txt",train_file)))
        ref_test=read.table(file=ref_test_file,head=F,sep=" ")
        ref_test=ref_test[,1:2]
        names(ref_test)<-c("id","m")
        orca_args=c(command,test_file,train_file,weights_file,orca_disttype,"-k",orca_knn,"-n",nrow(ref_test),dtype,orca_use_weights)
        out=system(paste0(orca_args,collapse=" "),intern=T,wait=T)
        test_scores=parse_orca_output(out,ref_test,category, traindata=train_scores$score)
    
        scores_all=rbind(scores_all,test_scores)

        ##scores_all$score<-scores_all$score*weights[[category]]
        res=list(category=scores_all[,c("id","score")])
        names(res)<-category
        scores_file=paste0(maliciogram_dir,category,"_scores.csv")
        write.table(scores_all, file=scores_file,quote=F,col.names=F,row.names=F,sep=";")

        #res
    #     #cat(category,"train", nrow(ref_train), "test", nrow(fer_test) "weight ",weights[[category]],"\n")
    #     #row.names(scores_all)<-scores_all$name
    #     #scores_vector=scores_all[,"score",drop=F]
    #     #scores_vector=scores_vector*weights[[type]]
        #maliciogram[scores_all$name,category]<-scores_all$score*weights[[category]]
        #maliciogram[ref_train$name,"malicious"]<-ref_train$malicious
        #maliciogram[ref_test$name,"malicious"]<-ref_test$malicious
        #maliciogram[ref_train$name,"train"]<-1
        #maliciogram[ref_test$name,"train"]<-0
    }

}
if(3 %in% stage){
        maliciogram=data.frame()
        orca_dir=orca.dir#get_orca_dir(paste0(base_dir,fold))
        maliciogram_dir=paste0(bin.dir,"/maliciogram/")
        maliciogram_file=paste0(maliciogram_dir,"maliciogram.csv")
        for(train_file in dir(path=paste0(bin.dir,"/binary"),pattern="train_bin.bin$",full.names=T)){
            category=sub("_train_bin.bin","",basename(train_file)) 
            scores_file=paste0(maliciogram_dir,category,"_scores.csv")
            scores=read.table(file=scores_file,head=F,sep=";",stringsAsFactors=F)
            names(scores)<-c("id","category","score")
            scores=scores[scores$category==category,]  
            cat(category," ",dim(scores),"\n")
            ref_test_file=sub("_train","_test",sub("/binary","",sub("bin$","txt",train_file)))
            ref_test=read.table(file=ref_test_file,head=T,sep=" ",stringsAsFactors=F)
            ref_test=ref_test[,c(1,2)]
            names(ref_test)<-c("id","m")

            ref_train_file=sub("/binary","",sub("bin$","txt",train_file))
            ref_train=read.table(file=ref_train_file,head=T,sep=" ",stringsAsFactors=F)
            ref_train=ref_train[,c(1,2)]
            names(ref_train)<-c("id","m")
            #FIXME:
            #now moved from parse_
            if (toPROBABILITY){
                base_scores=scores[scores$id %in% ref_train$id,]
                scores$score<-gaussian_cutoff(scores,base_scores)
            }
            train_scores=scores[scores$id %in% ref_train$id,]
            smean=mean(train_scores$score)
            weight=exp(1/smean)
            #scores=maliciogram_list[[category]]
            if(orca_AVERAGE){
                scores$score<-scores$score/orca_knn
            }
            maliciogram[as.character(scores$id),category]<-scores$score*weight
            maliciogram[as.character(ref_train$id),"m"]<-ref_train$m
            maliciogram[as.character(ref_test$id),"m"]<-ref_test$m
            maliciogram[as.character(ref_test$id),"train"]<-0
            maliciogram[as.character(ref_train$id),"train"]<-1
        }
        maliciogram[is.na(maliciogram)]<-0
        maliciogram$name<-row.names(maliciogram)
        #write.table(scores_all, file=scores_file,quote=F,col.names=F,row.names=F,sep=";")
        write.table(maliciogram, file=maliciogram_file,quote=F,row.names=F,sep=";")
}

if (4 %in% stage){
    print("*classification*")
#TODO: clean orca and dpred executables
#TODO: add separate maliciogram construction from scores_all file
#do classification
library("kernlab")
library("e1071")
#    result<-
#foreach(fold=1:n.folds,.combine=rbind,.packages=c("kernlab","e1071","logging"))%dopar%{
        maliciogram_dir=paste0(bin.dir,"/maliciogram/")
        #get_maliciogram_dir(paste0(base_dir,fold))
        maliciogram_file=paste0(maliciogram_dir,"maliciogram.csv")
        maliciogram = read.csv(file=maliciogram_file, head=TRUE, sep=";")
        #ff=!colnames(maliciogram) %in% c("NO_SENSITIVE_SOURCE", "","","")#ACCOUNT_INFORMATION
        #print(ff)
        #maliciogram=maliciogram[,ff]
            #print(dim(maliciogram))
        train = split_data_features_metadata(maliciogram[maliciogram$train==1,])
        test = split_data_features_metadata(maliciogram[maliciogram$train==0,])
        train.data = train$features
        #cat(names(maliciogram))
        test.data=test$features
        test.metadata=test$metadata
        gamma_quantile=sigest(as.matrix(train.data),scaled=doScale,frac=1)
        gamma=mean(gamma_quantile)#gamma_quantile[[1]]#
        print(gamma)
        if (is.na(gamma)){
            gamma=0.1
            logerror(paste0("sigest got wrong values: ",paste0(gamma_quantile,collapse=" ")))
        }
        if (TRUE){
            #svm from e1071
            model = svm(train.data, type="one-classification",nu=nu, scale=doScale, gamma=gamma,kernel="radial")
            #print(model)
            predict_data = predict(model, test.data, decision.values = TRUE)
            decision_values = attributes(predict_data)$decision.values
            merged_results = cbind(test.metadata,predicted=predict_data,decision_values=decision_values)

        #add detection rate from virus total
        #merged_results = merge(merged_results,vt_data[,c("name","detection")], by="name",all.x=T)
            mispredicted = merged_results[(merged_results$m == 0 & merged_results$predicted == FALSE) | (merged_results$m == 1 & merged_results$predicted == TRUE),]
            false_positives = merged_results[(merged_results$m == 0 & merged_results$predicted == FALSE),]
            false_negatives = merged_results[(merged_results$m == 1 & merged_results$predicted == TRUE),]
            true_positives = merged_results[(merged_results$m == 1 & merged_results$predicted == FALSE),]
            true_negatives = merged_results[(merged_results$m == 0 & merged_results$predicted == TRUE),]
        }else{
            #svm from kernlab
            model = ksvm(as.matrix(train.data), kernel="rbfdot", nu=nu, scales=doScale, type="one-svc",kpar=list(sigma=gamma))
            #print(model)
            predict_data = predict(model, test.data)
            merged_results = cbind(test.metadata,predicted=predict_data)
            mispredicted = merged_results[(merged_results$malicious == 0 & merged_results$predicted == FALSE) | (merged_results$malicious == 1 & merged_results$predicted == TRUE),]
            false_positives = merged_results[(merged_results$malicious == 0 & merged_results$predicted == FALSE),]
            false_negatives = merged_results[(merged_results$malicious == 1 & merged_results$predicted == TRUE),]
            true_positives = merged_results[(merged_results$malicious == 1 & merged_results$predicted == FALSE),]
            true_negatives = merged_results[(merged_results$malicious == 0 & merged_results$predicted == TRUE),]
        }


        classification_dir=paste0(bin.dir,"/")
        #get_classification_dir(paste0(base_dir,fold))
        fp_file=paste0(classification_dir,"false_positives.csv")
        fn_file=paste0(classification_dir,"false_negatives.csv")
        write.table(false_positives[,names(test.metadata)],file=fp_file,row.names=F)
        write.table(false_negatives[,names(test.metadata)],file=fn_file,row.names=F)
        classification_file=paste0(classification_dir,"classification.txt")
        write.table(merged_results,file=paste0(classification_dir,"outliers.csv"),,row.names=F,sep=";")
        tp=nrow(true_positives)
        tn=nrow(true_negatives)
        fp=nrow(false_positives)
        fn=nrow(false_negatives)

        pos=tp+fn#length(which(test.metadata$malicious==0))
        neg=tn+fp#length(which(test.metadata$malicious==1))

        g=sqrt(tp/pos*tn/neg)
        acc=(tp+tn)/(pos+neg)
        tpr=tp/pos
        tnr=tn/neg

    #TODO: save 1) fp fn    
    #           2) g acc tpr tnr
    #           3)
        fileConn=file(classification_file)
        writeLines(c("g",g,"acc",acc,"tpr",tpr,"tnr",tnr,"TP", tp,"TN", tn, "FP", fp,"FN", fn), fileConn, sep=" ")
        close(fileConn)
        result=c(g,acc,tpr,tnr,tp,tn,fp,fn)
        print(result)
    #TODO: add writing of fpositives and fnegatives
    #TODO: create .combine function
    # list(model=model,
    #  predict_data=predict_data, 
    #  mispredicted=mispredicted, 
    #  false_positives=false_positives,
    #  false_negatives=false_negatives,
    #  true_positives=true_positives,
    #  true_negatives=true_negatives,
    #  neg_count=length(which(test.metadata$malicious==0)),
    #  pos_count=length(which(test.metadata$malicious==1)),
    #  all_test_values=merged_results
    #  )
    result=as.data.frame(result)
    if(ncol(result)==1)result=data.frame(t(result))
    #if (is.null(dim(result)))result=data.frame(t(result))
        colnames(result)<-c("g","acc","tpr","tnr","tp","tn","fp","fn")
    tp=mean(result$tp)
    tn=sum(result$tn)
    fp=sum(result$fp)
    fn=mean(result$fn)

    pos=tp+fn
    neg=tn+fp
    g=sqrt(tp/pos*tn/neg)
    acc=(tp+tn)/(pos+neg)
    tpr=tp/pos
    tnr=tn/neg

#aggregate cross-validation
    classification_file=paste0(bin.dir,"/classification_all.txt")
    #get_classification_file()
    fileConn=file(classification_file)
    writeLines(c("g",g,"acc",acc,"tpr",tpr,"tnr",tnr,"TP", tp,"TN", tn, "FP", fp,"FN", fn), fileConn, sep=" ")
#TODO: write all params?
    close(fileConn)

    all_res_file=paste0(bin.dir,"/results.txt")
    #get_result_file()
    sink(all_res_file, append=T)
    cat("knn",orca_knn,"nu",nu,"g",g,"acc",acc,"tpr",tpr,"tnr",tnr,"\n")
    sink()
    #cat(gid,get_ids(),"knn",orca_knn,"nu",nu,"g",g,"acc",acc,"tpr",tpr,"tnr",tnr,"\n")
}

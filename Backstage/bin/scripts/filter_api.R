#old method
source("utils.R")
doclean<-function(d){
    #d=filt_api_act(d)# for life-cycle methods only
    d$api<-sub("(^<.*:.*)\\((.*android\\.net\\.Uri [a-zA-Z_]*),.*\\)>","\\1:\\2",d$api)#d=extract_uri(d)
    #d=std_ui(d)# currently concider buttons only, look into standart_ui.txt
    ##d=freq_api(d) 
    ud=unique(d[,c("apk","api")])
    apif=table(ud$api)
    fapi=names(apif[apif>1])
    d=d[d$api %in% fapi,]
    ##
    #d=freq_event(d)# no use
    return(d)   
}

group_log<-function(x) {
    res=gsub("^<?android\\.util\\.Log:.*","android.util.Log",x)
}
trim_api<-function(x) {
    res=gsub("[<>]","",x)
    return(res)
}
remove_return<-function(x) {
    res=gsub("(.*): .*? (.*)","\\1: \\2",x)
    return(res)
}
remove_support<-function(x) {
    res=sub("support\\.v[0-9].","",x)
    return(res)
}
remove_params<-function(x) {
    res=gsub("(.*)\\(.*","\\1",x)
    return(res)
}

group_by_name<-function(x) {
    res=gsub(".* (\\w)","\\1",x)
    return(res)
}

group_by_class<-function(x) {
    res=gsub("<?(.*):.*","\\1",x)
    return(res)
}
group_by_package<-function(x) {
    cn=group_by_class(x)
    #cn=sub("^<(android\\.[^\\.]*).*","\\1",cn)
    cn=sub("(android\\.[^\\.]*(\\.(pm|res|admin|storage|tts))?).*","\\1",cn)
    cn=sub("(com\\.android\\.internal\\.telephony).*","\\1",cn)
    cn=sub("(com\\.google\\.android.\\gms[^\\.]*).*","\\1",cn)
    cn=sub("(com\\.google\\.android.\\maps[^\\.]*).*","\\1",cn)
    cn=sub("(org\\.apache\\.http).*","\\1",cn)
    return(cn)
}

require(argparse)
parser = ArgumentParser()
parser$add_argument("-a", required=TRUE, dest="api.file",help="")
parser$add_argument("-o", required=TRUE, dest="output",help="out")
parser$add_argument("-d", required=TRUE, type="integer", dest="depth",help="depth")
#parser$add_argument("-f",  nargs="+" dest="filter", help="api ")
parser$add_argument("-c", dest="category",action="store_true", help="use categories")
parser$add_argument("-r", nargs="+",dest="rules", help="name, class, package, return, params")
#args = parser$parse_args(c("-a","api","-o","api_filtr"))
args = parser$parse_args()

depth=args$depth

api.file=args$api.file
if (grepl("rds$",api.file)){
    api=readRDS(api.file)
}else{
    api=read.table(api.file,head=T,sep=";")
}

#filter depth
print("api dim")
print(dim(api))
api=api[api$depth<=depth,]
api=unique(api[,names(api)!="depth",])
head(api)
print("after depth")
print(dim(api))
#api[api$api=="NOAPI",]$category<-"NOAPI"

api$api<-trim_api(api$api)
api$api<-group_log(api$api)
api$api<-remove_support(api$api)

if (args$category){
    exclude.cat.list=c("NAN","","NO_CATEGORY", "NO_API")
    api=api[!api$category %in% exclude.cat.list,]
}
rules=args$rules
if (!any(is.na(rules))){
    rules.list=c("return","params","class","method","package")
    stopifnot(rules %in% rules.list)
    if ("return" %in% rules){
        api$api<-remove_return(api$api)
    }
    if ("params" %in% rules){
        api$api<-remove_params(api$api)
    }
    if ("class" %in% rules){
        api$api<-group_by_class(api$api)
    }else
    if ("name" %in% rules){
        api$api<-group_by_name(api$api)
    }else
    if ("package" %in% rules){
        api$api<-group_by_package(api$api)
    }
}
api$apk<-sub("\\.apk$","",api$apk)
print(dim(api))
api=unique(api)
print(dim(api))
out.file=args$output
save_data(api,out.file)


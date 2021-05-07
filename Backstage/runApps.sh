#please install http://www.gnu.org/software/parallel/ before using it

FOLDER=/path/to/apks/

runApp() {
    bash runApp.sh $FOLDER/$1
}

export -f runApp

ls $FOLDER/*.apk | parallel -j $1 --timeout 1800 runApp
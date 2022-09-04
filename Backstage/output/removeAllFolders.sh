for i in `ls -A $1`;
do
    if ! [ $i == "appSerialized.txt" ] && ! [ $i == "images" ] ;then
        rm -rf $1/$i;
    fi
done

#!/bin/bash
set -o noglob

wait_output_folder(){
    output_dir="$(dirname $1)" #split for folder name
    echo "Waiting for connection file in folder $output_dir"
    #output_dir = $(basename $1)1 #split for
    inotifywait $output_dir -e create --quiet |
        #while read /$(basename $1 .apk)-output
        #if the file corresponds to output dir
            adb logcat | grep "OPENCONNECTION CALLED" >> $output_dir/reached_connection.txt

        #output_dir=#the apk name strip .apk + output
        #adb logcat | grep "OPENCONNECTION CALLED" | tee -a $output_dir/reached_connection.txt 2>&1
        #(head -n 1 $output_dir/reached_connection.txt && (cut -d ":" -s -f 4- $output_dir/reached_connection.txt | sort -u)) > $output_dir/reached_connection2.txt
}

run_test(){
    if [ "$6" -eq "1" ]; then
        echo "Killing stoat server running at $5"
    fi
}

run_app (){
    #if [ "$6" -eq "1" ]; then
    #    adb kill-server
    #    adb start-server
    #fi
    if [ "$6" -eq "1" ]; then
        #deal with kill later
        adb start-server
    fi
    echo "Killing stoat server running at $5"
    lsof -i:$5 | grep $(whoami) | awk '{print $2}' | xargs kill -9
    #ps aux | grep $(whoami) | grep ServerNew.jar | awk '{print $2}' | xargs kill -9
    echo "Config path '$7'"
    ruby bin/run_stoat_testing.rb \
        --avd_name $1 \
        --apk_path $2 \
        --stg $3 \
        --avd_port $4 \
        --stoat_port $5 \
        --config_apk_path "$7"
    #&
    # shellcheck disable=SC1072
    #if [ -n "$6"]; then
    #    wait_output_folder $2 &
    #fi
}

#run_app $1 $2 $3 $4 $5 $6 $7


#ls $2/*.apk | xargs --max-procs=4 -I apk -n 1 bash -c run_app "goalexplorer" apk "$(basename apk)_stg.xml"
#for apk in $2/*
    #if [[ $apk == *.apk ]]
        #for i in {1..$instances}
        #do
i=$4 #
#avd_name="goalexploreravd$i"
avd_name="Test_AVD"$i
j=$((2*$i))
avd_port=$((5560 + $j)) #need to be even 5560 + (2*$i)
stoat_port="200$i"
echo "Running tool for $1 on $avd_name with ports avd: $avd_port and stoat $stoat_port"
echo "Config path $3"
echo "Running tool for $1 on $avd_name with ports avd: $avd_port and stoat $stoat_port" >&2
run_app $avd_name $1 $2 $avd_port $stoat_port $i $3
        #done

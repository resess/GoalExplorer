#!/bin/bash

#Timestamp function
timestamp() {
    date +"%s"
}

start_time=$(timestamp)

output_dir=$2
adb -s $1 logcat -v epoch | grep "OPENCONNECTION CALLED" | awk '{t=$1;$1=$NF;$NF=t}1' > $output_dir/tmp.txt
echo "DONE WITH LOGCAT RUN"
#| sed "s/^/$(start_time) $(timestamp) /"
(cut -d ":" -s -f 2- $output_dir/tmp.txt | awk -v var="$start_time" '{print $1,$2,$3,$4-var}' | sort -u -k3,3) >> $output_dir/reached_connection.txt
#(cut -d ":" -s -f 4- $output_dir/tmp.txt | sort -u) >> $output_dir/reached_connection.txt
#rm $output_dir/tmp.txt
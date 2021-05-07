STOAT_PATH=/home/bowenluo/workspace/goalexplorer/implementation/Executor/Stoat

ruby $STOAT_PATH/a3e/bin/rec.rb \
    --avd=stoat \
    --apk=$STOAT_PATH/a3e/test/apk/"$1".apk \
    --dev=emulator-5554 \
    --port=2000 \
    --no-rec -loop \
    --search=weighted \
    --events=1000 \
    --event_delay=200 \
    --stg=$STOAT_PATH/a3e/test/stg/"$1"_stg.xml
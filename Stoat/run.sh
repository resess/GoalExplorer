# Paths that Stoat relies on
#sudo --preserve-env=PATH ruby bin/run_stoat_testing.rb --app_dir ~/workspace/ReCDroid/Evaluation\ Result/apks/1.newsblur-v6.10_debug.apk --force_create
#ruby bin/run_stoat_testing.rb --app_dir ~/workspace/ReCDroid/Evaluation\ Result/apks/1.newsblur-v6.10_debug.apk --force_create
#ruby bin/run_stoat_testing.rb --app_dir ~/workspace/apks/AnyMemo_debug.apk --retrace_steps ~/workspace/2019_android_targeted_exploration/implementation/Executor/Stoat/steps.txt --stg /home/fizzer/workspace/2019_android_targeted_exploration/implementation/Executor/Stoat/testapp.xml

#export ANDROID_HOME=/home/fizzer/Android/Sdk
#export ANDROID_AVD_HOME=/home/fizzer/.android/avd
#
#export PATH=$PATH:$ANDROID_HOME/tools
#export PATH=$PATH:$ANDROID_HOME/tools/bin
#export PATH=$PATH:$ANDROID_HOME/platform-tools
#export PATH=$PATH:$ANDROID_HOME/build-tools/25.0.0
#export PATH=$ANDROID_HOME/emulator:$ANDROID_HOME/tools:$PATH

ruby bin/run_stoat_testing.rb \
    --avd_name stoat \
    --apk_path /home/bowenluo/workspace/goalexplorer/implementation/Executor/Stoat/a3e/test/apk/a2dp-debug.apk \
    --stg /home/bowenluo/workspace/goalexplorer/implementation/Executor/Stoat/a3e/test/stg/a2dp-debug_stg.xml

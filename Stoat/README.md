# Installation Instructions
This package will work on Linux system. There are many binaries that the scripts rely on so there are no guarantees on other platforms. 
1. Install Ruby
2. Install [Nokogiri](https://nokogiri.org/tutorials/installing_nokogiri.html)
3. Install Python 2.7
4. Install [uiautomator](https://github.com/xiaocong/uiautomator)

You will need Android SDK. This will work best if you install it through [Android Studio](https://developer.android.com/studio).
Through either Android Studio or the [sdkmanager](https://developer.android.com/studio/command-line/sdkmanager) binary, install the following packages.
1. build-tools;25.0.0 (Android SDK Build-Tools 25.0.0)
2. android-19 (Android 4.4 API level 19)
3. Android Emulator
4. Android SDK platform-tools

Then add the following lines to your .bashrc/$PATH after you find where your Android SDK had been [installed](https://stackoverflow.com/questions/25176594/android-sdk-location). 

```shell script
export ANDROID_HOME = <Android SDK Path>

export PATH=$PATH:$ANDROID_HOME/tools
export PATH=$PATH:$ANDROID_HOME/tools/bin
export PATH=$PATH:$ANDROID_HOME/platform-tools
export PATH=$PATH:$ANDROID_HOME/build-tools/25.0.0
```

# Trouble Shooting Instructions

Make sure that the following scripts are executable
- waitForEmu.sh
- setupEmu.sh
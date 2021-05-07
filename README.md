# GoalExplorer

This repository hosts GoalExplorer which automatically triggers the functionality of interest in an apk. 
The core idea behind GoalExplorer is to first statically model the application UI screens and transitions between these screens, producing a Screen Transition Graph (STG). 
Then GoalExplorer uses the STG to guide the dynamic exploration of the application to the particular target of interest: an Android activity, API call, or a program statement.

This repository contains the source code for the application to produce STGs for android applications along with the tool 
used to dynamically explore to find the target of interest. 

## Table of Contents
1. [Pre-requisites](#pre-requisites)
2. [Building The Tool](#Building-The-Tool)
3. [Using The Tool](#Using-The-Tool)
---


## Pre-requisites
The tool is only support to run on a Linux environment
* Install the Android SDK and build tools: https://developer.android.com/studio/intro/update
    * Make sure that `$ANDROID_HOME` is set to the SDK directory 
    * Make sure that the following directories is added to `$PATH`
        * `$ANDROID_HOME/tools`
        * `$ANDROID_HOME/tools/bin`
        * `$ANDROID_HOME/platform-tools`
        * `$ANDROID_HOME/build-tools/<installed version>`
* Install Java
* Install Python3
    * Install the [uiautomator](https://github.com/xiaocong/uiautomator) package
* Install Ruby 
    * Install the [Nokogiri](https://nokogiri.org/tutorials/installing_nokogiri.html) gem
---


## Building The Tool
The application to build a STG uses modified versions of [Backstage](https://github.com/uds-se/backstage) and 
[FlowDroid](https://github.com/secure-software-engineering/FlowDroid). You need to first build these modules before 
building GoalExplorer. 

### Example Commands
```
# Build Backstage
cd Backstage/
mvn -DskipTests clean install 
cd .. 

# Build FlowDroid
cd FlowDroid/
mvn -DskipTests clean install
cd .. 

# Build GoalExplorer 
cd GoalExplorer/ 
mvn -DskipTests clean package
```
---


## Using The Tool 
To build a STG run the generated `.jar` file which should be located at 
`GoalExplorer/target/`. 

### Command
```
java -jar {JAR_PATH} ge [OPTIONS] [-cb <arg>] [-d] [-h] -i <arg> 
          [-l <arg>] [-o <arg>] [-s <arg>] [-t <arg>] [-v]
```

### Options
```
  usage: ge [OPTIONS] [-cb <arg>] [-cg <arg>] [-d] [-h] -i <arg> 
            [-l <arg>] [-o <arg>] [-s <arg>] [-t <arg>] [-v]
   -cb <arg>           the maximum number of callbacks modeled for each
                       component (default to 20)
   -d,--debug          debug mode (default disabled)
   -h,--help           print the help message
   -i,--input <arg>    input apk path (required)
   -l,--api <arg>      api level (default to 23)
   -o,--output <arg>   output directory (default to "sootOutput")
   -s,--sdk <arg>      path to android sdk (default value can be set in
                       config file)
   -t <arg>            maximum timeout during callback analysis in seconds
                       (default: 60)
   -v,--version        print version info
```

An STG for the android application will be generated in the output directory. 
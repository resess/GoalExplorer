ANDROID_JAR=libs/android.jar
CURRENT_DIR=.
JDK=/usr/bin
file=$1

appName=`basename ${file} .apk`
export fileName=$(basename $file)
cd $CURRENT_DIR/output
$JDK/java -jar apktool_2.1.1.jar -s -f d $file
cd $CURRENT_DIR

echo "Running analysis for $fileName"
$JDK/java -Xmx40g -Xss5m -cp target/Backstage-5.1-SNAPSHOT-jar-with-dependencies.jar st.cs.uni.saarland.de.testApps.TestApp -apk ${file} -androidJar $ANDROID_JAR -apkToolOutput output/$appName -rAnalysis -uiTimeoutValue 30 -uiTimeoutUnit SECONDS -rTimeoutValue 30 -rTimeoutUnit SECONDS -maxDepthMethodLevel 15 -numThreads 24 -rLimitByPackageName
echo "Analysis for $fileName finished"

# cd $CURRENT_DIR/output
# bash removeAllFolders.sh $appName
# cd $CURRENT_DIR



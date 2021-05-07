# 1 - serialized results, 2 - output file path and prefix
java -cp ../target/Backstage-5.1-SNAPSHOT-jar-with-dependencies.jar st.cs.uni.saarland.de.saveData.ApiResultsProcessor -i $1 -o $2

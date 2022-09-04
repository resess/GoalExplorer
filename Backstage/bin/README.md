# Backstage - Detecting Behavior Anomalies in Graphical User Interfaces

An official web-page of the Backstage project can be found [here](https://www.st.cs.uni-saarland.de/appmining/backstage/).

## UI analysis phase

### Prerequiesites:
1. Java 8
2. Maven


### Building:
```bash
$ mvn initialize
$ mvn package
```
### Usage:
The script below only works on Linux and MacOS systems. If you want to run it on Windows, just examine the `runApp.sh` file and run `apktool` and `backstage.jar` manually.
```bash
./runApp.sh PATH_TO_APK/myApp.apk
```

### Results
The tool produces:
* `appSerialized.txt` file in `output/<name_of_apk>` folder with the UI model
* `<name_of_apk>_forward_apiResults_1.xml` with the mappting of callbacks to APIs

Those files are needed to obtain the mapping between UI elements and APIs later on. 

## Generating data for the next phase

```bash
cd scripts
./ui_extraction.sh output <output_file_prefix>
./api_extraction.sh results <output_dir>
```


## Detecting outliers phase 

### Prerequiesites:
1. R
2. Python 3
3. pip3 
4. Linux or MacOS

### Before you start
Before running the scripts you need to follow the steps below:
#### Open `R` console:
```R
install.packages("logging", dependencies=TRUE)
install.packages("stringr", dependencies=TRUE)
install.packages("argparse", dependencies=TRUE)
slam_link="https://cran.r-project.org/src/contrib/Archive/slam/slam_0.1-37.tar.gz"
install.packages(slam_link, repos = NULL, type="source")
install.packages("skmeans", dependencies=TRUE)
install.packages("cluster", dependencies=TRUE)
install.packages("clue", dependencies=TRUE)
install.packages("doParallel", dependencies=TRUE)
install.packages("data.table", dependencies=TRUE)
install.packages("proxy", dependencies=TRUE)
```
####  Make sure you have `Python v3` and `pip v3` installed and: 
```bash
pip3 install argparse
pip3 install numpy
pip3 install pandas
pip3 install webcolors
pip3 install gensim
pip3 install nltk
pip3 install spacy
python3 -m spacy.en.download
```
Finally, open `python3` console:
```python
import nltk
nltk.download()
```
and download the following packages:
* wordnet
* stopwords
* words

#### Obtaining a raw data
In order to run a mutation analysis you need to obtain a raw data with information about UI elements, their labels and APIs.
Please donwload a [backstage_data.zip](https://www.st.cs.uni-saarland.de/~avdiienko/files/backstage_data.zip), unzip it to the script folder and put the reference to it via `$SNAP_DIR` variable inside the `launch.sh` script.

The zip-archive contains a `data` folder inside. Ideally, you should move it to the `scripts` folder and the `$SNAP_DIR` is already referencing it.

### Run the analysis
```bash
cd scripts
./launch.sh
```

### Reproducing results from tables V, VI and VII from the [Technical Report](https://www.st.cs.uni-saarland.de/appmining/backstage/backstage_tech_report.pdf)

Mutation procedure takes place only in the middle of the whole analysis. Thus, you don't need to rerun the whole script in order to try different mutation techniques.

Open the `launch.sh` file in your favourite text editor and find the line with invocation of `make_mutants.R`.
By default we use `high distance` mode, but you can also make outliers based on `random` choice or `crossover label mutations`.
You can specify a desired mode by using `-t` parameter in `make_mutants.R` file. Possible values are:
* dist - stands for `high distance` mode
* rand - stands for `random` mode
* cross - stands for `crossover label mutations`

You need to run the whole analysis only once. Next time, you can skip all steps up to the place when mutations take place.

### Results of mutation
You can inspect mutation results in `$DATA_DIR/top_bin/results.txt` folder.

#launch
DATA_DIR=data_sens_main
SNAP_DIR=data
rm -rf $DATA_DIR
mkdir $DATA_DIR

Rscript filter_api.R -a $SNAP_DIR/api_10.csv -o $DATA_DIR/api_5c.rds -d 5 -r return params

Rscript filter_ui.R -u $SNAP_DIR/ui_unique.rds -o $DATA_DIR/clean_ui.rds -f like_buttons
Rscript merge_api_ui.R -a $DATA_DIR/api_5c.rds -u $DATA_DIR/clean_ui.rds -oa $DATA_DIR/new_api.rds -ou $DATA_DIR/new_ui.txt
python3 tag_lemma.py -i $DATA_DIR/new_ui.txt -o $DATA_DIR/ui_tag.txt -v $DATA_DIR/label_vec.txt

Rscript calculate_dist-1.R -lv $DATA_DIR/label_vec.txt -o $DATA_DIR/label_dist.rds -u $DATA_DIR/ui_tag.txt
Rscript align_api.R -u $DATA_DIR/ui_tag.txt -a $DATA_DIR/new_api.rds -oa $DATA_DIR/aligned_api.rds

Rscript cluster_vec.R -u $DATA_DIR/ui_tag.txt -v $DATA_DIR/label_vec.txt -o $DATA_DIR/cluster100_words.txt -m $DATA_DIR/clust100.rds -n 250 -cm 1.1
Rscript infer_clusters.R -u $DATA_DIR/ui_tag.txt -v $DATA_DIR/label_vec.txt -c $DATA_DIR/cluster100_context.rds -l $DATA_DIR/cluster100_labels.rds  -m $DATA_DIR/clust100.rds -f $DATA_DIR/cluster100_all.rds

Rscript make_mutants.R -u $DATA_DIR/ui_tag.txt -o $DATA_DIR/ui_mutants.rds -n 5000 -d $DATA_DIR/label_dist.rds -t dist

Rscript make_test.R -u $DATA_DIR/ui_tag.txt -m $DATA_DIR/ui_mutants.rds -n 5000 -o $DATA_DIR/test_data.rds
Rscript make_feature_table_api.R -a $DATA_DIR/aligned_api.rds -m $DATA_DIR/ui_mutants.rds -oa $DATA_DIR/api_features.rds -om $DATA_DIR/mutant_features.rds
Rscript make_feature_table_ui.R -u $DATA_DIR/ui_tag.txt -m $DATA_DIR/ui_mutants.rds -cv $DATA_DIR/cluster100_context.rds -lv $DATA_DIR/cluster100_labels.rds -ou $DATA_DIR/ui_topics.rds -om $DATA_DIR/mutant_topics.rds

rm -rf $DATA_DIR/top_bin_id
rm -rf $DATA_DIR/top_bin

Rscript make_bins_from_topics.R -i $DATA_DIR/ui_topics.rds -o $DATA_DIR/top_bin_id -m $DATA_DIR/mutant_topics.rds -t 0.5
Rscript populate_bins.R -f $DATA_DIR/api_features.rds -b $DATA_DIR/top_bin_id/ -o $DATA_DIR/top_bin/ -m $DATA_DIR/mutant_features.rds -t $DATA_DIR/test_data.rds
Rscript bin_outliers.R -b $DATA_DIR/top_bin/ -s 1 2 3 4

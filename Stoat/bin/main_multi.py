import os
import sys
import time
import datetime
import subprocess
import shutil

REPEATED_RUNS = 2
# PARALLEL_RUNS = 4
apk_path = sys.argv[1]
REPEATED_RESULTS_DIR = sys.argv[2]
APK_OFFSET = 0
MODEL_TIME = sys.argv[3] # minutes
MCMC_TIME = sys.argv[4] # minutes
AVD_NAME = sys.argv[5]
AVD_PORT = sys.argv[6]
STOAT_PORT = sys.argv[7]

# def chunkList(lst, n):
# 	return [lst[i::n] for i in xrange(n)]

if __name__ == "__main__":
	"""
	For batch processing APKs. 2 hour per APK
	All intermediate output are saved under the instrumented app folder
	"""
	start = 1
	for i in range(start, REPEATED_RUNS + start):

		instrumented_app_dirs = []
		ith_apk = 0
		total_app = len(os.listdir(apk_path))

		for dir in os.listdir(apk_path):
			dir_path = apk_path + dir
			if os.path.isdir(dir_path) and not dir_path.endswith('_output'):
				target_dir = REPEATED_RESULTS_DIR + "/" + str(i) + "/" + dir_path.split("/")[-1]
				if os.path.exists(target_dir):
					raise ValueError("Error: results folders already exist! Not overwritten...")
				elif ith_apk >= APK_OFFSET:
					instrumented_app_dirs.append(dir_path)
				ith_apk += 1
			elif dir.endswith('.apk'):
				target_dir = REPEATED_RESULTS_DIR + "/" + str(i) + "/" + dir_path.split("/")[-1]
				if ith_apk >= APK_OFFSET and (not os.path.exists(target_dir)):
					instrumented_app_dirs.append(dir_path)
				ith_apk += 1

		print "Will work on apps:", instrumented_app_dirs

		# test_apps = chunkList(instrumented_app_dirs, PARALLEL_RUNS)

		for app_dir in instrumented_app_dirs:
			print "### Working on:", app_dir
			shutil.rmtree(app_dir+"/stoat_fsm_output", ignore_errors=True)
			shutil.rmtree(app_dir+"/coverage", ignore_errors=True)
			shutil.rmtree(app_dir+"/stoat_mcmc_sampling_output", ignore_errors=True)
			os.system("timeout " + str(MODEL_TIME+MCMC_TIME) + "m ruby run_stoat_testing.rb --app_dir " + app_dir + \
				" --avd_name " + AVD_NAME + " --avd_port " + str(AVD_PORT) + " --stoat_port " + STOAT_PORT + " --project_type apk --model_time " + \
				str(MODEL_TIME) +"m --mcmc_time " + str(MCMC_TIME) +"m")
			print "2 hour timeout. Will work on next app ..."

			target_dir = REPEATED_RESULTS_DIR + "/" + str(i) + "/" + app_dir.split("/")[-1]
			os.system("mkdir -p " + target_dir)
			os.system("cp -r " + app_dir + "/stoat_fsm_output " + target_dir)
			os.system("cp -r " + app_dir + "/coverage " + target_dir)
			os.system("cp -r " + app_dir + "/stoat_mcmc_sampling_output " + target_dir)

	print "### All Done."

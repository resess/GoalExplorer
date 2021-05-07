import os
import subprocess
import datetime
import time
import csv
import sys
import re

def getPackageActivity(emalator_serial):
    adb_output = subprocess.check_output(["adb","-s",emalator_serial,"shell", "dumpsys", "window", "windows", "|", "grep", "-E", "\'mCurrentFocus\'"])
    regex_search_output = re.search('(u0\s)(.*)(\/)(.*)(\})', str(adb_output))
    if (regex_search_output is None):
        regex_search_output = getFacebook(emalator_serial)
    try:
        package_name = regex_search_output.group(2)
        activity_name = regex_search_output.group(4)
        return package_name, activity_name
    except AttributeError, e:
        return "",""

def getFacebook(emalator_serial):
    adb_output = subprocess.check_output(["adb","-s",emalator_serial,"shell","dumpsys","window", "windows", "|", "grep", "-E", "\'mFocusedApp\'"])
    regex_search_output = re.search('(u0\s)(.*)(\/)(.*)(\s)', str(adb_output))
    return regex_search_output

package_name = sys.argv[1]
emalator_serial=sys.argv[2]

with open('/data/duling/TestingTools/results/rawresult/'+package_name+'.txt', 'a+') as f:
    covered_act = set()
    last_activity_name = ''
    while(True):
        package_activity = getPackageActivity(emalator_serial)
        if (package_activity[0]=="" or package_activity[1]==""):
            f.writelines('Error retriving activity name: ' + datetime.datetime.now().strftime("%H:%M:%S") + '\n')
            f.flush()
        elif package_activity[1].startswith('.'):
            continue
        else:
            activity_name = package_activity[1].split()[0]
            if not ('com.android.launcher' in activity_name):
                if not (activity_name == last_activity_name):
                    last_activity_name = activity_name
                    if not (activity_name in covered_act):
                        covered_act.add(activity_name)
                    f.writelines(str(len(covered_act)) + ', ' + activity_name + ', ' + datetime.datetime.now().strftime("%H:%M:%S") + '\n')
                    f.flush()


#!/usr/bin/python

from uiautomator import Device
import sys


# get the target device
d = Device(sys.argv[1])

# click  
instance_num = d(className=sys.argv[2]).count
if instance_num > int(sys.argv[3]):
	d(className=sys.argv[2],instance = int(sys.argv[3])).click()
elif instance_num == int(sys.argv[3]):
	d(className=sys.argv[2],instance = (int(sys.argv[3])-1)).click()
else:
	print("Could not find classname instance" + str(sys.argv[2])+ " "+str(sys.argv[3])+" among "+ str(instance_num) + " instances")
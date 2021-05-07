#!/usr/bin/python

from uiautomator import Device
import sys
import random


# get the target device
d = Device(sys.argv[1])

# edit text
if d(className=sys.argv[2]).count > int(sys.argv[3]):
	d(className=sys.argv[2],instance=int(sys.argv[3])).set_text(sys.argv[4])










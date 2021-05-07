#!/usr/bin/python

from uiautomator import Device
import sys
import random


# get the target device
d = Device(sys.argv[1])

# edit text
if d(text=sys.argv[2]).exists:
	d(text=sys.argv[2]).set_text(sys.argv[3])










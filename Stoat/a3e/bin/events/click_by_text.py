#!/usr/bin/python

from pydoc import classname
from uiautomator import Device
import sys

# get the target device
d = Device(sys.argv[1])

# check whether the text exists
if d(text=sys.argv[2]).exists:
	d(text=sys.argv[2]).click()
else:
	print("Could not find instance by text "+sys.argv[2])

#d(resourceId).child(clickable).click()
#d(resourceId).child_by_text(text, clickable).click()
#d(resourceId)[pos].child(clickable).click()







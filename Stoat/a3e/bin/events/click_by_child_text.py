#!/usr/bin/python

from pydoc import classname
from uiautomator import Device
import sys

# get the target device
d = Device(sys.argv[1])

# check whether the text exists
if d(className=sys.argv[2]).child(clickable=True).child_by_text(sys.argv[3]).exists:
	print("Clicking on the element with text")
	#print(d(className="android.widget.ListView").child(clickable=True).child_by_text("french-body-parts.db").info)
	#print(d(className="android.widget.ListView").child_by_text("french-body-parts.db", clickable=True).info)
	d(className=sys.argv[2]).child(clickable=True).child_by_text(sys.argv[3]).click()
else:
	print("Could not find instance by text "+sys.argv[3])

#d(resourceId).child(clickable=True).click()
#d(resourceId).child_by_text(text, clickable=True).click()
#d(resourceId)[pos].child(clickable).click()







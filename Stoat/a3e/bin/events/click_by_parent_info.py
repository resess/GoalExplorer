#!/usr/bin/python

from pydoc import classname
from uiautomator import Device
import sys

# get the target device
d = Device(sys.argv[1])

# check whether the text exists
if d(resourceId=sys.argv[2]).exists and d(resourceId=sys.argv[2]).child(clickable=True).exists:
    #Gets clickable child with itself or a granchild containing given text
    print(d(resourceId=sys.argv[2]).info)
    if sys.argv[4] and d(resourceId=sys.argv[2]).child_by_text(sys.argv[4], clickable=True).exists:
        print(d(resourceId=sys.argv[2]).child_by_text(sys.argv[4], clickable=True).info)
        print("Clicking on the element with res id and text")
        #print(d(className="android.widget.ListView").child(clickable=True).child_by_text("french-body-parts.db").info)
        #print(d(className="android.widget.ListView").child_by_text("french-body-parts.db", clickable=True).info)
        d(resourceId=sys.argv[2]).child_by_text(sys.argv[4], clickable=True).click()
    else:
        #Gets the first clickable child
        #print(d(resourceId=sys.argv[2]).child_by_instance(2, clickable=True).info)
        d(resourceId=sys.argv[2]).child(clickable=True).click()
elif d(className=sys.argv[3]).exists and d(className=sys.argv[3]).child(clickable=True).exists:
    if sys.argv[4] and d(className=sys.argv[3]).child_by_text(sys.argv[4], clickable=True).exists:
        print("Clicking on the element with classname and text")
        #print(d(className="android.widget.ListView").child(clickable=True).child_by_text("french-body-parts.db").info)
        #print(d(className="android.widget.ListView").child_by_text("french-body-parts.db", clickable=True).info)
        d(className=sys.argv[3]).child_by_text(sys.argv[4], clickable=True).click()
    else:
         d(className=sys.argv[3]).child(clickable=True).click()
else:
    print("Could not find instance by res name "+sys.argv[2])

#d(resourceId).child(clickable=True).click()
#d(resourceId).child_by_text(text, clickable=True).click()
#d(resourceId)[pos].child(clickable).click()







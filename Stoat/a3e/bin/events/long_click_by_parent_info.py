#!/usr/bin/python

from pydoc import classname
from uiautomator import Device
import sys

# get the target device
d = Device(sys.argv[1])

# check whether the text exists
if d(resourceId=sys.argv[2]).exists and d(resourceId=sys.argv[2]).child(longClickable=True).exists:
    #Gets longClickable child with itself or a granchild containing given text
    #print(d(resourceId=sys.argv[2]).info)
    if sys.argv[4] and d(resourceId=sys.argv[2]).child_by_text(sys.argv[4], longClickable=True).exists:
        print(d(resourceId=sys.argv[2]).child_by_text(sys.argv[4], longClickable=True).info)
        print("Clicking on the element with res id and text")
        #print(d(className="android.widget.ListView").child(longClickable=True).child_by_text("french-body-parts.db").info)
        #print(d(className="android.widget.ListView").child_by_text("french-body-parts.db", longClickable=True).info)
        d(resourceId=sys.argv[2]).child_by_text(sys.argv[4], longClickable=True).swipe.right(steps=100)
    else:
        #Gets the first longClickable child
        print(d(resourceId=sys.argv[2]).child_by_instance(0, longClickable=True).info)
        d(resourceId=sys.argv[2]).child_by_instance(0, longClickable=True, className="android.widget.TextView").swipe.right(steps=10)
elif d(className=sys.argv[3]).exists and d(className=sys.argv[3]).child(longClickable=True).exists:
    if sys.argv[4] and d(className=sys.argv[3]).child_by_text(sys.argv[4], longClickable=True).exists:
        print("Clicking on the element with classname and text")
        #print(d(className="android.widget.ListView").child(longClickable=True).child_by_text("french-body-parts.db").info)
        #print(d(className="android.widget.ListView").child_by_text("french-body-parts.db", longClickable=True).info)
        d(className=sys.argv[3]).child_by_text(sys.argv[4], longClickable=True).swipe.right(steps=100)
    else:
         d(className=sys.argv[3]).child(longClickable=True).swipe.right(steps=100)
else:
    print("Could not find instance by res name "+sys.argv[2])

#d(resourceId).child(longClickable=True).long_click()
#d(resourceId).child_by_text(text, longClickable=True).long_click()
#d(resourceId)[pos].child(longClickable).long_click()







import sys
import os
import time
pathname = os.path.dirname(sys.argv[0])
#updating lib path
sys.path.append(os.path.abspath(pathname + os.path.sep +  "lib"))
import proactivelib
#updating vmware path
sys.path.append(os.path.abspath(pathname + os.path.sep + "vmware"))
import vmware
#updating virtualbox path
sys.path.append(os.path.abspath(pathname + os.path.sep + "virtualbox"))
import virtualbox
#updating xenserver path
if sys.version_info[0] == 2 :
    sys.path.append(os.path.abspath(pathname + os.path.sep + "xenserver"))
    sys.path.append(os.path.abspath(pathname + os.path.sep + "xenserver" + os.path.sep + "ext"))
    import xenserver

#This module is the main entry point to bootstrap
#ProActive environment when deploying through GCM
#on virtualized infrastructures.

#Can be called with an argument which is the absolute file path
#that will be used as logfile.

if len(sys.argv) >= 2 :
    logFile = sys.argv[1]
    print ("logging on ",logFile)
    out = open(logFile,"a")
    sys.stdout = out
    sys.stderr = out
else :
    print ("logging on standard output")
x = None

#you can register new Abstract_Runtime implementation here
proactivelib.Abstract_Runtime.addProvider(vmware.VMware_Runtime())
proactivelib.Abstract_Runtime.addProvider(virtualbox.Virtualbox_Runtime())
if sys.version_info[0] == 2 :
    proactivelib.Abstract_Runtime.addProvider(xenserver.XenServer_Runtime())

#iterates registered Abstract_Runtime implementation to find the good
#environment.
while x == None:
    x = proactivelib.Abstract_Runtime.getInstance()
    if x == None :
        print ("getInstance from Abstract_Runtime returned None.")
        print ("Waiting 10s...")
        time.sleep(10)
x.start()

'''
Created on 5 mai 2009

@author: jmguilla
'''
import sys
import proactivelib
from subprocess import *

#This module is the implementation of proativelib.Abstract_Runtime
#to be able to handle VMware virtualized environments.

class VMware_Runtime ( proactivelib.Abstract_Runtime ) :

    #you can change this vector to specify a different environment
    __getUtilCmd = ["vmware-guestd","--cmd","info-get guestinfo.bootstrapURL"]

    def isOk(self):
        """This method is used to check if the current environment
        matches vmware requirements to be used. If it is the case, and
        instance of VMware_Runtime will be returned"""
        try :
            proc = Popen(args = VMware_Runtime.__getUtilCmd, stdout = PIPE, stderr = PIPE)
            output = repr(proc.communicate()[0])
            if output.find("http://") != -1 :
                return self
            else :
                return None
        except :
            print ("An exception occured while testing vmware-guestd")
            return None

    def start(self):
        """Exctracts bootstrapURL calling vmware-guestd and calls
        _start from Abstract_Runtime"""
        proc = Popen(args = VMware_Runtime.__getUtilCmd, stdout = PIPE, stderr = PIPE)
        output = proc.communicate()[0].decode("utf-8")
        self._start(output)

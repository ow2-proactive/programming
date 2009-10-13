'''
Created on 5 mai 2009

@author: jmguilla
'''
import sys
import proactivelib
from subprocess import *


class Virtualbox_Runtime ( proactivelib.Abstract_Runtime ):

    #you can change this vector to specify a different environment
    __getUtilCmd = ["VBoxControl","guestproperty","get","bootstrapURL"]

    def isOk(self):
        """This static method is used to check if the current environment
        matches vmware requirements to be used. If it is the case, and
        instance of Virtualbox_Runtime will be returned"""
        try :
            proc = Popen(args = Virtualbox_Runtime.__getUtilCmd, stdout = PIPE, stderr = PIPE)
            output = repr(proc.communicate()[0])
            if output.find("Value:") != -1 :
                return self
            else :
                return None
        except :
            print ("An exception occurred while testing VBoxControl")
            return None

    def start(self):
        """Exctracts bootstrapURL calling VBoxControl and calls
        _start from Abstract_Runtime"""
        proc = Popen(args = Virtualbox_Runtime.__getUtilCmd, stdout = PIPE, stderr = PIPE)
        output = proc.communicate()[0].decode("utf-8")
        url = output.partition("Value: ")[2].strip()
        self._start(url)

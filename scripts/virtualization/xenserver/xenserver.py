#!/usr/bin/python

'''
Created on 5 mai 2009

@author: jmguilla
'''
import sys
import proactivelib
from xenserver_utils import XenServer_Helper
import re
import traceback
from subprocess import *

#you have to set the following info to be able to
#bootstrap proactive runtime

xenServerAddress = "http://192.168.1.193"
xenServerUserID = "root"
xenServerUserPWD = "root123"

class XenServer_Runtime( proactivelib.Abstract_Runtime ):

    def isOk(self):
        """This method is used to check if the current environment
        matches xenserver requirements to be used. If it is the case, an
        instance of VMware_Runtime will be returned"""
        try :
            self.helper = XenServer_Helper(xenServerAddress,xenServerUserID,xenServerUserPWD)
            bootstrapURL = self.helper.getData(XenServer_Helper.proacBootstrapURL)
            if bootstrapURL != None :
                return self
            else :
                return None
        except :
            print ("An exception occured while testing xenserver env")
            traceback.print_exc()
            return None

    def start(self):
        """Exctracts bootstrapURL and calls
        _start from Abstract_Runtime"""
        output = self.helper.getData(XenServer_Helper.proacBootstrapURL)
        self._start(output)

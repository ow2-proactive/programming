'''
Created on 5 mai 2009

@author: jmguilla
'''
import sys
import proactivelib
from hyperv_utils import *
import re
import traceback
from subprocess import *

#you have to set the following info to be able to
#bootstrap proactive runtime

#Your hyper-v server url
hypervServerAddress = None
#A hyper-v user
hypervServerUserID = None
#The user's password
hypervServerUserPWD = None

#Abstract_Runtime implementation for Microsoft Hyper-V
class HyperV_Runtime( proactivelib.Abstract_Runtime ):

    def isOk(self):
        """This method is used to check if the current environment
        matches Hyper-V requirements to be used. If it is the case, an
        instance of HyperV_Runtime will be returned"""
        try :
            self.helper = HyperV_Helper_Factory.getHyperV_Helper_instance(hypervServerAddress,hypervServerUserID,hypervServerUserPWD)
            bootstrapURL = self.helper.getData(HyperV_Helper.proacBootstrapURL)
            if bootstrapURL != None :
                return self
            else :
                return None
        except :
            print ("An exception occured while testing hyperv env")
            traceback.print_exc()
            return None

    def start(self):
        """Exctracts bootstrapURL and calls
        _start from Abstract_Runtime"""
        output = self.helper.getData(HyperV_Helper.proacBootstrapURL)
        self._start(output)

'''
Created on 5 mai 2009

@author: jmguilla
'''

#this module is the python library for use with ProActive
#GCMDeployment on virtualized infrastructures.

import os
import re
import sys
import time
import atexit
from subprocess import *

urlopen = None
if sys.version_info[0] == 3 :
        import urllib.request
        urlopen = urllib.request.urlopen
else :
        import urllib
        urlopen = urllib.urlopen

PA_RT_COMMAND = "runtimeCommand"

class Abstract_Runtime :

    __providers = [] #to keep known implementations
    __runtimes = [] #to keep known runtimes

    def __extractValues(self, url) :
        """extractValues(str) connects to the str
        parameter and collects every 'key = value'
        lines written in there. The result is a
        dictionnary containing all key, value."""
        lignes = {}
        res = {}
        for i in range(10) :
            try :
                lignes = urlopen(url).readlines()
                break
            except IOError :
                print ("Handling an exception while reading bootstrapURL, waiting 10s")
                sys.stdout.flush()
                sys.stderr.flush()
                time.sleep(10)
        for ligne in lignes:
            ligne = ligne.decode("utf-8").strip()
            key, val = ligne.split("=",1)
            res[key.strip()] = val.strip()
        return res

    def __bootstrapRuntime(self, dico) :
        """bootstrapRuntime(dict) is used to bootsrap
        ProActive's Runtime. 'dict' is a dictionnary
        containing all needed values to boot the
        environment. Available variables are:
        deploymentId, parentURL, routerAddress, routerPort"""
        command = dico[PA_RT_COMMAND]
        command = command.replace("\\\\","\\")
        cmdList = re.findall("[^\s\"]*(?:\"[^\"]*\")*[^\s\"]*[\s]*",command)
        for i in range(len(cmdList)):
            tmp = cmdList[i]
            tmp = tmp.strip()
            tmp = tmp.replace("\"","")
            cmdList[i] = tmp
        print ("Matching: ",command)
        print ("Submitting: ",cmdList)
        proc = Popen(args = cmdList, stdout = sys.stdout, stderr = sys.stderr)
        Abstract_Runtime.__registerRuntime(proc)
        proc.wait()

    def _start(self, url) :
        """launch proactive runtime.
        url: url to get bootstrap info -> string"""
        print ("Begining log: ", time.strftime('%d/%m/%y %H:%M',time.localtime()))
        print ("Given URL:", url)
        dico = self.__extractValues(str(url))
        print ("environment values:")
        print (dico)
        self.__bootstrapRuntime(dico)

    def __cleanChild():
        for i in range(len(Abstract_Runtime.__runtimes)):
            try:
                proc = Abstract_Runtime.__runtimes[i]
                proc.terminate()
            except AttributeError:
                print ("An error occured while cleaning environment.")
                print ("If you want to use this feature, be sure to run at least python 2.6")
    __cleanChild = staticmethod(__cleanChild)

    def addProvider(provider):
        Abstract_Runtime.__providers.append(provider)
    addProvider = staticmethod(addProvider)

    def __registerRuntime(rt):
        Abstract_Runtime.__runtimes.append(rt)
        atexit.register(Abstract_Runtime.__cleanChild)
    __registerRuntime = staticmethod(__registerRuntime)

    def getInstance():
        """this static method tries to initialize the environment
        returning the goot proactive provider. If you write new
        providers, add yours here"""
        for index,instance in enumerate(Abstract_Runtime.__providers):
            res = instance.isOk()
            if res != None:
                print ("find a matching environment")
                return instance
        print ("no matching environment found")
        return None
    getInstance = staticmethod(getInstance)



import XenAPI
import sys
import re
import traceback
from subprocess import *

#This module encompass the lib-xen python api to bring it to a more
#object oriented architecture corresponding to our needs.

class XenServer_Helper :

    #Here are developpers data
    __proacRTKey = "PARuntimeKey."
    __proacHardwareAddress = "ha"
    proacBootstrapURL = "bootstrapURL"
    proacHoldingVM = "holdingVM"
    proacRMUrl = "rmUrl"
    proacRMUser = "rmUser"
    proacRMPwd = "rmPwd"
    proacNodeSourceName = "nodesource"
    proacHostCapacity = "hostCapacity"
    proacVmCapcity = "vmCapacity"
    proacNodeNumber = "nodeNumber"
    proacDynamicPropertyBaseStr = "dynamic."
    proacNodeURLBaseStr = "nodeUrl."
    def __init__(self,url,user,pwd):
        self.url = url
        self.user = user
        self.pwd = pwd
        self.session = XenAPI.Session(self.url)
        self.session.xenapi.login_with_password(self.user, self.pwd)

    def __del__(self):
        self.session.xenapi.session.logout()

    def __getMacAddress(self):
        """getMacAddress returns an array filled with every
        detected NIC's mac address on the current computer"""
        proc = None
        if sys.platform == 'win32':
            proc = Popen( args = "ipconfig /all", stdout = PIPE, stderr = PIPE)
        else:
            proc = Popen( args = "/sbin/ifconfig", stdout = PIPE, stderr = PIPE)
        output = proc.communicate()[0]
        res = re.findall("(?:[0-9a-fA-F]{2}:){5}[0-9a-fA-F]{2}",output)
        return res

    def __fixHoldingVirtualMachine(self):
        """fix the current holding virtual machine
        field given the mac address"""

        vms = self.session.xenapi.VM.get_all()
        macs = self.__getMacAddress()
        print("vms:",vms)
        print("macs:",macs)
        url = None
        for i, vm in enumerate(vms):
            print("vm:",vm)
            data = self.session.xenapi.VM.get_xenstore_data(vm)
            for j,mac in enumerate(macs):
                print("mac:",mac)
                print("data:",data)
                key = XenServer_Helper.__proacRTKey + XenServer_Helper.__proacHardwareAddress
                try:
                    remoteMac = data[key].lower().strip()
                    mac = mac.lower().strip()
                    if remoteMac.startswith(mac) and remoteMac.endswith(mac):
                        self.holdingVM = vm
                        print ("holding vm: " + self.holdingVM)
                        return True
                except KeyError:
                    print ("invalid key: ", key, " supplied.")
        print("No holdingVM found")
        return False

    def __getHoldingVM(self):
        """This private method is used to determine the virtual
        machine in which one this programm is running. This is done
        by iterating on all vm registered within the remote XenServer
        instance until a vm with the same MAC Address is found"""
        res = self.__fixHoldingVirtualMachine()
        if res == True:
            return self.holdingVM
        else:
            return None

    def getData(self,key):
        """To get a data saved thanks to pushData method"""
        vm = self.__getHoldingVM()
        if vm == None:
            raise EnvironmentError("No holding VM found")
        datas = self.session.xenapi.VM.get_xenstore_data(self.holdingVM)
        try:
            data = datas[ XenServer_Helper.__proacRTKey + key ]
            return data
        except KeyError:
            print("Unable to get data " + key)
            traceback.print_exc()
            return None

    def pushData(self,key,value):
        """To write a data in the virtual machine environment"""
        vm = self.__getHoldingVM()
        if vm == None:
            raise EnvironmentError("No holding VM found")
        self.session.xenapi.VM.remove_from_xenstore_data(self.holdingVM,XenServer_Helper.__proacRTKey + key)
        self.session.xenapi.VM.add_to_xenstore_data(self.holdingVM,XenServer_Helper.__proacRTKey + key,value)

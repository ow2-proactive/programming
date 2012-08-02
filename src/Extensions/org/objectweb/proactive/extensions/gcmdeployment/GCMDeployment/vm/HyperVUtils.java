/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.vm;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;

import org.ow2.proactive.virtualizing.core.error.VirtualServiceException;


/**
 * Utility Class used to bootstrap Hyper-V virtual machine's environment.
 * Uses IPC via sockets for data exchange
 */
public abstract class HyperVUtils {

    /** for local inter process communication */
    public static final int message_length = 1000;
    public static final int TIMEOUT = 10000;

    //methods' name
    public static final String HOLDING_VM = "holdingVM";
    public static final String GET_DATA = "getData";
    public static final String PUSH_DATA = "pushData";

    /**
     * Program entry point
     * @param args args[0] == Server Socket Port for IPC
     * args[1] == Hyper-V server url
     * args[2] == Hyper-V user
     * args[3] == user's password
     * args[4] == holdingVM or getData
     * args[5..n] depends on args[4]
     * exit == 0 - OK
     * exit == 1 - command not found
     * exit == 2 - UnknownHostException
     * exit == 3 - VirtualServiceException
     * exit == 4 - IOException
     * exit == 5 - TIMEOUT, timeout value = 10seconds
     * @throws VirtualServiceException
     * @throws UnknownHostException
     * @throws IOException
     */
    public static void main(final String[] args) {
        if (!(args.length >= 5)) {
            System.err.println("usage: HyperVUtils url user pwd command [param]");
            System.err.println("where command == (holdingVM||getData)");
            throw new IllegalArgumentException("You must call HyperVUtils with at least 5 parameters");
        }
        Thread wmi = new Thread() {
            public void run() {
                try {
                    new HyperVUtilsWMI().mainImpl(args);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (VirtualServiceException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread winrm = new Thread() {
            public void run() {
                try {
                    new HyperVUtilsWinRM().mainImpl(args);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (VirtualServiceException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        winrm.start();
        wmi.start();
        try {
            winrm.join(TIMEOUT);
            wmi.join(TIMEOUT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Program entry point
     * @param args args[0] == Server Socket Port for IPC
     * args[1] == Hyper-V server url
     * args[2] == Hyper-V user
     * args[3] == user's password
     * args[4] == holdingVM or getData
     * args[5..n] depends on args[4]
     * @throws VirtualServiceException
     * @throws UnknownHostException
     * @throws IOException
     */
    protected void mainImpl(String[] args) throws VirtualServiceException, UnknownHostException, IOException {
        Formatter output = null;
        int z = 0;
        int port = Integer.parseInt(args[z++]);
        String url = args[z++];
        String user = args[z++];
        String pwd = args[z++];
        String command = args[z++];
        String result = null;
        try {
            if (command.equals(HOLDING_VM)) {
                if (!(args.length >= 6)) {
                    System.err.println("You must pass mac address to HyperVUtils holdingVM");
                    throw new IllegalArgumentException("You must pass mac address to HyperVUtils holdingVM");
                }
                String[] targetMacs = new String[args.length - z];
                for (int i = z; i < args.length; i++) {
                    targetMacs[i - z] = args[i];
                }
                result = getHoldingVM(url, user, pwd, targetMacs);
            } else if (command.equals(GET_DATA)) {
                if (!(args.length >= 7)) {
                    System.err.println("You must pass vm's name and property key to HyperVUtils getData");
                    throw new IllegalArgumentException(
                        "You must pass vm's name and property key to HyperVUtils getData");
                }
                String holdingVM = args[z++];
                String[] datas = new String[args.length - z];
                for (int i = z; i < args.length; i++) {
                    datas[i - z] = args[i];
                }
                result = getData(url, user, pwd, holdingVM, datas);
            } else if (command.equals(PUSH_DATA)) {
                if (!(args.length >= 8)) {
                    System.err
                            .println("You must pass vm's name, property key and property value to HyperVUtils pushData");
                    throw new IllegalArgumentException(
                        "You must pass vm's name and property key to HyperVUtils getData");
                }
                String holdingVM = args[z++];
                String key = args[z++];
                String value = args[z++];
                result = pushData(url, user, pwd, holdingVM, key, value);
            } else {
                System.err.println("command not found: " + command);
                System.exit(1);
            }
            if (result != null) {
                if (port != -1) {
                    Socket clientSocket = new Socket("127.0.0.1", port);
                    output = new Formatter(clientSocket.getOutputStream());
                } else {
                    output = new Formatter(System.out);
                }
                output.format("%1$" + ((port != -1) ? message_length : result.length()) + "s", result);
                output.flush();
            }
        } finally {
            if (output != null) {
                output.format("%1$" + ((port != -1) ? message_length : 3) + "s", "EOF");
                output.flush();
                if (port != -1) {
                    output.close();
                }
            }
        }
    }

    /** allows user to push data in the virtual machine named &quot;holdingVM&quot;.
     * if port is set, connect to the server listening on the current machine at the given port and serializes
     * data to the server.
     * @param port for IPC, server listening port.
     * @param url hypervisor's url
     * @param user hypervisor's user
     * @param pwd user's password
     * @param holdingVM the name of the virtual machine to update
     * @param key the key
     * @param value the key's associated value
     * @return the string OK if everything went well, the string KO otherwise
     */
    protected abstract String pushData(String url, String user, String pwd, String holdingVM, String key,
            String value);

    /**
     * Allows user to get the virtual machine that registered one of the macs as a
     * NIC mac address.
     * if port is set, connect to the server listening on the current machine at the given port and serializes
     * data to the server.
     * @param port the server socket port for data exchange, if -1, result will be printed on stdout
     * @param url hyper-v server url
     * @param user hyper-v user with enough permission
     * @param pwd user's password
     * @param macs a list a mac address to test
     * @throws UnknownHostException
     * @throws IOException
     * @throws VirtualServiceException
     */
    protected abstract String getHoldingVM(String url, String user, String pwd, String[] targetMacs);

    /**
     * Allows user to get a data registered in virtual machine configuration.
     * A such data has been push in virtual machine's environment using
     * {@link HyperVVM#pushData(String, String)} or {@link HyperVVM#pushKvpExchangeData(java.util.Hashtable)}
     * or {@link HyperVVM#pushKvpExchangeData(String, String)}.
     * You can also directly use Hyper-V native WMI management tool.
     * The result is a Map/Dictionary formatted regarding json spec.
     * (example: '{"testVar": "testVal", "testVar2": "testVal2"}' )
     * @param port the server socket port for data exchange, if -1, result will be printed on stdout
     * @param url hyper-v server url
     * @param user hyper-v user with enough permission
     * @param pwd user's password
     * @param vmName virtual machine's name
     * @param datas a list to key from which one data will be retrieved
     * @throws UnknownHostException
     * @throws IOException
     * @throws VirtualServiceException
     */
    protected abstract String getData(String url, String user, String pwd, String holdingVM, String[] datas);
}

class HyperVUtilsWMI extends HyperVUtils {

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getHoldingVM(String url, String user, String pwd, String[] macs) {
        try {
            org.ow2.proactive.virtualizing.hypervwmi.HyperVVMM vmm = new org.ow2.proactive.virtualizing.hypervwmi.HyperVVMM(
                url, user, pwd);
            ArrayList<org.ow2.proactive.virtualizing.hypervwmi.HyperVVM> vms = vmm.getVirtualMachines();
            for (org.ow2.proactive.virtualizing.hypervwmi.HyperVVM vm : vms) {
                String[] remoteMacs = vm.getMacAddress();
                for (String mac : remoteMacs) {
                    for (String targetMac : macs) {
                        if (mac != null && mac.equalsIgnoreCase(targetMac)) {
                            return vm.getName();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getData(String url, String user, String pwd, String vmName, String[] datas) {
        try {
            org.ow2.proactive.virtualizing.hypervwmi.HyperVVMM vmm = new org.ow2.proactive.virtualizing.hypervwmi.HyperVVMM(
                url, user, pwd);
            org.ow2.proactive.virtualizing.hypervwmi.HyperVVM vm = vmm.getNewVM(vmName);
            HashMap<String, String> result = new HashMap<String, String>();
            for (String dataKey : datas) {
                String res = null;
                try {
                    res = vm.getData(dataKey);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (res != null) {
                    result.put(dataKey, res);
                }
            }
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            Iterator<String> keys = result.keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                sb.append("\"" + key + "\":\"" + result.get(key) + "\"");
                if (keys.hasNext()) {
                    sb.append(", ");
                }
            }
            sb.append("}");
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String pushData(String url, String user, String pwd, String holdingVM, String key, String value) {
        try {
            org.ow2.proactive.virtualizing.hypervwmi.HyperVVMM vmm = new org.ow2.proactive.virtualizing.hypervwmi.HyperVVMM(
                url, user, pwd);
            org.ow2.proactive.virtualizing.hypervwmi.HyperVVM vm = vmm.getNewVM(holdingVM);
            System.out.println("pushing kvp: " + key + ":" + value);
            vm.pushData(key, value);
            return "OK";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "KO";
    }
}

class HyperVUtilsWinRM extends HyperVUtils {

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getHoldingVM(String url, String user, String pwd, String[] macs) {
        try {
            org.ow2.proactive.virtualizing.hypervwinrm.HyperVVMM vmm = new org.ow2.proactive.virtualizing.hypervwinrm.HyperVVMM(
                url, user, pwd);
            ArrayList<org.ow2.proactive.virtualizing.hypervwinrm.HyperVVM> vms = vmm.getVirtualMachines();
            for (org.ow2.proactive.virtualizing.hypervwinrm.HyperVVM vm : vms) {
                String[] remoteMacs = vm.getMacAddress();
                for (String mac : remoteMacs) {
                    for (String targetMac : macs) {
                        if (mac != null && mac.equalsIgnoreCase(targetMac)) {
                            return vm.getName();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getData(String url, String user, String pwd, String vmName, String[] datas) {
        try {
            org.ow2.proactive.virtualizing.hypervwinrm.HyperVVMM vmm = new org.ow2.proactive.virtualizing.hypervwinrm.HyperVVMM(
                url, user, pwd);
            org.ow2.proactive.virtualizing.hypervwinrm.HyperVVM vm = vmm.getNewVM(vmName);
            HashMap<String, String> result = new HashMap<String, String>();
            for (String dataKey : datas) {
                String res = null;
                try {
                    res = vm.getData(dataKey);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (res != null) {
                    result.put(dataKey, res);
                }
            }
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            Iterator<String> keys = result.keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                sb.append("\"" + key + "\":\"" + result.get(key) + "\"");
                if (keys.hasNext()) {
                    sb.append(", ");
                }
            }
            sb.append("}");
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String pushData(String url, String user, String pwd, String holdingVM, String key, String value) {
        try {
            org.ow2.proactive.virtualizing.hypervwinrm.HyperVVMM vmm = new org.ow2.proactive.virtualizing.hypervwinrm.HyperVVMM(
                url, user, pwd);
            org.ow2.proactive.virtualizing.hypervwinrm.HyperVVM vm = vmm.getNewVM(holdingVM);
            System.out.println("pushing kvp: " + key + ":" + value);
            vm.pushData(key, value);
            return "OK";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "KO";
    }
}
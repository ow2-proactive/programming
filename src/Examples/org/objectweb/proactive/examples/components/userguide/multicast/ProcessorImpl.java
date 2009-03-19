/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.examples.components.userguide.multicast;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;


public class ProcessorImpl implements BindingController, Runnable {
    SlaveMulticast slaves = null;

    public ProcessorImpl() {
    };

    /**
     * {@inheritDoc}
     */
    public void bindFc(String clientItfName, Object serverItf) throws NoSuchInterfaceException,
            IllegalBindingException, IllegalLifeCycleException {
        if ("slave".equals(clientItfName)) {
            //System.err.println("XXXXXXXXXXXXX" + serverItf  + "class: " + serverItf.getClass());
            // if (serverItf instanceof Slave) {
            slaves = (SlaveMulticast) serverItf;
            //  } else {
            //      throw new IllegalBindingException(clientItfName);
            //  }
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String[] listFc() {
        return new String[] { "slave" };
    }

    /**
     * {@inheritDoc}
     */
    public Object lookupFc(String clientItfName) throws NoSuchInterfaceException {
        if ("slave".equals(clientItfName)) {
            return slaves;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void unbindFc(String clientItfName) throws NoSuchInterfaceException, IllegalBindingException,
            IllegalLifeCycleException {
        if ("slave".equals(clientItfName)) {
            slaves = null;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    public void run() {
        List<List<String>> multicastArgsList = new ArrayList<List<String>>();
        for (int i = 0; i < 6; i++) {
            multicastArgsList.add(i, new ArrayList<String>());

            for (int j = 0; j < i; j++) {
                multicastArgsList.get(i).add("arg " + j);
            }
        }
        //        System.err.println("RUN in Processor");
        //        for (List<String> list : multicastArgsList) {
        //            System.err.println("Avec " + list.size() + " arguments.");
        //            Object[] sw = ((List<String>) slaves.computeSync(list,
        //                    "Sync")).toArray();
        //            for (Object object : sw) {
        //                System.err.println("Object result: " + object);
        //            }
        //            System.err.println();
        //        }
        //        System.err.println("END in Processor");
        System.err.println("RUN in Processor");
        for (List<String> list : multicastArgsList) {
            System.err.println("Avec " + list.size() + " arguments.");
            Object[] sw = ((List<StringWrapper>) slaves.computeAsync(list, "Async")).toArray();
            for (Object object : sw) {
                System.err.println("Object result: " + object);
            }
            System.err.println();
        }
        System.err.println("END in Processor");

        System.err.println("RUN in Processor");
        for (List<String> list : multicastArgsList) {
            System.err.println("Avec " + list.size() + " arguments.");
            slaves.compute(list, "OneWay");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.err.println();
        }
        System.err.println("END in Processor");
    }
}

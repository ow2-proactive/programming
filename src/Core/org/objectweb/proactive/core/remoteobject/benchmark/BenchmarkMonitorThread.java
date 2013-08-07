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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.remoteobject.benchmark;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.remoteobject.RemoteObjectRequest;
import org.objectweb.proactive.core.remoteobject.RemoteRemoteObject;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class BenchmarkMonitorThread extends Observable {
    static final Logger LOGGER_RO = ProActiveLogger.getLogger(Loggers.REMOTEOBJECT);

    private static Method methods[];
    private String clazz;

    static {
        try {
            methods = new Method[6];
            methods[0] = BenchmarkObject.class.getDeclaredMethod("init", new Class[0]);
            methods[1] = BenchmarkObject.class.getDeclaredMethod("getResult", new Class[0]);
            methods[2] = BenchmarkObject.class.getDeclaredMethod("doTest", new Class[0]);
            methods[3] = BenchmarkObject.class.getDeclaredMethod("receiveResponse",
                    new Class<?>[] { Object.class });
            methods[4] = BenchmarkObject.class
                    .getDeclaredMethod("setParameter", new Class[] { String.class });
            ;
            methods[5] = BenchmarkObject.class.getDeclaredMethod("getRequest", new Class[0]);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private Map<URI, RemoteRemoteObject> remainingBenchmark;
    private ArrayList<Pair> orderedProtocols;
    private ArrayList<String> unaccessibles;

    private boolean finished = false;
    private Thread thread;

    public BenchmarkMonitorThread(Map<URI, RemoteRemoteObject> remoteObjectUrls, String benchmarkObjectclazz) {
        LOGGER_RO.debug("[Multi-protocol] Benchmark choose : " + benchmarkObjectclazz);
        this.remainingBenchmark = new LinkedHashMap<URI, RemoteRemoteObject>();
        ArrayList<String> uniq = new ArrayList<String>();
        for (URI uri : remoteObjectUrls.keySet()) {
            if (!uniq.contains(uri.getScheme())) {
                this.remainingBenchmark.put(uri, remoteObjectUrls.get(uri));
                uniq.add(uri.getScheme());
            }
        }
        this.orderedProtocols = new ArrayList<Pair>();
        this.unaccessibles = new ArrayList<String>();
        this.clazz = benchmarkObjectclazz;
    }

    /**
     * Add a new list of RemoteRemoteObject to test and launch the benchmark if there are new protocol (based on URI scheme)
     */
    public boolean addAndRestartIfNecessary(Map<URI, RemoteRemoteObject> remoteObjects) {
        boolean add = false, restart = false;
        synchronized (remainingBenchmark) {
            for (URI uri1 : remoteObjects.keySet()) {
                add = true;

                if (!unaccessibles.contains(uri1.getScheme())) {
                    // Protocol already waiting for a benchmark
                    for (URI uri2 : remainingBenchmark.keySet()) {
                        if (uri1.getScheme().equalsIgnoreCase(uri2.getScheme())) {
                            add = false;
                            break;
                        }
                    }
                    if (add)
                        // Benchmark is already done for this protocol
                        for (Pair p : orderedProtocols) {
                            if (uri1.getScheme().equalsIgnoreCase(p.protocol)) {
                                add = false;
                                break;
                            }
                        }
                    // No information found for this protocol, can be added to the "remaining" list.
                    if (add) {
                        remainingBenchmark.put(uri1, remoteObjects.get(uri1));
                        restart = true;
                    }
                }
                if (restart) {
                    this.launchBenchmark();
                } else {
                    // If benchmark are already done, simply return results to the Observers
                    this.notifyAdd();
                }
            }
        }
        return restart;
    }

    /**
     * Use Collections.sort
     */
    private void addAndSort(Pair pair) {
        this.orderedProtocols.add(pair);
        Collections.sort(orderedProtocols);
    }

    /**
     * Return an array containing the protocol scheme by extracting the value from the Pair object
     */
    private String[] getOrder() {
        String[] copy = new String[orderedProtocols.size()];
        for (int i = 0; i < orderedProtocols.size(); i++) {
            copy[i] = orderedProtocols.get(i).protocol;
        }
        // ordered := [a, b, c]
        // fixed := [d, b]
        // should return [b, a, c]
        // Return all protocols from orderedProtocols' array
        // but reordered them in accordance with the property
        if (CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOLS_ORDER.isSet()) {
            List<String> fixedOrder = CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOLS_ORDER.getValue();
            String[] ret = new String[copy.length];
            boolean braek = false;
            // For each box to fill in the array
            for (int i = 0; i < ret.length; i++) {
                // Try to keep the 'fixed order'
                for (int j = 0; j < fixedOrder.size(); j++) {
                    braek = false;
                    // But check if it works
                    for (int k = 0; k < copy.length; k++) {
                        if (fixedOrder.get(j) != null && copy[k] != null &&
                            fixedOrder.get(j).equalsIgnoreCase(copy[k])) {
                            ret[i] = copy[k];
                            copy[k] = null;
                            fixedOrder.set(j, null);
                            braek = true;
                            break;
                        }
                    }
                    if (braek)
                        break;
                }
                // Fill the blank boxes with protocol that works
                // but which are not in PA_COMMUNICATION_PROTOCOLS_ORDER
                // property
                if (ret[i] == null) {
                    for (int j = 0; j < copy.length; j++) {
                        if (copy[j] != null) {
                            ret[i] = copy[j];
                            copy[j] = null;
                            break;
                        }
                    }
                }
            }
            return ret;
        } else
            // Property isn't set
            return copy;
    }

    public class BenchmarkThread implements Runnable {
        public void run() {
            finished = false;
            while (!remainingBenchmark.isEmpty()) {
                URI uri = null;
                // Use iterator for being able to remove RemoteRemoteObject from the Collection remainingBenchmark when done
                for (Iterator<Entry<URI, RemoteRemoteObject>> iter = remainingBenchmark.entrySet().iterator(); iter
                        .hasNext();) {

                    Class<BenchmarkObject> benchmarkClass = null;
                    try {
                        benchmarkClass = (Class<BenchmarkObject>) Class.forName(clazz);
                    } catch (ClassNotFoundException e1) {
                        throw new ProActiveRuntimeException("The class \"" + clazz +
                            "\" hasn't been found or don't implement the BenchmarkObject interface.");
                    }
                    Object benchmark = null;
                    try {
                        benchmark = benchmarkClass.newInstance();
                    } catch (InstantiationException e1) {
                        e1.printStackTrace();
                    } catch (IllegalAccessException e1) {
                        e1.printStackTrace();
                    }

                    try {
                        Entry<URI, RemoteRemoteObject> entry = iter.next();
                        RemoteRemoteObject rro = entry.getValue();
                        uri = entry.getKey();
                        //benchmark.setParameter(int) : pass a parameter to the benchmark
                        if (CentralPAPropertyRepository.PA_BENCHMARK_PARAMETER.isSet()) {
                            methods[4].invoke(benchmark, CentralPAPropertyRepository.PA_BENCHMARK_PARAMETER
                                    .getValue());
                        }
                        //benchmark.init() : Initialize the benchmark
                        methods[0].invoke(benchmark, new Object[0]);
                        // benchmark.getRequest() : get the request to send to the RemoteObject
                        RemoteObjectRequest request = (RemoteObjectRequest) methods[5].invoke(benchmark,
                                new Object[0]);
                        // while ( benchmark.doTest() )
                        while ((Boolean) methods[2].invoke(benchmark, new Object[0])) {
                            Object res = rro.receiveMessage(request).getResult().getResult();
                            // benchmark.receiveResponse(Object res) : send the returned value to the benchmark object
                            methods[3].invoke(benchmark, res);
                        }
                        // benchmark.getResult() : return the benchmark's result
                        Integer result = (Integer) methods[1].invoke(benchmark, new Object[0]);
                        if (LOGGER_RO.isDebugEnabled()) {
                            LOGGER_RO.debug("[Multi-protocol] Benchmark result for " + uri + " is " +
                                (result.intValue() == 0 ? "OK" : result.intValue()));
                        }
                        Pair pair = new Pair(uri.getScheme(), result.intValue());
                        addAndSort(pair);
                        iter.remove();
                    } catch (NullPointerException npe) {
                        LOGGER_RO.warn("Protocol " + uri.getScheme() + " is unaccessible during benchmark.");
                        iter.remove();
                        unaccessibles.add(uri.getScheme());
                        continue;
                    } catch (ProActiveException pae) {
                        LOGGER_RO.warn("Protocol " + uri.getScheme() + " is unaccessible during benchmark.");
                        iter.remove();
                        unaccessibles.add(uri.getScheme());
                        continue;
                    } catch (IOException ioe) {
                        LOGGER_RO.warn("Protocol " + uri.getScheme() + " is unaccessible during benchmark.");
                        iter.remove();
                        unaccessibles.add(uri.getScheme());
                        continue;
                    } catch (RenegotiateSessionException e) {
                        e.printStackTrace();
                        // Reflection part
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
            setChanged();
            notifyObservers(getOrder());
            deleteObservers();
            finished = true;
        }
    }

    public void launchBenchmark() {
        if (finished || (this.thread == null || !this.thread.isAlive())) {
            this.thread = new Thread(new BenchmarkThread());
            this.thread.start();
        }
    }

    public void notifyAdd() {
        if (finished) {
            this.setChanged();
            this.notifyObservers(getOrder());
            deleteObservers();
        }
    }
}

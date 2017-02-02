/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.objectweb.proactive.core.remoteobject.benchmark;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.ProtocolException;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.remoteobject.RemoteObjectRequest;
import org.objectweb.proactive.core.remoteobject.RemoteObjectSet;
import org.objectweb.proactive.core.remoteobject.RemoteRemoteObject;
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
            methods[3] = BenchmarkObject.class.getDeclaredMethod("receiveResponse", new Class<?>[] { Object.class });
            methods[4] = BenchmarkObject.class.getDeclaredMethod("setParameter", new Class[] { String.class });
            ;
            methods[5] = BenchmarkObject.class.getDeclaredMethod("getRequest", new Class[0]);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private ConcurrentHashMap<URI, RemoteRemoteObject> remainingBenchmark;

    private ConcurrentHashMap<URI, RemoteRemoteObject> receivedRROS;

    private ConcurrentHashMap<URI, Integer> benchmarkDone;

    private ConcurrentHashMap<URI, Integer> previousBenchmarkResults;

    private boolean finished = false;

    private Thread thread;

    public BenchmarkMonitorThread(Map<URI, RemoteRemoteObject> remoteObjectUrls, String benchmarkObjectclazz) {
        LOGGER_RO.debug("[Multi-protocol] Benchmark choose : " + benchmarkObjectclazz);
        this.remainingBenchmark = new ConcurrentHashMap<URI, RemoteRemoteObject>();
        this.receivedRROS = new ConcurrentHashMap<URI, RemoteRemoteObject>();
        HashSet<String> uniq = new HashSet<String>();
        for (URI uri : remoteObjectUrls.keySet()) {
            if (!uniq.contains(uri.getScheme())) {
                this.remainingBenchmark.put(uri, remoteObjectUrls.get(uri));
                uniq.add(uri.getScheme());
            }
        }
        this.benchmarkDone = new ConcurrentHashMap<URI, Integer>();
        this.previousBenchmarkResults = new ConcurrentHashMap<URI, Integer>();
        this.clazz = benchmarkObjectclazz;
    }

    /**
     * Add a new list of RemoteRemoteObject to the test, for uris who are not already processing
     * The addition is done on-the-fly, i.e. the benchmark thread will be updated right away with new benchmarks to do
     */
    public boolean addOnTheFly(Map<URI, RemoteRemoteObject> remoteObjects, Map<URI, Integer> lastBenchmarkResults) {
        boolean restart = false;
        previousBenchmarkResults.putAll(lastBenchmarkResults);
        for (URI uri1 : remoteObjects.keySet()) {
            // it is not necessary to synchronize the following sequence of instructions are the other thread first put, then remove
            if (remainingBenchmark.containsKey(uri1) || benchmarkDone.containsKey(uri1)) {
                // Benchmark is already done for this protocol
            } else {
                remainingBenchmark.put(uri1, remoteObjects.get(uri1));
                receivedRROS.put(uri1, remoteObjects.get(uri1));
            }
        }

        if (restart) {
            this.launchBenchmark();
        } else {
            // If benchmark needs not to be restarted, simply return results to the Observers
            this.notifyAdd();
        }
        return restart;
    }

    @SuppressWarnings("unchecked")
    public class BenchmarkThread implements Runnable {
        public void run() {
            finished = false;
            try {
                while (true) {
                    LOGGER_RO.debug("[Multi-protocol] Starting benchmark");
                    while (!remainingBenchmark.isEmpty()) {
                        URI uri = null;
                        // Use iterator for being able to remove RemoteRemoteObject from the Collection remainingBenchmark when done
                        for (Iterator<Entry<URI, RemoteRemoteObject>> iter = remainingBenchmark.entrySet()
                                                                                               .iterator(); iter.hasNext();) {

                            Class<BenchmarkObject> benchmarkClass = null;
                            try {
                                benchmarkClass = (Class<BenchmarkObject>) Class.forName(clazz);
                            } catch (ClassNotFoundException e1) {
                                throw new ProActiveRuntimeException("[Multi-Protocol] The class \"" + clazz +
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
                                    methods[4].invoke(benchmark,
                                                      CentralPAPropertyRepository.PA_BENCHMARK_PARAMETER.getValue());
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
                                                    (result.intValue() == RemoteObjectSet.NOCHANGE_VALUE ? "OK"
                                                                                                         : result.intValue()));
                                }
                                if (result.intValue() == RemoteObjectSet.NOCHANGE_VALUE &&
                                    previousBenchmarkResults.containsKey(uri) &&
                                    previousBenchmarkResults.get(uri) != RemoteObjectSet.UNREACHABLE_VALUE) {
                                    // in case the value is 0, we keep the previous bench value, unless it is -infinity
                                    benchmarkDone.put(uri, previousBenchmarkResults.get(uri));
                                } else {
                                    benchmarkDone.put(uri, result.intValue());
                                }
                                iter.remove();
                            } catch (NullPointerException npe) {
                                handleProtocolException(npe, uri, iter);
                            } catch (ProtocolException pe) {
                                handleProtocolException(pe, uri, iter);
                            } catch (IOException ioe) {
                                handleProtocolException(ioe, uri, iter);
                            } catch (ProActiveRuntimeException e) {
                                handleProtocolException(e, uri, iter);
                            } catch (ProActiveException e) {
                                handleProtocolException(e, uri, iter);
                                // Reflection part, abort the process
                            } catch (IllegalArgumentException e) {
                                e.printStackTrace();
                                return;
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                                return;
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                                return;
                            }
                        }
                    }
                    setChanged();
                    notifyObservers(benchmarkDone);

                    // Wait the time specified by the period and restart
                    Thread.sleep(CentralPAPropertyRepository.PA_BENCHMARK_PERIOD.getValue());
                    benchmarkDone.clear();
                    remainingBenchmark.clear();
                    remainingBenchmark.putAll(receivedRROS);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            deleteObservers();
            finished = true;
        }

        private void handleProtocolException(Exception e, URI uri, Iterator<Entry<URI, RemoteRemoteObject>> iter) {
            LOGGER_RO.warn("[Multi-Protocol] Benchmark result for " + uri + " is : UNACCESSIBLE");
            LOGGER_RO.debug("", e);
            // the order, first put, then remove should be kept to avoid the need of synchronizing the addOnTheFly thread
            benchmarkDone.put(uri, RemoteObjectSet.UNREACHABLE_VALUE);
            iter.remove();
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
            this.notifyObservers(benchmarkDone);
            deleteObservers();
        }
    }
}

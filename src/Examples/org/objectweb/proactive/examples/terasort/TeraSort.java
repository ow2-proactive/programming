/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package org.objectweb.proactive.examples.terasort;

import java.io.File;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.api.PALifeCycle;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.objectweb.proactive.extensions.dataspaces.api.PADataSpaces;
import org.objectweb.proactive.extensions.dataspaces.api.RandomAccessContent;
import org.objectweb.proactive.extensions.dataspaces.api.RandomAccessMode;
import org.objectweb.proactive.extensions.dataspaces.exceptions.ConfigurationException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.NotConfiguredException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.SpaceNotFoundException;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;


public class TeraSort {
    static final private Logger logger = ProActiveLogger.getLogger("terasort");

    public static void main(String[] args) throws ProActiveException, FileSystemException {
        long before = System.currentTimeMillis();
        new TeraSort(args[0]);
        long after = System.currentTimeMillis();
        logger.info("Time elapsed: " + (after - before) / 1000 + " seconds");
    }

    public TeraSort() {
    }

    public TeraSort(String gcmaFile) throws ProActiveException, FileSystemException {
        GCMApplication gcma = PAGCMDeployment.loadApplicationDescriptor(new File(gcmaFile));
        GCMVirtualNode vn = gcma.getVirtualNode("workers");
        gcma.startDeployment();
        gcma.waitReady();

        List<Node> nodes = vn.getCurrentNodes();

        Master m = (Master) PAActiveObject.newActive(Master.class.getName(), new Object[] { nodes });
        PAFuture.waitFor(m.sort());

        gcma.kill();
        PALifeCycle.exitSuccess();
    }

    /**
     * Manages the sort:
     * <ul>
     *  <li>Creates the {@link Dispatcher}s and {@link Sorter}s</li>
     *  <li>Start the dispatch process (global ordering)</li>
     *  <li>Start the sorting process (local sort)
     * </ul>
     */
    public static class Master implements Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = 500L;
        private List<Node> nodes;
        private Dispatcher[] dispatchers;
        private Sorter[] sorters;

        public Master() {
        }

        public Master(List<Node> nodes) throws ActiveObjectCreationException, NodeException,
                SpaceNotFoundException, NotConfiguredException, ConfigurationException, FileSystemException {
            this.nodes = nodes;
            this.dispatchers = new Dispatcher[nodes.size()];
        }

        public BooleanWrapper sort() throws SpaceNotFoundException, NotConfiguredException,
                ConfigurationException, FileSystemException, ActiveObjectCreationException, NodeException {
            DataSpacesFileObject foIn = PADataSpaces.resolveDefaultInput("input.data");

            long size = foIn.getContent().getSize();
            long nbRows = size / 100; // 1 record == 100 bytes
            long rowPerWorker = nbRows / nodes.size();

            foIn.close();
            foIn = null;

            // Deploy the sorters
            sorters = new Sorter[nodes.size()];
            for (int i = 0; i < nodes.size(); i++) {
                Sorter s = (Sorter) PAActiveObject.newActive(Sorter.class.getName(), new Object[] { i },
                        nodes.get(i));
                sorters[i] = s;
            }

            // Deploy the workers
            for (int i = 0; i < nodes.size() - 1; i++) {
                Dispatcher w = (Dispatcher) PAActiveObject.newActive(Dispatcher.class.getName(),
                        new Object[] { i, i * rowPerWorker, rowPerWorker, sorters }, nodes.get(i));
                dispatchers[i] = w;
            }

            int i = nodes.size() - 1;
            Dispatcher w = (Dispatcher) PAActiveObject.newActive(Dispatcher.class.getName(), new Object[] {
                    i, i * rowPerWorker, nbRows - (i * rowPerWorker), sorters }, nodes.get(i));
            dispatchers[i] = w;

            // Dispatch Barrier 
            LinkedList<BooleanWrapper> futures = new LinkedList<BooleanWrapper>();
            for (Dispatcher worker : dispatchers) {
                futures.add(worker.dispatch());
            }
            PAFuture.waitForAll(futures);

            futures.clear();

            // Sort Barrier
            for (Sorter sorter : sorters) {
                futures.add(sorter.sort());
            }
            PAFuture.waitForAll(futures);

            return new BooleanWrapper(true);
        }

    }

    /** A terasort record.
     * 
     * The sole purpose of this class is to have Comparable object to be able to perform a
     * Collection.sort() and avoid to rewrite our own merge sort.
     * 
     * The drawback is the overhead of creating the record objects (memory + GC).
     */
    final static class Record implements Comparable<Record>, Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = 500L;
        private byte[] buf;

        public Record(byte[] line) {
            this.buf = line;
        }

        public int compareTo(Record o) {
            return compareBytes(this.buf, 0, 10, o.buf, 0, 10);
        }

        private int compareBytes(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
            int end1 = s1 + l1;
            int end2 = s2 + l2;
            for (int i = s1, j = s2; i < end1 && j < end2; i++, j++) {
                int a = (b1[i] & 0xff);
                int b = (b2[j] & 0xff);
                if (a != b) {
                    return a - b;
                }
            }
            return l1 - l2;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append('[');

            for (int i = 0; i < 9; i++) {
                sb.append(buf[i]);
                sb.append(", ");
            }
            sb.append(buf[9]);
            sb.append("] == ");
            sb.append(byteArrayToUnsignedInt(buf));

            return sb.toString();
        }
    }

    /**
     * Sort a given partition of record.
     * 
     * The partition must fit in memory or an OOM will occur.
     */
    public static class Sorter implements Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = 500L;
        private int id;
        private ArrayList<Record> lines;

        public Sorter() {
        }

        public Sorter(int id) {
            this.id = id;
            this.lines = new ArrayList<Record>();
        }

        public void add(List<Record> newLines) {
            this.lines.addAll(newLines);
        }

        public BooleanWrapper sort() {
            try {
                Collections.sort(lines);

                DataSpacesFileObject fo = PADataSpaces.resolveDefaultOutput("out_" + id);
                fo.createFile();
                OutputStream os = fo.getContent().getOutputStream(false);
                for (Record line : lines) {
                    os.write(line.buf);
                }

                os.close();
            } catch (Throwable t) {
                logger.error("ERROR", t);
            }

            return new BooleanWrapper(true);
        }
    }

    /**
     * Read a chunk of data and send each record to a given Sorter. 
     * 
     * The ASCII terasort is not yet supported.
     * We assume a normal distribution of the keys
     */
    public static class Dispatcher implements Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = 500L;

        private Sorter[] sorters;

        private LinkedList<Record>[] buf;
        private int bufIndex;
        private int id;
        private long start;
        private long size;

        public Dispatcher() {
        }

        public Dispatcher(int id, long offset, long size, Sorter[] sorters) {
            logger.info("Worker " + id + " starting. Offset: " + offset + " size: " + size);
            this.id = id;
            this.start = offset;
            this.size = size;
            this.sorters = sorters;
            this.buf = new LinkedList[sorters.length];
            for (int i = 0; i < sorters.length; i++) {
                this.buf[i] = new LinkedList<Record>();
            }
            this.bufIndex = 0;
        }

        public int assign(int nbSorter, byte[] line) {
            long step = (long) ((1L << 32) / nbSorter);

            long value = byteArrayToUnsignedInt(line);
            int ret = (int) (value / step);
            return ret;

        }

        public BooleanWrapper dispatch() {
            try {
                DataSpacesFileObject foIn = PADataSpaces.resolveDefaultInput("input.data");
                RandomAccessContent rac = foIn.getContent()
                        .getRandomAccessContent(RandomAccessMode.READ_ONLY);

                rac.seek(start * 100);

                for (long offset = start; offset < start + size; offset++) {
                    byte[] line = new byte[100];
                    rac.readFully(line);
                    int dest = assign(sorters.length, line);

                    buf[dest].add(new Record(line));

                    bufIndex++;
                    if (bufIndex == 100000) {
                        flushBuf();
                    }
                }

                flushBuf();
                logger.info("DONE");

            } catch (Throwable e) {
                logger.error("ERROR", e);
            }

            return new BooleanWrapper(true);
        }

        private void flushBuf() {
            for (int i = 0; i < sorters.length; i++) {
                if (!buf[i].isEmpty()) {
                    sorters[i].add(buf[i]);
                }
                buf[i].clear();
            }

            bufIndex = 0;
        }
    }

    static public long byteArrayToUnsignedInt(final byte[] buf) {
        long ret = ((long) (buf[0] & 0xFF) << 24);
        ret |= ((long) (buf[1] & 0xFF) << 16);
        ret |= ((long) (buf[2] & 0xFF) << 8);
        ret |= ((long) (buf[3] & 0xFF));
        return ret;
    }
}

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
package org.objectweb.proactive.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * StackTraceUtil
 *
 * Simple utilities to return the stack trace of an
 * exception as a String.
 *
 * @author The ProActive Team
**/
public final class StackTraceUtil {

    static String nl = System.getProperty("line.separator");

    public static String getStackTrace(Throwable aThrowable) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        aThrowable.printStackTrace(printWriter);
        return result.toString();
    }

    public static boolean equalsStackTraces(Throwable a, Throwable b) {
        if ((a == null) && (b == null)) {
            return true;
        }
        if (a == null) {
            return false;
        }
        if (b == null) {
            return false;
        }
        if (!a.getClass().equals(b.getClass())) {
            return false;
        }
        if ((a.getMessage() == null) && (b.getMessage() != null)) {
            return false;
        }
        if ((b.getMessage() == null) && (a.getMessage() != null)) {
            return false;
        }

        if ((a.getMessage() != null) && (b.getMessage() != null) && !a.getMessage().equals(b.getMessage())) {
            return false;
        }
        return equalsStackTraces(a.getCause(), b.getCause());

    }

    public static String getAllStackTraces() {
        Map<Thread, StackTraceElement[]> allThreads = Thread.getAllStackTraces();
        SortedSet<Thread> sortedThreads = new TreeSet<>(new Comparator<Thread>() {
            @Override
            public int compare(Thread o1, Thread o2) {
                // higher ids first
                return (int) (o2.getId() - o1.getId());
            }
        });
        sortedThreads.addAll(allThreads.keySet());
        StringBuffer stringBuffer = new StringBuffer();
        for (Thread thread : sortedThreads) {
            StackTraceElement[] trace = allThreads.get(thread);
            StringBuffer threadInfo = new StringBuffer();
            threadInfo.append("\"" + thread.getName() + "\"");
            threadInfo.append(" #" + thread.getId());
            if (thread.getThreadGroup() != null) {
                threadInfo.append(" group=" + thread.getThreadGroup().getName());
            }
            threadInfo.append(thread.isDaemon() ? " daemon" : "");
            threadInfo.append(" prio=" + thread.getPriority());

            threadInfo.append(" " + thread.getState());

            stringBuffer.append(threadInfo + nl);
            for (int i = 0; i < trace.length; i++) {
                stringBuffer.append(" " + trace[i] + nl);
            }
            stringBuffer.append(nl);
        }
        return stringBuffer.toString();

    }

    @SafeVarargs
    public static <T> T[] concatAll(T[] first, T[]... rest) {

        if (first == null) {
            throw new IllegalArgumentException("Unexpected array : " + first);
        }
        int totalLength = first.length;
        if (rest != null) {
            for (T[] array : rest) {
                if (array != null) {
                    totalLength += array.length;
                }
            }
        }
        T[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        if (rest != null) {
            for (T[] array : rest) {
                if (array != null) {
                    System.arraycopy(array, 0, result, offset, array.length);
                    offset += array.length;
                }
            }
        }
        return result;
    }

    @SuppressWarnings("all")
    public static void main(String[] args) {
        System.out.println(Arrays.asList(concatAll(new Integer[] { 1, 2, 3 }, new Integer[] { 4, 5, 6 })));
        System.out.println(Arrays.asList(concatAll(new Integer[] { 1, 2, 3 },
                                                   (Integer[]) null,
                                                   new Integer[] { 4, 5, 6 })));
        System.out.println(Arrays.asList(concatAll(new Integer[] { 1, 2, 3 },
                                                   (Integer[]) null,
                                                   new Integer[] { 4, 5, 6 },
                                                   null)));
        System.out.println(Arrays.asList(concatAll(new Integer[] { 1, 2, 3 }, (Integer[]) null)));
    }

}

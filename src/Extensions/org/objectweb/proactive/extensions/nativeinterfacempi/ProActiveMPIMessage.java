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
package org.objectweb.proactive.extensions.nativeinterfacempi;

import org.objectweb.proactive.extensions.nativeinterface.application.NativeMessage;


public class ProActiveMPIMessage implements java.io.Serializable, NativeMessage {

    private static final long serialVersionUID = 52;

    private int msg_type = 0;

    private int srcJobId = 0;

    private int destJobId = 0;

    // form sender of the message
    private int srcRank = 0;

    // receiver of message
    private int destRank = 0;

    // number of element in the message
    private int count = 0;

    // type of data in buffer
    private int datatype = 0;

    // special tag
    private int tag = 0;

    // name of the method called on user object
    private String method = null;

    //  name of the method called on user object
    private String clazz = null;

    // data
    private byte[] serializedPayload;

    // parameters call from native code
    private String parameters;

    private long timer = 0;

    // the tab of parameters
    private String[] params;

    public ProActiveMPIMessage() {

    }

    /////////////////////
    ///// SETTERS  //////
    /////////////////////
    public void setMsgType(int msgtype) {
        this.msg_type = msgtype;
    }

    public void setSrcJobID(int idJob) {
        this.srcJobId = idJob;
    }

    public void setDestJobID(int idJob) {
        this.destJobId = idJob;
    }

    public void setSerializedMsg(byte[] data) {
        //  this.data = new byte[data.length];
        this.serializedPayload = data;
    }

    public void parseParameters() {
        if (parameters != null) {
            this.params = parameters.split("\t");
        }
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setSrcRank(int src) {
        this.srcRank = src;
    }

    public void setDestRank(int dest) {
        this.destRank = dest;
    }

    public void setDatatype(int datatype) {
        this.datatype = datatype;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    /////////////////////
    ///// GETTERS  //////
    /////////////////////
    public int getMsgType() {
        return this.msg_type;
    }

    public int getSrcRank() {
        return this.srcRank;
    }

    public int getDestRank() {
        return this.destRank;
    }

    public int getSrcJobId() {
        return srcJobId;
    }

    public int getDestJobId() {
        return destJobId;
    }

    public String getMethod() {
        return method;
    }

    public String getClazz() {
        return clazz;
    }

    public int getDatatype() {
        return datatype;
    }

    public String[] getParams() {
        return params;
    }

    public byte[] getSerializedMessage() {
        return this.serializedPayload;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("\n ######## Message ######### ");
        sb.append("\n Class: ");
        sb.append(this.getClass().getName());
        sb.append("\n srcJobId: " + this.srcJobId);
        sb.append("\n destJobId: " + this.destJobId);
        sb.append("\n msg_type: " + this.msg_type);
        sb.append("\n Count: " + this.count);
        sb.append("\n src: " + this.srcRank);
        sb.append("\n dest: " + this.destRank);
        sb.append("\n datatype: " + this.datatype);
        sb.append("\n tag: " + this.tag);
        return sb.toString();
    }

    public String toString(String prefix) {
        StringBuffer sb = new StringBuffer();
        sb.append(prefix).append(" ######## Message ######### \n");
        sb.append(prefix).append(" Class: ").append(this.getClass().getName() + "\n");
        sb.append(prefix).append(" srcJobId: " + this.srcJobId + "\n");
        sb.append(prefix).append(" destJobId: " + this.destJobId + "\n");
        sb.append(prefix).append(" msg_type: " + this.msg_type + "\n");
        sb.append(prefix).append(" Count: " + this.count + "\n");
        sb.append(prefix).append(" src: " + this.srcRank + "\n");
        sb.append(prefix).append(" dest: " + this.destRank + "\n");
        sb.append(prefix).append(" datatype: " + this.datatype + "\n");
        sb.append(prefix).append(" tag: " + this.tag + "\n");
        return sb.toString();
    }

    public int getCount() {
        return count;
    }

    public long getTimer() {
        return timer;
    }

    public void setTimer(long timer) {
        this.timer = timer;
    }
}

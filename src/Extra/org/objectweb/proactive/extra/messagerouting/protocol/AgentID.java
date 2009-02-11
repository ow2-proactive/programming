package org.objectweb.proactive.extra.messagerouting.protocol;

import java.io.Serializable;


/** An unique identifier for Agents 
 *
 * Each client is identified by an unique {@link AgentID}. A client receive its
 * ID when it connects to the router for the first time. 
 * 
 * @since ProActive 4.1.0
 */
@SuppressWarnings("serial")
public class AgentID implements Serializable {
    final private long id;

    public AgentID(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return Long.toString(id);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AgentID other = (AgentID) obj;
        if (id != other.id)
            return false;
        return true;
    }

}

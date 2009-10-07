package org.objectweb.proactive.core.body.tags;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ObjectForSynchro;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Tag : Local Memory
 * This object allow to stock couple key/value on the current
 * active object usable by the Tag who create it.
 * 
 * Each LocalMemoryTag has a Lease time. When the lease is 
 * inferior to 0, the object is removed.
 * 
 * Each time there is an access on the localMemory, the lease
 * time is increased by the half value of the initial lease.
 */
public class LocalMemoryTag implements Serializable {

    static {
        LocalMemoryLeaseThread.start();
    }

    /** Message Tagging LocalMemory Logger */
    private static Logger logger = ProActiveLogger.getLogger(Loggers.MESSAGE_TAGGING_LOCALMEMORY);

    private String tagIDReferer;

    private int currentlease;
    private int leaseInc;

    private ObjectForSynchro lock;
    private Map<String, Object> memory;

    /**
     * Constructor
     * @param tagID - ID Of the Tag
     * @param lease - Lease time of this LocalMemory
     */
    public LocalMemoryTag(String tagID, int lease) {
        lock = new ObjectForSynchro();
        this.tagIDReferer = tagID;
        this.currentlease = lease;
        this.leaseInc = lease / 2;
        this.memory = new HashMap<String, Object>();
        if (logger.isDebugEnabled()) {
            logger.debug("New Tag LocalMemory for the tag " + tagID + " with a lease of " + lease);
        }
    }

    /**
     * Add a Key/Value to this local memory
     * @param key   - the Key
     * @param value - the Value
     */
    public void put(String key, Object value) {
        synchronized (lock) {
            this.currentlease += leaseInc;
        }
        this.memory.put(key, value);
        if (logger.isDebugEnabled()) {
            logger.debug("Put value in the Tag LocalMemory of " + tagIDReferer + " : key=" + key +
                ", value=" + value);
        }
    }

    /**
     * To get back a previous entry of this local memory
     * @param key - the Key to retrieve the value
     * @return the value if it exist, null otherwise.
     */
    public Object get(String key) {
        synchronized (lock) {
            this.currentlease += leaseInc;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Get value in the Tag LocalMemory of " + tagIDReferer + " : key=" + key);
        }
        return this.memory.get(key);
    }

    /**
     * To know if the lease time exceeded.
     * @return true if the lease value is <= 0
     */
    public boolean leaseExceeded() {
        if (logger.isDebugEnabled()) {
            logger.debug("Lease value under 0 for the Tag LocalMemory of " + tagIDReferer);
        }
        return currentlease <= 0;
    }

    /**
     * Decrement the current lease value
     * @param decValue - Value of the decrement
     */
    public void decCurrentLease(int decValue) {
        synchronized (lock) {
            this.currentlease -= decValue;
            if (logger.isDebugEnabled()) {
                logger.debug("Decrement lease value of the Tag LocalMemory of " + tagIDReferer +
                    ": new lease value = " + currentlease);
            }
        }
    }

    /**
     * To get the Tag Id to which is attached this local memory
     * @return
     */
    public String getTagIDReferer() {
        return this.tagIDReferer;
    }

}

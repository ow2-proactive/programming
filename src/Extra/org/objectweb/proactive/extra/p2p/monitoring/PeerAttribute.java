package org.objectweb.proactive.extra.p2p.monitoring;

public class PeerAttribute implements Comparable<PeerAttribute> {

    protected String name;
    protected String value;

    public PeerAttribute(String n, String v) {
        this.name = n;
        this.value = v;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String toString() {
        return this.name + " : " + this.value;
    }

    public int compareTo(PeerAttribute o) {
        return this.name.compareTo(((PeerAttribute) o).getName());

    }

}

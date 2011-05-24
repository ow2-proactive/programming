package functionalTests.multiactivities.quicksort;

import java.io.Serializable;

public class Region implements Serializable {
    public int from;
    public int to;
    
    public Region(int from, int to) {
        this.from = from;
        this.to = to;
    }

    void setFromTo(int from, int to){
        this.from = from;
        this.to = to;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public void setTo(int to) {
        this.to = to;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Region) {
            Region o = (Region) obj;
            if (this.from>o.from && this.from<o.to) return false;
            if (this.to>o.from && this.to<o.to) return false;
            
            return true;
        }
        return false;
    }
    
    @Override
    public String toString() {
        return from+"-"+to;
    }
    
}
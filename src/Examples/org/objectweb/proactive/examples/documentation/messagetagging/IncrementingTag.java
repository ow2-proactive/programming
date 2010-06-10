package org.objectweb.proactive.examples.documentation.messagetagging;

import org.objectweb.proactive.core.body.tags.Tag;


// @snippet-start IncrementingTag
public class IncrementingTag extends Tag {

    /**
     * 
     */
    private static final long serialVersionUID = 430L;
    private Integer depth;

    public IncrementingTag(String id) {
        super(id);
        this.depth = 0;
    }

    @Override
    public Tag apply() {
        this.depth++;
        return this;
    }

    public Integer getDepth() {
        return this.depth;
    }

}
//@snippet-end IncrementingTag
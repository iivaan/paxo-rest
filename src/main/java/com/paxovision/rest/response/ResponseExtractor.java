package com.paxovision.rest.response;

import javax.annotation.Nullable;

/** Object wrapper, which can be set only once */
public class ResponseExtractor {
    private Object result;
    private boolean extracted = false;

    public void setValue(Object result) {
        if (extracted) {
            throw new UnsupportedOperationException("This ResponseExtractor already contain value!");
        }
        this.result = result;
        this.extracted = true;
    }

    @Nullable
    public Object getValue(){
        return result;
    }

    public boolean isExtracted(){
        return extracted;
    }
}

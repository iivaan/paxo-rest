package com.paxovision.rest.assertions;

public abstract class Matchers<D> {
    protected D matchingObject;

    public Matchers(D object) {
        this.matchingObject = object;
    }

    public D getMatchingObject() {
        return matchingObject;
    }

    public abstract <T> T match();
}

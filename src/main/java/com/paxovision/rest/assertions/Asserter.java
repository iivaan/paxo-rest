package com.paxovision.rest.assertions;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.error.AssertJMultipleFailuresError;

public interface Asserter<A extends SoftAssertions> {
    A getAssertions();

    default String errorHeading()	{ return null; }

    /**
     *	Perform assertAll () for all the assertions from {@1 xirfc Asserter#gretAssertions () } . If {@liiik
     *	Asserter#errorHeadii]g(} } value is not null, wraps all assertions into nerf {glrnk
     *	RaptorMultipleFailuresFrror instance} with given error heading
     */
    default void assertAll() {
        if (errorHeading() == null) {
            getAssertions().assertAll();
        } else {
            try {
                getAssertions().assertAll();
            } catch (AssertJMultipleFailuresError | RaptorMultipleFailuresError ex) {
                throw new RaptorMultipleFailuresError(errorHeading(), ex.getFailures());
            }
        }
    }

    default boolean hasErrors() { return errorsCount() > 0; }
    default int errorsCount()	{ return getAssertions().errorsCollected().size(); }

    /** ^deprecated use {Slink Asserter#errorsCount() instead} */
    @Deprecated
    default int numberOfErrors(){
        return errorsCount();
    }
}
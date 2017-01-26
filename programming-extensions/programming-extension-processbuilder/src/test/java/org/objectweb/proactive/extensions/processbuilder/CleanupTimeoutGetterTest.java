package org.objectweb.proactive.extensions.processbuilder;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


import org.junit.Test;

public class CleanupTimeoutGetterTest {

    private static final long CLEANUP_TIME_DEFAULT_SECONDS = 10;
    private static final String CLEANUP_TIME_PROPERTY_NAME = "proactive.process.builder.cleanup.time.seconds";

    @Test
    public void testThatDefaultTimeoutIsReturnedIfNoPropertyIsSet() {
        testThatReturnedTimeoutIs(CLEANUP_TIME_DEFAULT_SECONDS);
    }

    @Test
    public void testThatDefaultTimeoutIsReturnedIfPropertyIsSetToGarbage() {
        System.setProperty(CLEANUP_TIME_PROPERTY_NAME, "bahasd3342");
        testThatReturnedTimeoutIs(CLEANUP_TIME_DEFAULT_SECONDS);
        System.clearProperty(CLEANUP_TIME_PROPERTY_NAME);
    }

    @Test
    public void testThatCorrectValueIsReturnedIfProperyIsSet() {
        System.setProperty(CLEANUP_TIME_PROPERTY_NAME, "15");
        testThatReturnedTimeoutIs(15L);
        System.clearProperty(CLEANUP_TIME_PROPERTY_NAME);
    }

    private void testThatReturnedTimeoutIs(long expectedTimeout) {

        CleanupTimeoutGetter cleanupTimeoutGetter =
                new CleanupTimeoutGetter();
        assertThat(cleanupTimeoutGetter.getCleanupTimeSeconds(), is(expectedTimeout));
    }

}
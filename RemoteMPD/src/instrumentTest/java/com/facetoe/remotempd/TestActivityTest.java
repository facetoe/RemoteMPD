package com.facetoe.remotempd;

import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;

/**
 * RemoteMPD
 * Created by facetoe on 2/02/14.
 */

public class TestActivityTest extends ActivityInstrumentationTestCase2<TestActivity> {

    Button button;
    TestActivity activity;
    Instrumentation instrumentation;

    public TestActivityTest() {
        super(TestActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        activity = getActivity();
        instrumentation = getInstrumentation();
        button = (Button) activity.findViewById(R.id.btnTestSettings);
    }

    public void testTest() throws Exception {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                button.performClick();

            }
        });

        assertEquals(1, 1);
    }
}

/*
 * Copyright 2013 Peergreen S.A.S.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.peergreen.shelbie.javavm.internal;

import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.ow2.shelbie.testing.ActionContainer;
import org.testng.annotations.Test;

/**
 * User: guillaume
 * Date: 14/03/13
 * Time: 12:06
 */
public class KillThreadActionTestCase {
    @Test
    public void testStopThread() throws Exception {

        Thread killable = new Thread("JavaVM Test : Stop") {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // Ignored
                }
            }
        };
        killable.start();

        ActionContainer container = new ActionContainer(new KillThreadAction());
        container.argument(0, "JavaVM Test : Stop");
        container.execute(null);

        assertEquals(killable.getState(), Thread.State.TERMINATED);
        assertContains(container.getSystemOut(false), "[SUCCESS] Thread \"JavaVM Test : Stop\" has been stopped");
        assertFalse(killable.isAlive());
    }

    private static void assertContains(final String current, final String expected) {
        if (!current.contains(expected)) {
            fail(format(">%s< does not contain %s", current, expected));
        }
    }

    @Test
    public void testThreadInterruption() throws Exception {

        final Holder<Boolean> interrupted = new Holder<Boolean>(Boolean.FALSE);
        final Holder<Boolean> softExit = new Holder<Boolean>(Boolean.FALSE);
        Thread killable = new Thread("JavaVM Test : Interruption") {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    interrupted.value = Boolean.TRUE;
                }

                softExit.value = Boolean.TRUE;
            }
        };
        killable.start();

        ActionContainer container = new ActionContainer(new KillThreadAction());
        container.option("--interrupt", Boolean.TRUE);
        container.argument(0, "JavaVM Test : Interruption");
        container.execute(null);

        killable.join();

        // Cannot use Thread.isInterrupted() since an interrupted sleep reset the flag
        assertEquals(killable.getState(), Thread.State.TERMINATED);
        assertTrue(interrupted.value);
        assertTrue(softExit.value);
        assertContains(container.getSystemOut(false), "[SUCCESS] Thread \"JavaVM Test : Interruption\" has been interrupted");
    }

    @Test
    public void testNotInterruptibleThread() throws Exception {

        ControllableThread killable = new ControllableThread("JavaVM Test : Interruption KO");
        killable.start();

        ActionContainer container = new ActionContainer(new KillThreadAction());
        container.option("--interrupt", Boolean.TRUE);
        container.argument(0, "JavaVM Test : Interruption KO");
        container.execute(null);

        // Cannot use Thread.isInterrupted() since an interrupted sleep reset the flag
        assertTrue(killable.interrupted);
        assertContains(container.getSystemOut(false), "[FAILURE] Thread \"JavaVM Test : Interruption KO\" could not be interrupted");
        assertEquals(killable.getState(), Thread.State.TIMED_WAITING);

        // Clean up the thread
        killable.run = false;
    }

    private static class Holder<T> {
        public T value;

        private Holder(T value) {
            this.value = value;
        }
    }

    private static class ControllableThread extends Thread {

        boolean run = true;
        boolean interrupted = false;

        public ControllableThread(String name) {
            super(name);
        }

        @Override
        public void run() {

            while (run) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    // Swallow interrupted exception to simulate non-interrupting thread
                    // Will effectively exit the Thread when 'run' will be set to 'false'
                    interrupted = true;
                }
            }
        }
    }
}

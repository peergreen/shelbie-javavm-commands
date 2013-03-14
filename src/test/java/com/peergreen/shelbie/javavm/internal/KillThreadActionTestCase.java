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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

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

        Thread killable = new Thread("Killable") {
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
        container.argument(0, "Killable");
        container.execute(null);

        assertEquals(killable.getState(), Thread.State.TERMINATED);
        assertFalse(killable.isAlive());
    }

    @Test
    public void testThreadInterruption() throws Exception {

        final Holder<Boolean> interrupted = new Holder<Boolean>(Boolean.FALSE);
        final Holder<Boolean> softExit = new Holder<Boolean>(Boolean.FALSE);
        Thread killable = new Thread("Killable") {
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
        container.argument(0, "Killable");
        container.execute(null);

        // Cannot use Thread.isInterrupted() since an interrupted sleep reset the flag
        assertTrue(interrupted.value);
        assertTrue(softExit.value);
        assertEquals(killable.getState(), Thread.State.TERMINATED);
    }

    private static class Holder<T> {
        public T value;

        private Holder(T value) {
            this.value = value;
        }
    }
}

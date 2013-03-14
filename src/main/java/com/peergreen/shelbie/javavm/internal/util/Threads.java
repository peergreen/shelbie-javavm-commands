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

package com.peergreen.shelbie.javavm.internal.util;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;

/**
 * User: guillaume
 * Date: 20/02/13
 * Time: 11:54
 */
public class Threads {

    private Threads() {}

    public static String color(Thread.State state) {
        if (state == Thread.State.BLOCKED || state == Thread.State.TERMINATED) {
            return "red";
        }
        if (state == Thread.State.RUNNABLE) {
            return "yellow";
        }

        return "green";
    }

    public static ThreadGroup getRootThreadGroup() {
        ThreadGroup group = Thread.currentThread().getThreadGroup();
        ThreadGroup parent;
        while ((parent = group.getParent()) != null) {
            group = parent;
        }
        return group;
    }

    public static Thread[] getAllThreads( ) {
        ThreadGroup root = getRootThreadGroup();
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        int nAlloc = bean.getThreadCount();
        int n = 0;
        Thread[] threads;
        do {
            nAlloc *= 2;
            threads = new Thread[nAlloc];
            n = root.enumerate(threads, true);
        } while (n == nAlloc);
        return Arrays.copyOf(threads, n);
    }

    public static Thread getThread(long threadId) {
        for (Thread thread : getAllThreads()) {
            if (threadId == thread.getId()) {
                return thread;
            }
        }
        // System thread, not visible using Thread API
        return null;
    }

    public static Thread getThread(String name) {
        for (Thread thread : getAllThreads()) {
            if (name.equals(thread.getName())) {
                return thread;
            }
        }
        // System thread, not visible using Thread API
        return null;
    }
}

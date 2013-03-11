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

import java.lang.management.LockInfo;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;

import org.apache.felix.gogo.commands.Action;
import org.fusesource.jansi.Ansi;

import com.peergreen.shelbie.javavm.internal.util.Threads;

/**
 * User: guillaume
 * Date: 20/02/13
 * Time: 17:25
 */
public abstract class AbstractThreadAction implements Action {
    protected void printStackTrace(Ansi buffer, ThreadInfo threadInfo) {

        LockInfo[] lockedSynchronizers = threadInfo.getLockedSynchronizers();
        for (LockInfo lockedSynchronizer : lockedSynchronizers) {
            buffer.render("  Owned synchronizer <@|bold %#016xd|@> (a %s)", lockedSynchronizer.getIdentityHashCode(), lockedSynchronizer.getClassName());
            buffer.newline();
        }

        StackTraceElement[] stackTraceElements = threadInfo.getStackTrace();
        MonitorInfo[] lockedMonitors = threadInfo.getLockedMonitors();

        int index = 0;
        for (StackTraceElement element : stackTraceElements) {

            String info = element.isNativeMethod() ? "Native Method" : format("%s:%s", element.getFileName(), element.getLineNumber());
            buffer.render(format("    @|faint at|@ %s.%s(@|underline %s|@)", element.getClassName(), element.getMethodName(), info));
            buffer.newline();

            if (index == 0) {
                // First frame, maybe we have a lock
                LockInfo lockInfo = threadInfo.getLockInfo();
                if (lockInfo != null) {
                    // - waiting to lock <0x000000016ddfe1e0> (a java.lang.String)
                    // - parking to wait for  <0x0000000170554520> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
                    // - waiting on <0x00000001703300d8> (a org.eclipse.osgi.framework.internal.core.Framework)
                    buffer.render("    - ");
                    if (isParking(element)) {
                        buffer.render("parking to wait for ");
                    } else if (isWaiting(element)) {
                        buffer.render("waiting on ");
                    } else {
                        buffer.render("waiting to lock ");
                    }
                    buffer.render("<@|bold %#016x|@> (a %s)", lockInfo.getIdentityHashCode(), lockInfo.getClassName());
                    buffer.newline();
                }
            }

            MonitorInfo monitor = findMonitor(lockedMonitors, index);
            if (monitor != null) {
                //- locked <0x000000016ddfe220> (a java.lang.String)
                buffer.render("    - locked <@|bold %#016x|@> (a %s)", monitor.getIdentityHashCode(), monitor.getClassName());
                buffer.newline();
            }

            // Move the cursor
            index++;
        }
    }

    private MonitorInfo findMonitor(MonitorInfo[] monitors, int index) {
        for (MonitorInfo monitor : monitors) {
            if (monitor.getLockedStackDepth() == index) {
                return monitor;
            }
        }
        return null;
    }

    protected void printThreadInfo(Ansi buffer, ThreadInfo info) {

        // Sample expected output (from a kill -3 on Oracle Java SE)
        // "process reaper" daemon prio=5 tid=0x00007fd76a24c800 nid=0x6c03 waiting on condition [0x0000000157b77000]
        //   java.lang.Thread.State: TIMED_WAITING (parking)

        buffer.render(format("\"@|bold %s|@\" ", info.getThreadName()));

        // System threads, hidden with the Thread API, but visible through the MBean
        // daemon and priority not available
        Thread thread = Threads.getThread(info.getThreadId());
        if (thread != null) {
            if (thread.isDaemon()) {
                buffer.render("@|bold daemon|@ ");
            }
            buffer.render("prio=@|bold %d|@ ", thread.getPriority());
        }

        buffer.render(format("tid=@|bold %#016x|@", info.getThreadId()));
        buffer.newline();
        buffer.render(format("  %s: @|bold,%s %s|@", Thread.State.class.getName(), Threads.color(info.getThreadState()), info.getThreadState().name()));
        if (isSleeping(info)) {
            buffer.render(" (@|bold sleeping|@)");
        } else if (isParking(info)) {
            buffer.render(" (@|bold parking|@)");
        } else if (isOnObjectMonitor(info)) {
            buffer.render(" (@|bold on object monitor|@)");
        }
        buffer.newline();
    }

    private boolean isOnObjectMonitor(ThreadInfo info) {
        return info.getLockInfo() != null;
    }

    private boolean isSleeping(ThreadInfo info) {
        return info.getStackTrace().length > 0 && isSleeping(info.getStackTrace()[0]);
    }

    private boolean isParking(ThreadInfo info) {
        return info.getStackTrace().length > 0 && isParking(info.getStackTrace()[0]);
    }

    private boolean isSleeping(StackTraceElement element) {
        return Thread.class.getName().equals(element.getClassName()) && "sleep".equals(element.getMethodName());
    }

    private boolean isWaiting(StackTraceElement element) {
        return Object.class.getName().equals(element.getClassName()) && "wait".equals(element.getMethodName());
    }

    private boolean isParking(StackTraceElement element) {
        return "sun.misc.Unsafe".equals(element.getClassName()) && "park".equals(element.getMethodName());
    }
}

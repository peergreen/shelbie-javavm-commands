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
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

import org.apache.felix.gogo.commands.Command;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.HandlerDeclaration;
import org.apache.felix.service.command.CommandSession;
import org.fusesource.jansi.Ansi;

import com.peergreen.shelbie.javavm.internal.util.Threads;

/**
 * Display deadlocks information (if any).
 * @author Guillaume Sauthier
 */
@Component
@Command(name = "dead-locks",
         scope = "javavm",
         description = "Display deadlocks (if any)")
@HandlerDeclaration("<sh:command xmlns:sh='org.ow2.shelbie'/>")
public class DeadLocksAction extends AbstractThreadAction {

    public Object execute(final CommandSession session) throws Exception {

        Ansi buffer = Ansi.ansi();

        ThreadMXBean mbean = ManagementFactory.getThreadMXBean();
        long[] ids = mbean.findDeadlockedThreads();

        if (ids == null) {
            System.out.printf("No deadlocks found.");
            return null;
        }

        // Found a deadlock
        buffer.render("@|bold,red %d Threads|@ are involved in a deadlock situation: ", ids.length);
        for (int i = 0 ; i < ids.length; i++) {
            if (i != 0) {
                buffer.a(", ");
            }
            buffer.render("\"@|bold %s|@\"", mbean.getThreadInfo(ids[i]).getThreadName());
        }
        buffer.newline();
        buffer.newline();


        ThreadInfo[] lockedThreads = mbean.getThreadInfo(ids, true, true);
        for (ThreadInfo threadInfo : lockedThreads) {
            // Print some general thread info
            buffer.render(format("Thread \"@|bold %s|@\" (@|faint %016x|@) [@|%s %s|@]",
                    threadInfo.getThreadName(),
                    threadInfo.getThreadId(),
                    Threads.color(threadInfo.getThreadState()),
                    threadInfo.getThreadState().name()));
            buffer.newline();
            MonitorInfo[] lockedMonitors = threadInfo.getLockedMonitors();
            if (lockedMonitors.length != 0) {
                buffer.render("  Locked monitors:");
                buffer.newline();
                for (MonitorInfo monitorInfo : lockedMonitors) {
                    buffer.render("  * @|bold %#016x|@ (%s)", monitorInfo.getIdentityHashCode(), monitorInfo.getClassName());
                    buffer.newline();

                    StackTraceElement lockedStackFrame = monitorInfo.getLockedStackFrame();
                    String method = lockedStackFrame.isNativeMethod() ? "Native method" : format("%s:%s", lockedStackFrame.getFileName(), lockedStackFrame.getLineNumber());
                    buffer.render("    @|faint in|@ %s.%s(@|underline %s|@)", lockedStackFrame.getClassName(), lockedStackFrame.getMethodName(), method);
                    buffer.newline();
                }
            }

            LockInfo[] lockedSynchronizers = threadInfo.getLockedSynchronizers();
            if (lockedSynchronizers.length != 0) {
                buffer.render("  Owned synchronizers:%n");
                for (LockInfo lockInfo : lockedSynchronizers) {
                    buffer.render("  * @|bold %#016x|@ (%s)", lockInfo.getIdentityHashCode(), lockInfo.getClassName());
                    buffer.newline();
                }
            }

            LockInfo lockInfo = threadInfo.getLockInfo();
            if (lockInfo != null) {
                buffer.render("  Waiting for resource @|bold %#016x|@ (%s)", lockInfo.getIdentityHashCode(), lockInfo.getClassName());
                buffer.render(" which is held by thread \"@|bold,red %s|@\" (id:%#016x)", threadInfo.getLockOwnerName(), threadInfo.getLockOwnerId());
                buffer.newline();
            }
        }

        buffer.newline();
        buffer.render("@|bold Detailed stack traces of involved Threads:|@");
        buffer.newline();
        buffer.newline();

        boolean first = true;
        for (ThreadInfo lockedThread : lockedThreads) {
            if (first) {
                first = false;
            } else {
                buffer.newline();
            }
            printThreadInfo(buffer, lockedThread);
            printStackTrace(buffer, lockedThread);
        }

        System.out.println(buffer.toString());

        return null;
    }

}
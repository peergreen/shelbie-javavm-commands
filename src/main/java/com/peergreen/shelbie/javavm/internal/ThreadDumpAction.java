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

import org.apache.felix.gogo.commands.Action;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.HandlerDeclaration;
import org.apache.felix.service.command.CommandSession;
import org.fusesource.jansi.Ansi;

import java.util.Map;

import static java.lang.String.format;

/**
 * List the Subsystems.
 */
@Component
@Command(name = "thread-dump",
         scope = "javavm",
         description = "Dump Thread(s) stack traces.")
@HandlerDeclaration("<sh:command xmlns:sh='org.ow2.shelbie'/>")
public class ThreadDumpAction implements Action {

    public Object execute(final CommandSession session) throws Exception {

        // TODO Use ThreadMXBean from the JVM for additional information
        // http://docs.oracle.com/javase/6/docs/api/java/lang/management/ThreadMXBean.html

        Ansi buffer = Ansi.ansi();

        boolean first = true;
        for (Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
            if (first) {
                first = false;
            } else {
                buffer.newline();
            }
            Thread thread = entry.getKey();
            printThreadInfo(buffer, thread);
            printStackTrace(buffer, entry.getValue());
        }

        System.out.println(buffer.toString());
        return null;
    }

    private void printStackTrace(Ansi buffer, StackTraceElement[] stackTraceElements) {

        /*
        	at sun.misc.Unsafe.park(Native Method)
	- parking to wait for  <0x0000000140033c20> (a java.util.concurrent.SynchronousQueue$TransferStack)
	at java.util.concurrent.locks.LockSupport.parkNanos(LockSupport.java:226)
	at java.util.concurrent.SynchronousQueue$TransferStack.awaitFulfill(SynchronousQueue.java:460)
	at java.util.concurrent.SynchronousQueue$TransferStack.transfer(SynchronousQueue.java:359)
	at java.util.concurrent.SynchronousQueue.poll(SynchronousQueue.java:942)
	at java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1043)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1103)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:603)
	at java.lang.Thread.run(Thread.java:722)

         */
        for (StackTraceElement element : stackTraceElements) {
            String info = element.isNativeMethod() ? "Native Method" : format("%s:%s", element.getFileName(), element.getLineNumber());
            buffer.render(format("    @|faint at|@ %s.%s(@|underline %s|@)%n", element.getClassName(), element.getMethodName(), info));
        }
    }

    private void printThreadInfo(Ansi buffer, Thread thread) {
        // "process reaper" daemon prio=5 tid=0x00007fd76a24c800 nid=0x6c03 waiting on condition [0x0000000157b77000]
        //   java.lang.Thread.State: TIMED_WAITING (parking)
        String daemon = thread.isDaemon() ? "@|bold daemon|@ " : "";
        buffer.render(format("\"@|bold %s|@\" %sprio=@|bold %d|@ tid=@|bold %#016x|@%n", thread.getName(), daemon, thread.getPriority(), thread.getId()));
        buffer.render(format("  %s: @|bold,%s %s|@%n", Thread.State.class.getName(), color(thread.getState()), thread.getState().name()));
    }

    private String color(Thread.State state) {
        if (state == Thread.State.BLOCKED || state == Thread.State.TERMINATED) {
            return "red";
        }
        if (state == Thread.State.RUNNABLE) {
            return "yellow";
        }

        return "green";
    }

}
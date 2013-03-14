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

import org.apache.felix.gogo.commands.Action;
import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.HandlerDeclaration;
import org.apache.felix.service.command.CommandSession;
import org.fusesource.jansi.Ansi;

import com.peergreen.shelbie.javavm.internal.util.Threads;

/**
 * Kill the given Thread.
 * @author Guillaume Sauthier
 */
@Component
@Command(name = "kill-thread",
         scope = "javavm",
         description = "Try to force Thread exit.")
@HandlerDeclaration("<sh:command xmlns:sh='org.ow2.shelbie'/>")
public class KillThreadAction implements Action {

    @Option(name = "-i",
            aliases = "--interrupt",
            description = "Try to interrupt the thread instead of stopping it")
    private boolean interrupt = false;

    @Argument(name = "thread-name",
              required = true,
              description = "Name of the Thread to be killed/interrupted")
    private String name;

    public Object execute(final CommandSession session) throws Exception {

        Thread thread = Threads.getThread(name);

        if (thread != null) {

            // TODO Need confirmation ?

            String action;
            if (interrupt) {
                thread.interrupt();
                action = "interrupted";
            } else {
                // Try to interrupt first (soft kill), then force stop
                thread.interrupt();
                thread.stop();
                action = "stopped";
            }

            Ansi buffer = Ansi.ansi();
            if (!thread.isAlive()) {
                // Successful termination
                buffer.render(format("[@|green,bold %S|@] ", "success"));
                buffer.render(format("Thread \"@|bold %s|@\" has been %s", thread.getName(), action));
            } else {
                // State did not change, stopping failed
                buffer.render(format("[@|red,bold %S|@] ", "failure"));
                buffer.render(format("Thread \"@|bold %s|@\" could not be %s", thread.getName(), action));
            }

            System.out.println(buffer.toString());
        }

        return null;
    }

}
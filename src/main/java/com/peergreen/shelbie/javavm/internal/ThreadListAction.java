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

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.regex.Pattern;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.HandlerDeclaration;
import org.apache.felix.service.command.CommandSession;
import org.fusesource.jansi.Ansi;

/**
 * List the threads.
 */
@Component
@Command(name = "thread-list",
         scope = "javavm",
         description = "List Thread(s) and their stack traces.")
@HandlerDeclaration("<sh:command xmlns:sh='org.ow2.shelbie'/>")
public class ThreadListAction extends AbstractThreadAction {

    @Argument(name = "pattern",
              description = "When used, printed Threads will be filtered (only Threads whose name is matching this value)")
    private String name;

    public Object execute(final CommandSession session) throws Exception {

        ThreadMXBean mbean = ManagementFactory.getThreadMXBean();

        Pattern pattern = Pattern.compile(".*");
        if (name != null) {
            pattern = Pattern.compile(".*" + name + ".*", Pattern.CASE_INSENSITIVE);
        }

        Ansi buffer = Ansi.ansi();

        boolean first = true;
        for (ThreadInfo threadInfo : mbean.dumpAllThreads(true, true)) {
            if (pattern.matcher(threadInfo.getThreadName()).matches()) {
                if (first) {
                    first = false;
                } else {
                    buffer.newline();
                }
                printThreadInfo(buffer, threadInfo);
                printStackTrace(buffer, threadInfo);
            }
        }

        System.out.println(buffer.toString());
        return null;
    }

}
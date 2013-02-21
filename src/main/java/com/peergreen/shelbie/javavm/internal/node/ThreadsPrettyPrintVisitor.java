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

package com.peergreen.shelbie.javavm.internal.node;

import static com.peergreen.shelbie.javavm.internal.util.Threads.color;
import static java.lang.String.format;

import java.io.PrintStream;

import org.fusesource.jansi.Ansi;

import com.peergreen.tree.Node;
import com.peergreen.tree.visitor.print.TreePrettyPrintNodeVisitor;

/**
 * User: guillaume
 * Date: 20/02/13
 * Time: 11:59
 */
public class ThreadsPrettyPrintVisitor extends TreePrettyPrintNodeVisitor<Object> {

    public ThreadsPrettyPrintVisitor(PrintStream stream) {
        super(stream);
    }

    @Override
    protected void doPrintInfo(PrintStream stream, Node<Object> objectNode) {
        Object o = objectNode.getData();
        if (o instanceof ThreadGroup) {
            ThreadGroup group = (ThreadGroup) o;
            Ansi buffer = Ansi.ansi();
            String daemon = group.isDaemon() ? " daemon" : "";
            buffer.render("ThreadGroup[@|bold %s|@]@|bold %s|@ max-priority:@|bold %d|@%n",
                    group.getName(),
                    daemon,
                    group.getMaxPriority());
            stream.printf(buffer.toString());
        } else if (o instanceof Thread) {
            Thread thread = (Thread) o;
            Ansi buffer = Ansi.ansi();
            String daemon = thread.isDaemon() ? " daemon" : "";
            buffer.render(format("Thread[@|bold %s|@]@|bold %s|@ priority:@|bold %d|@ (@|%s %s|@)%n",
                    thread.getName(),
                    daemon,
                    thread.getPriority(),
                    color(thread.getState()),
                    thread.getState().name()));
            stream.printf(buffer.toString());
        }

    }
}

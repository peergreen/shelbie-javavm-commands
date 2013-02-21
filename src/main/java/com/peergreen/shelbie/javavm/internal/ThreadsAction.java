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

import static com.peergreen.shelbie.javavm.internal.util.Threads.getRootThreadGroup;

import org.apache.felix.gogo.commands.Action;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.HandlerDeclaration;
import org.apache.felix.service.command.CommandSession;

import com.peergreen.shelbie.javavm.internal.node.ThreadGroupNodeAdapter;
import com.peergreen.shelbie.javavm.internal.node.ThreadsPrettyPrintVisitor;
import com.peergreen.tree.node.LazyNode;

/**
 * Display the hierarchy of Threads and ThreadGroup containers.
 * @author Guillaume Sauthier
 */
@Component
@Command(name = "threads",
         scope = "javavm",
         description = "Display the hierarchy of Threads and ThreadGroup containers.")
@HandlerDeclaration("<sh:command xmlns:sh='org.ow2.shelbie'/>")
public class ThreadsAction implements Action {

    public Object execute(final CommandSession session) throws Exception {

        // Build the initial Node and traverse it
        LazyNode<Object> node = new LazyNode<Object>(new ThreadGroupNodeAdapter(), getRootThreadGroup());
        node.walk(new ThreadsPrettyPrintVisitor(System.out));

        return null;
    }

}
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

import java.util.ArrayList;
import java.util.Collection;

import com.peergreen.tree.NodeAdapter;

/**
* User: guillaume
* Date: 20/02/13
* Time: 11:42
*/
public class ThreadGroupNodeAdapter implements NodeAdapter<Object> {
    @Override
    public Iterable<Object> getChildren(Object o) {
        Collection<Object> children = new ArrayList<Object>();
        if (o instanceof ThreadGroup) {
            ThreadGroup group = (ThreadGroup) o;

            ThreadGroup[] childGroup = new ThreadGroup[group.activeGroupCount()];
            group.enumerate(childGroup, false);
            for (ThreadGroup threadGroup : childGroup) {
                if (threadGroup != null) {
                    children.add(threadGroup);
                }
            }

            Thread[] childThread = new Thread[group.activeCount()];
            group.enumerate(childThread, false);
            for (Thread thread : childThread) {
                if (thread != null) {
                    children.add(thread);
                }
            }
        }
        return children;
    }
}

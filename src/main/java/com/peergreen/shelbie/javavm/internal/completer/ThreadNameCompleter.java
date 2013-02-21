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

package com.peergreen.shelbie.javavm.internal.completer;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;

import com.peergreen.shelbie.javavm.internal.util.Threads;
import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;

/**
 * Helper for active Thread names completion.
 */
@Component(propagation = true)
@Provides(specifications = Completer.class)
public class ThreadNameCompleter extends StringsCompleter {

    /**
     * Create a new StringsCompleter with a single possible completion
     * values.
     */
    public ThreadNameCompleter() {
        super("");
    }

    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {
        // Update candidates list
        getStrings().clear();
        getStrings().addAll(getThreadNames());
        return super.complete(buffer, cursor, candidates);
    }

    private Collection<String> getThreadNames() {
        // Use TreeSet to keep the structure sorted
        Set<String> names = new TreeSet<String>();
        for (Thread thread : Threads.getAllThreads()) {
            names.add(thread.getName());
        }
        return names;
    }


}

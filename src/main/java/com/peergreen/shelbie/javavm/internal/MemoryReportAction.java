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

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryManagerMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.felix.gogo.commands.Action;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.HandlerDeclaration;
import org.apache.felix.service.command.CommandSession;
import org.fusesource.jansi.Ansi;

/**
 * Display memory usage.
 * @author Guillaume Sauthier
 */
@Component
@Command(name = "memory",
         scope = "javavm",
         description = "Display details of the JVM's memory usage.")
@HandlerDeclaration("<sh:command xmlns:sh='org.ow2.shelbie'/>")
public class MemoryReportAction implements Action {

    public static final String MEMORY_USAGE_TITLE = "%16s %16s %16s %16s %16s";
    public static final String MEMORY_USAGE_TEMPLATE = "%16s %16s %15d%% %16s %16s";

    public Object execute(final CommandSession session) throws Exception {

        Ansi buffer = Ansi.ansi();

        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

        buffer.render("%30s", "");
        buffer.render(MEMORY_USAGE_TITLE, "Used (kB)", "Committed (kB)", "U/C (%)", "Init (kB)", "Max (kB)");
        buffer.newline();

        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        buffer.render("%-30s", "Global Heap Usage");
        buffer.render(format(MEMORY_USAGE_TEMPLATE,
                kBytes(heapMemoryUsage.getUsed()),
                kBytes(heapMemoryUsage.getCommitted()),
                getUsedMemoryPercent(heapMemoryUsage),
                kBytes(heapMemoryUsage.getInit()),
                kBytes(heapMemoryUsage.getMax())));
        buffer.newline();

        MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();
        buffer.render("%-30s", "Global Non-Heap Usage");
        buffer.render(format(MEMORY_USAGE_TEMPLATE,
                kBytes(nonHeapMemoryUsage.getUsed()),
                kBytes(nonHeapMemoryUsage.getCommitted()),
                getUsedMemoryPercent(nonHeapMemoryUsage),
                kBytes(nonHeapMemoryUsage.getInit()),
                kBytes(nonHeapMemoryUsage.getMax())));
        buffer.newline();
        buffer.newline();

        for (MemoryManagerMXBean memoryManagerMXBean : ManagementFactory.getMemoryManagerMXBeans()) {
            buffer.render("Manager @|bold %s|@ (%s)", memoryManagerMXBean.getName(), memoryManagerMXBean.isValid());
            buffer.newline();
            long used = 0;
            long committed = 0;
            List<MemoryPoolMXBean> pools = findMemoryPoolMXBeans(Arrays.asList(memoryManagerMXBean.getMemoryPoolNames()));
            for (MemoryPoolMXBean pool : pools) {
                MemoryUsage usage = pool.getUsage();
                used += usage.getUsed();
                committed += usage.getCommitted();
                buffer.render("@|bold %30s|@", pool.getName(), kBytes(usage.getMax()));
                buffer.render(format(MEMORY_USAGE_TEMPLATE,
                        kBytes(usage.getUsed()),
                        kBytes(usage.getCommitted()),
                        getUsedMemoryPercent(usage),
                        kBytes(usage.getInit()),
                        kBytes(usage.getMax())));
                buffer.newline();
            }

            // Print manager's total
            buffer.render("%30s", "total:");
            buffer.render(format("%16s %16s",
                    kBytes(used),
                    kBytes(committed)));
            buffer.newline();

        }

        System.out.print(buffer.toString());
        return null;
    }

    private int getUsedMemoryPercent(MemoryUsage usage) {
        return (int) (((double) usage.getUsed() / (double) usage.getCommitted()) * 100);
    }

    private String kBytes(long bytes) {
        return format("%,d", (long) (bytes / 1024));
    }

    private List<MemoryPoolMXBean> findMemoryPoolMXBeans(List<String> poolNames) {
        List<MemoryPoolMXBean> selected = new ArrayList<MemoryPoolMXBean>();
        for (MemoryPoolMXBean bean : ManagementFactory.getMemoryPoolMXBeans()) {
            if (poolNames.contains(bean.getName())) {
                selected.add(bean);
            }
        }

        return selected;
    }

}
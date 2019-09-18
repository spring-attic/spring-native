/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.main;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.sun.tools.attach.VirtualMachine;

/**
 * @author Dave Syer
 *
 */
@SuppressWarnings("restriction")
public class VirtualMachineMetrics {

    static final String CONNECTOR_ADDRESS = "com.sun.management.jmxremote.localConnectorAddress";

    public static Map<String, Long> fetch(String pid) {
        if (pid == null) {
            return Collections.emptyMap();
        }
        try {
            VirtualMachine vm = VirtualMachine.attach(pid);
            vm.startLocalManagementAgent();
            String connectorAddress = vm.getAgentProperties()
                    .getProperty(CONNECTOR_ADDRESS);
            JMXServiceURL url = new JMXServiceURL(connectorAddress);
            JMXConnector connector = JMXConnectorFactory.connect(url);
            MBeanServerConnection connection = connector.getMBeanServerConnection();
            gc(connection);
            Map<String, Long> metrics = new HashMap<>(
                    new BufferPools(connection).getMetrics());
            metrics.putAll(new Threads(connection).getMetrics());
            metrics.putAll(new Classes(connection).getMetrics());
            vm.detach();
            return metrics;
        }
        catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private static void gc(MBeanServerConnection mBeanServer) {
        try {
            final ObjectName on = new ObjectName("java.lang:type=Memory");
            mBeanServer.getMBeanInfo(on);
            mBeanServer.invoke(on, "gc", new Object[0], new String[0]);
        }
        catch (Exception ignored) {
            System.err.println("Unable to gc");
        }
    }

    public static long total(Map<String, Long> metrics) {
        return BufferPools.total(metrics);
    }

    public static long heap(Map<String, Long> metrics) {
        return BufferPools.heap(metrics);
    }

}

class Threads {

    private final MBeanServerConnection mBeanServer;

    public Threads(MBeanServerConnection mBeanServer) {
        this.mBeanServer = mBeanServer;
    }

    public Map<String, Long> getMetrics() {
        final Map<String, Long> gauges = new HashMap<>();
        final String name = "Threads";
        try {
            final ObjectName on = new ObjectName("java.lang:type=Threading");
            mBeanServer.getMBeanInfo(on);
            Integer value = (Integer) mBeanServer.getAttribute(on, "ThreadCount");
            gauges.put(name(name), Long.valueOf(value) * 1024 * 1024);
        }
        catch (Exception ignored) {
            System.err.println("Unable to load thread pool MBeans: " + name);
        }
        return Collections.unmodifiableMap(gauges);
    }

    private static String name(String name) {
        return name.replace(" ", "-");
    }

}

class Classes {

    private final MBeanServerConnection mBeanServer;

    public Classes(MBeanServerConnection mBeanServer) {
        this.mBeanServer = mBeanServer;
    }

    public Map<String, Long> getMetrics() {
        final Map<String, Long> gauges = new HashMap<>();
        final String name = "Classes";
        try {
            final ObjectName on = new ObjectName("java.lang:type=ClassLoading");
            mBeanServer.getMBeanInfo(on);
            Integer value = (Integer) mBeanServer.getAttribute(on, "LoadedClassCount");
            gauges.put(name(name), Long.valueOf(value));
        }
        catch (Exception ignored) {
            System.err.println("Unable to load thread pool MBeans: " + name);
        }
        return Collections.unmodifiableMap(gauges);
    }

    private static String name(String name) {
        return name.replace(" ", "-");
    }

}

class BufferPools {

    private static final String[] ATTRIBUTES = { "HeapMemoryUsage",
            "NonHeapMemoryUsage" };

    private final MBeanServerConnection mBeanServer;

    public BufferPools(MBeanServerConnection mBeanServer) {
        this.mBeanServer = mBeanServer;
    }

    public static long total(Map<String, Long> metrics) {
        long total = 0;
        System.err.println(metrics);
        for (int i = 0; i < ATTRIBUTES.length; i++) {
            final String name = name(ATTRIBUTES[i]);
            total += metrics.containsKey(name) ? metrics.get(name) : 0;
        }
        total += metrics.getOrDefault("Threads", 0L);
        return total;
    }

    public static long heap(Map<String, Long> metrics) {
        long total = 0;
        for (int i = 0; i < ATTRIBUTES.length; i++) {
            final String name = name(ATTRIBUTES[i]);
            if (name.startsWith("Heap")) {
                total += metrics.containsKey(name) ? metrics.get(name) : 0;
            }
        }
        return total;
    }

    public Map<String, Long> getMetrics() {
        final Map<String, Long> gauges = new HashMap<>();
        for (int i = 0; i < ATTRIBUTES.length; i++) {
            try {
                final ObjectName on = new ObjectName("java.lang:type=Memory");
                final String name = ATTRIBUTES[i];
                mBeanServer.getMBeanInfo(on);
                CompositeData value = (CompositeData) mBeanServer.getAttribute(on, name);
                gauges.put(name(name), (Long) value.get("used"));
            }
            catch (Exception ignored) {
                System.err.println("Unable to load memory pool MBeans");
            }
        }
        return Collections.unmodifiableMap(gauges);
    }

    private static String name(String name) {
        return name.replace(" ", "-");
    }

}

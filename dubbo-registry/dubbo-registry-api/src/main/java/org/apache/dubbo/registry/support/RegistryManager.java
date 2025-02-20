/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dubbo.registry.support;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.registry.NotifyListener;
import org.apache.dubbo.registry.Registry;
import org.apache.dubbo.registry.client.ServiceDiscovery;
import org.apache.dubbo.registry.client.ServiceDiscoveryRegistry;
import org.apache.dubbo.rpc.model.ApplicationModel;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Application Level, used to collect Registries
 * 注册管理器
 */
public class RegistryManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegistryManager.class);

    private ApplicationModel applicationModel;

    /**
     * Registry Collection Map<RegistryAddress, Registry>
     */
    private final Map<String, Registry> registries = new ConcurrentHashMap<>();

    /**
     * The lock for the acquisition process of the registry
     */
    protected final ReentrantLock lock = new ReentrantLock();

    private final AtomicBoolean destroyed = new AtomicBoolean(false);

    public RegistryManager(ApplicationModel applicationModel) {
        this.applicationModel = applicationModel;
    }

    /**
     * Get all registries
     *
     * @return all registries
     */
    public Collection<Registry> getRegistries() {
        return Collections.unmodifiableCollection(new LinkedList<>(registries.values()));
    }

    public Registry getRegistry(String key) {
        return registries.get(key);
    }

    public void putRegistry(String key, Registry registry) {
        registries.put(key, registry);
    }

    public  List<ServiceDiscovery> getServiceDiscoveries() {
        return getRegistries()
            .stream()
            .filter(registry -> registry instanceof ServiceDiscoveryRegistry)
            .map(registry -> (ServiceDiscoveryRegistry) registry)
            .map(ServiceDiscoveryRegistry::getServiceDiscovery)
            .collect(Collectors.toList());
    }

    /**
     * Close all created registries
     */
    public void destroyAll() {
        if (!destroyed.compareAndSet(false, true)) {
            return;
        }

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Close all registries " + getRegistries());
        }
        // Lock up the registry shutdown process
        lock.lock();
        try {
            for (Registry registry : getRegistries()) {
                try {
                    registry.destroy();
                } catch (Throwable e) {
                    LOGGER.warn(e.getMessage(), e);
                }
            }
            registries.clear();
        } finally {
            // Release the lock
            lock.unlock();
        }
    }

    /**
     * Reset state of AbstractRegistryFactory
     */
    public void reset() {
        destroyed.set(false);
        registries.clear();
    }

    protected Registry getDefaultNopRegistryIfDestroyed() {
        if (destroyed.get()) {
            LOGGER.warn("All registry instances have been destroyed, failed to fetch any instance. " +
                "Usually, this means no need to try to do unnecessary redundant resource clearance, all registries has been taken care of.");
            return DEFAULT_NOP_REGISTRY;
        }
        return null;
    }

    protected Lock getRegistryLock() {
        return lock;
    }

    public void removeDestroyedRegistry(Registry toRm) {
        lock.lock();
        try {
            registries.entrySet().removeIf(entry -> entry.getValue().equals(toRm));
        } finally {
            lock.unlock();
        }
    }

    // for unit test
    public void clearRegistryNotDestroy() {
        registries.clear();
    }

    public static RegistryManager getInstance(ApplicationModel applicationModel) {
        return applicationModel.getBeanFactory().getBean(RegistryManager.class);
    }

    private static final Registry DEFAULT_NOP_REGISTRY = new Registry() {
        @Override
        public URL getUrl() {
            return null;
        }

        @Override
        public boolean isAvailable() {
            return false;
        }

        @Override
        public void destroy() {

        }

        @Override
        public void register(URL url) {

        }

        @Override
        public void unregister(URL url) {

        }

        @Override
        public void subscribe(URL url, NotifyListener listener) {

        }

        @Override
        public void unsubscribe(URL url, NotifyListener listener) {

        }

        @Override
        public List<URL> lookup(URL url) {
            return null;
        }
    };
}

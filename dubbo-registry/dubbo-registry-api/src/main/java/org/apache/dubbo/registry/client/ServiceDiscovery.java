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
package org.apache.dubbo.registry.client;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.lang.Prioritized;
import org.apache.dubbo.metadata.MetadataInfo;
import org.apache.dubbo.registry.RegistryService;
import org.apache.dubbo.registry.client.event.listener.ServiceInstancesChangedListener;

import java.util.List;
import java.util.Set;

import static org.apache.dubbo.common.constants.CommonConstants.REGISTRY_DELAY_NOTIFICATION_KEY;

/**
 * Defines the common operations of Service Discovery, extended and loaded by ServiceDiscoveryFactory
 * 为服务发现，定义一些常用的操作
 */
public interface ServiceDiscovery extends RegistryService, Prioritized {
    // 注册服务
    void register() throws RuntimeException;
    // 更新服务
    void update() throws RuntimeException;
    // 注销服务
    void unregister() throws RuntimeException;

    /**
     * Gets all service names
     *
     * @return non-null read-only {@link Set}
     */
    Set<String> getServices();
    // 根据服务名来获取所有的服务实例
    List<ServiceInstance> getInstances(String serviceName) throws NullPointerException;
    // 当实例发生变化是，增加服务实例
    default void addServiceInstancesChangedListener(ServiceInstancesChangedListener listener)
            throws NullPointerException, IllegalArgumentException {
    }

    /**
     * unsubscribe to instance change event.
     *
     * @param listener
     * @throws IllegalArgumentException
     */
    // 当ServiceInstancesChangedEvent发生时，删除服务实例
    default void removeServiceInstancesChangedListener(ServiceInstancesChangedListener listener)
            throws IllegalArgumentException {
    }
    // 创建监听器
    default ServiceInstancesChangedListener createListener(Set<String> serviceNames) {
        return new ServiceInstancesChangedListener(serviceNames, this);
    }

    ServiceInstance getLocalInstance();

    MetadataInfo getLocalMetadata();

    MetadataInfo getRemoteMetadata(String revision);

    MetadataInfo getRemoteMetadata(String revision, ServiceInstance instance);

    /**
     * Destroy the {@link ServiceDiscovery}
     *
     * @throws Exception If met with error
     */
    void destroy() throws Exception;

    boolean isDestroy();

    default URL getUrl() {
        return null;
    }

    default long getDelay() {
        return getUrl().getParameter(REGISTRY_DELAY_NOTIFICATION_KEY, 5000);
    }

    /**
     * A human-readable description of the implementation
     *
     * @return The description.
     */
    String toString();
}

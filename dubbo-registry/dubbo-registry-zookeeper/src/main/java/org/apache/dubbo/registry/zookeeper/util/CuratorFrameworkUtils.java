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
package org.apache.dubbo.registry.zookeeper.util;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.client.DefaultServiceInstance;
import org.apache.dubbo.registry.client.ServiceInstance;
import org.apache.dubbo.registry.zookeeper.ZookeeperInstance;
import org.apache.dubbo.rpc.model.ScopeModelUtil;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstanceBuilder;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.curator.x.discovery.ServiceInstance.builder;
import static org.apache.dubbo.registry.zookeeper.util.CuratorFrameworkParams.BASE_SLEEP_TIME;
import static org.apache.dubbo.registry.zookeeper.util.CuratorFrameworkParams.BLOCK_UNTIL_CONNECTED_UNIT;
import static org.apache.dubbo.registry.zookeeper.util.CuratorFrameworkParams.BLOCK_UNTIL_CONNECTED_WAIT;
import static org.apache.dubbo.registry.zookeeper.util.CuratorFrameworkParams.MAX_RETRIES;
import static org.apache.dubbo.registry.zookeeper.util.CuratorFrameworkParams.MAX_SLEEP;

/**
 * Curator Framework Utilities Class
 *
 * @since 2.7.5
 */
public abstract class CuratorFrameworkUtils {

    public static ServiceDiscovery<ZookeeperInstance> buildServiceDiscovery(CuratorFramework curatorFramework,
                                                                            String basePath) {
        return ServiceDiscoveryBuilder.builder(ZookeeperInstance.class)
            .client(curatorFramework)
            .basePath(basePath)
            .build();
    }

    /**
     * 传入一个url，返回一个zookeeper的客户端
     * @param connectionURL
     * @return
     * @throws Exception
     */
    public static CuratorFramework buildCuratorFramework(URL connectionURL) throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.builder()
            .connectString(connectionURL.getBackupAddress())
            .retryPolicy(buildRetryPolicy(connectionURL))
            .build();
        curatorFramework.start();
        curatorFramework.blockUntilConnected(BLOCK_UNTIL_CONNECTED_WAIT.getParameterValue(connectionURL),
            BLOCK_UNTIL_CONNECTED_UNIT.getParameterValue(connectionURL));

        if (!curatorFramework.getState().equals(CuratorFrameworkState.STARTED)) {
            throw new IllegalStateException("zookeeper client initialization failed");
        }

        if (!curatorFramework.getZookeeperClient().isConnected()) {
            throw new IllegalStateException("failed to connect to zookeeper server");
        }
        return curatorFramework;
    }

    /**
     * 生成一个zookeeper的重试策略
     * @param connectionURL
     * @return
     */
    public static RetryPolicy buildRetryPolicy(URL connectionURL) {
        int baseSleepTimeMs = BASE_SLEEP_TIME.getParameterValue(connectionURL);
        int maxRetries = MAX_RETRIES.getParameterValue(connectionURL);
        int getMaxSleepMs = MAX_SLEEP.getParameterValue(connectionURL);
        return new ExponentialBackoffRetry(baseSleepTimeMs, maxRetries, getMaxSleepMs);
    }


    public static List<ServiceInstance> build(URL registryUrl, Collection<org.apache.curator.x.discovery.ServiceInstance<ZookeeperInstance>> instances) {
        return instances.stream().map((i)->build(registryUrl, i)).collect(Collectors.toList());
    }

    public static ServiceInstance build(URL registryUrl, org.apache.curator.x.discovery.ServiceInstance<ZookeeperInstance> instance) {
        String name = instance.getName();
        String host = instance.getAddress();
        int port = instance.getPort();
        ZookeeperInstance zookeeperInstance = instance.getPayload();
        DefaultServiceInstance serviceInstance = new DefaultServiceInstance(name, host, port, ScopeModelUtil.getApplicationModel(registryUrl.getScopeModel()));
        serviceInstance.setMetadata(zookeeperInstance.getMetadata());
        return serviceInstance;
    }

    public static org.apache.curator.x.discovery.ServiceInstance<ZookeeperInstance> build(ServiceInstance serviceInstance) {
        ServiceInstanceBuilder builder;
        String serviceName = serviceInstance.getServiceName();
        String host = serviceInstance.getHost();
        int port = serviceInstance.getPort();
        Map<String, String> metadata = serviceInstance.getSortedMetadata();
        String id = generateId(host, port);
        ZookeeperInstance zookeeperInstance = new ZookeeperInstance(id, serviceName, metadata);
        try {
            builder = builder()
                .id(id)
                .name(serviceName)
                .address(host)
                .port(port)
                .payload(zookeeperInstance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return builder.build();
    }

    public static String generateId(String host, int port) {
        return host + ":" + port;
    }
}

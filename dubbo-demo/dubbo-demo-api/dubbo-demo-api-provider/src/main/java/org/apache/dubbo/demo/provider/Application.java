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
package org.apache.dubbo.demo.provider;

import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.MetadataReportConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.ServiceConfig;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;
import org.apache.dubbo.demo.DemoService;

import java.util.concurrent.CountDownLatch;

public class Application {
    public static void main(String[] args) throws Exception {
        if (isClassic(args)) {
            startWithExport();
        } else {
            startWithBootstrap();
        }
    }

    private static boolean isClassic(String[] args) {
        return args.length > 0 && "classic".equalsIgnoreCase(args[0]);
    }

    private static void startWithBootstrap() {
        ServiceConfig<DemoServiceImpl> service = new ServiceConfig<>();
        service.setInterface(DemoService.class);
        service.setRef(new DemoServiceImpl());

        DubboBootstrap bootstrap = DubboBootstrap.getInstance();
        bootstrap.application(new ApplicationConfig("dubbo-demo-api-provider"))
            .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
            .protocol(new ProtocolConfig(CommonConstants.DUBBO, -1))
            .service(service)
            .start()
            .await();
    }

    private static void startWithExport() throws InterruptedException {
        // ServiceConfig到底是什么东西？
        // Service是什么东西， 定义成一个服务， 每个服务可以包含多个接口
        // ServiceConfig顾名思义，针对这个dubbo服务的一些配置信息
        // 泛型里包含的这个DemoServiceImpl是什么东西，服务的接口必须有实现代码，DemoServiceImpl=服务接口的实现代码
        ServiceConfig<DemoServiceImpl> service = new ServiceConfig<>();
        // 设置你的服务暴露出去的接口
        service.setInterface(DemoService.class);
        // 进一步明确设置你的暴露出去的接口的实现代码
        service.setRef(new DemoServiceImpl());
        // application name，在服务框架里，定位都是你的服务名称
        service.setApplication(new ApplicationConfig("dubbo-demo-api-provider"));
        // 所有的rpc框架，必须要跟注册中心配合使用，服务启动之后必须向注册中心注册
        // 注册中心是知道每一个服务有几个实例，每个实例在哪台服务器上
        // 其他的服务，就必须找注册中心，询问我要调用的服务有几个实例，分别在什么机器上
        // 设置zookeeper作为注册中心的地址
        service.setRegistry(new RegistryConfig("zookeeper://127.0.0.1:2181"));
        // 元数据上报配置，dubbo服务实例启动之后，肯定会有自己的元数据，必须上报到zookeeper上去
        service.setMetadataReportConfig(new MetadataReportConfig("zookeeper://127.0.0.1:2181"));
        // 都配置完毕之后，走一个export方法，推断，就一定会有网络监听的程序必须会启动
        // 别人调用你，需要跟你建立网络连接，再进行网络通信，按照协议，把请求数据发送给你，执行rpc调用
        // 把自己作为一个服务实例注册到zk里面去
        service.export();

        System.out.println("dubbo service started");
        new CountDownLatch(1).await();
    }
}

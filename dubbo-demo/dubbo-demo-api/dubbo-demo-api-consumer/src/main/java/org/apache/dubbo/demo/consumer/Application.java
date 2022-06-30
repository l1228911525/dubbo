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
package org.apache.dubbo.demo.consumer;

import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.MetadataReportConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;
import org.apache.dubbo.demo.DemoService;
import org.apache.dubbo.rpc.service.GenericService;

import java.util.concurrent.ExecutionException;

public class Application {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        runWithRefer();
//        if (isClassic(args)) {
//            runWithRefer();
//        } else {
//            runWithBootstrap();
//        }
    }

    private static boolean isClassic(String[] args) {
        return args.length > 0 && "classic".equalsIgnoreCase(args[0]);
    }

    private static void runWithBootstrap() throws ExecutionException, InterruptedException {
        ReferenceConfig<DemoService> reference = new ReferenceConfig<>();
        reference.setInterface(DemoService.class);
        reference.setGeneric("true");

        DubboBootstrap bootstrap = DubboBootstrap.getInstance();
        bootstrap.application(new ApplicationConfig("dubbo-demo-api-consumer"))
            .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
            .protocol(new ProtocolConfig(CommonConstants.DUBBO, -1))
            .reference(reference)
            .start();

        DemoService demoService = bootstrap.getCache().get(reference);
        String message = demoService.sayHello("dubbo");
        System.out.println(message);

        // generic invoke
        GenericService genericService = (GenericService) demoService;
        Object genericInvokeResult = genericService.$invoke("sayHello", new String[]{String.class.getName()},
            new Object[]{"dubbo generic invoke"});
        System.out.println(genericInvokeResult);
    }

    private static void runWithRefer() throws ExecutionException, InterruptedException {
        // ReferenceConfig是什么东西？
        // Reference本身是什么东西，有一个provider服务实例的一个引用
        // ReferenceConfig本身是属于要调用的其他服务的引用的配置
        // 通过泛型传递了调用的服务实例对外暴露的接口
        ReferenceConfig<DemoService> reference = new ReferenceConfig<>();
        // application, consumer服务实例自己本身也是一个服务实例
        reference.setApplication(new ApplicationConfig("dubbo-demo-api-consumer"));
        // 注册中心
        reference.setRegistry(new RegistryConfig("zookeeper://127.0.0.1:2181"));
        // 元数据上报地址，也必须进行设置
        reference.setMetadataReportConfig(new MetadataReportConfig("zookeeper://127.0.0.1:2181"));
        // 正式设置一下你要调用的服务的接口
        reference.setInterface(DemoService.class);
        // 直接通过ReferenceConfig的get方法，拿到了一个DemoService接口类型的东西
        // 必然是基于接口生成的动态代理，实现了DemoService接口，你只要调用这个动态代理的接口
        // 底层必然会去想办法调用provider服务实例的接口
        DemoService service = reference.get();
        // 发起RPC调用
        String message = service.sayHello("dubbo");
        System.out.println(message);
    }
}

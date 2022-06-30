package org.apache.dubbo.demo.dubbospi;

import org.apache.dubbo.common.extension.ExtensionLoader;

public class DubboSPITest {

    public static void main(String[] args) {
        ExtensionLoader<Operation> loader = ExtensionLoader.getExtensionLoader(Operation.class);
        Operation division = loader.getExtension("plus");
        System.out.println("result: " + division.operate(1, 2));
    }

}

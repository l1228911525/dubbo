package org.apache.dubbo.demo.dubbospi;

import org.apache.dubbo.common.extension.SPI;

@SPI
public interface Operation {

    int operate(int num1, int num2);

}

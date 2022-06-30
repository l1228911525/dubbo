package org.apache.dubbo.demo.dubbospi.impl;

import org.apache.dubbo.demo.dubbospi.Operation;

public class PlusOperation implements Operation {
    @Override
    public int operate(int num1, int num2) {
        return num1 + num2;
    }
}

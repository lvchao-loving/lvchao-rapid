package com.lvchao.rapid;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * cas test
 * </p>
 *
 * @author lvchao
 * @since 2023/2/25 11:14
 */
public class Test_CAS {
    public static void main(String[] args) {
        AtomicInteger integer = new AtomicInteger(0);
        System.out.println(integer.addAndGet(4));
    }
}

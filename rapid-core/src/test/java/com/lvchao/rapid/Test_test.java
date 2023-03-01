package com.lvchao.rapid;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * <p>
 * 文件描述（必填！！！）
 * </p>
 *
 * @author lvchao
 * @since 2023/2/26 12:57
 */
@Slf4j
public class Test_test {

    @Test
    public void test01(){
        System.out.println("日志输出...");
        log.info("日志输出...");
        log.error("日志输出...");
    }

}

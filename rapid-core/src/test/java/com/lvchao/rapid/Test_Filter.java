package com.lvchao.rapid;

import com.lvchao.rapid.core.netty.processor.filter.error.DefaultErrorFilter;
import com.lvchao.rapid.core.netty.processor.filter.post.StatisticsPostFilter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * <p>
 * 测试 StatisticsPostFilter
 * </p>
 *
 * @author lvchao
 * @since 2023/2/27 18:10
 */
@Slf4j
public class Test_Filter {

    @Test
    public void test01(){
        StatisticsPostFilter statisticsPostFilter = new StatisticsPostFilter();
    }

    @Test
    public void test02(){
        StatisticsPostFilter statisticsPostFilter = new StatisticsPostFilter();
        DefaultErrorFilter defaultErrorFilter = new DefaultErrorFilter();
        log.info("test");
        log.info("test");
        log.info("test");
    }
}

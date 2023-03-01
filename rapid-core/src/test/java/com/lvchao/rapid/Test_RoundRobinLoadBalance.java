package com.lvchao.rapid;

import com.lvchao.rapid.common.config.ServiceInstance;
import com.lvchao.rapid.core.balance.LoadBalance;
import com.lvchao.rapid.core.balance.RoundRobinLoadBalance;
import com.lvchao.rapid.core.context.AttributeKey;
import com.lvchao.rapid.core.context.RapidContext;
import com.lvchao.rapid.core.context.RapidRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * 文件描述（必填！！！）
 * </p>
 *
 * @author lvchao
 * @since 2023/2/26 19:21
 */
@Slf4j
public class Test_RoundRobinLoadBalance {

    @Test
    public void test01(){
        Set<ServiceInstance> serviceInstanceSet = new HashSet<>();
        for (int i = 1; i <= 5; i++) {
            ServiceInstance serviceInstance = new ServiceInstance();
            serviceInstanceSet.add(serviceInstance);
            serviceInstance.setServiceInstanceId(i + "");
            serviceInstance.setUniqueId("version:" + i);
            serviceInstance.setVersion(i + "");
        }

      //  RapidRequest rapidRequest = new RapidRequest();


        RapidContext.Builder rapidContextBuilder = new RapidContext.Builder();
        RapidContext rapidContext = rapidContextBuilder.setProtocol("HTTP").build();
        rapidContext.putAttribute(AttributeKey.MATCH_INSTANCES,serviceInstanceSet);
        LoadBalance rrLoadBalance = new RoundRobinLoadBalance();
        System.out.println(rrLoadBalance.select(rapidContext));
        System.out.println(rrLoadBalance.select(rapidContext));
        System.out.println(rrLoadBalance.select(rapidContext));
        System.out.println(rrLoadBalance.select(rapidContext));
        System.out.println(rrLoadBalance.select(rapidContext));
        System.out.println(rrLoadBalance.select(rapidContext));
        System.out.println(rrLoadBalance.select(rapidContext));
        System.out.println(rrLoadBalance.select(rapidContext));
    }

    @Test
    public void test02(){
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder("/user/name/{id}", Charset.defaultCharset());
        System.out.println(queryStringDecoder.path());
    }

}

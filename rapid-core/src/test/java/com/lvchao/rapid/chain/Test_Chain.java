package com.lvchao.rapid.chain;

import org.junit.Test;

/**
 * <p>
 * 测试编写的请求链【最简单的链路数据结构】
 * </p>
 *
 * @author lvchao
 * @since 2023/2/27 20:30
 */
public class Test_Chain {

    @Test
    public void test01() throws Throwable{
        DefaultProcessorEntityChain<Entity> chain = new DefaultProcessorEntityChain<>();
        chain.addFirst(new LinkedProcessorEntity01());
        chain.addFirst(new LinkedProcessorEntity02());
        chain.addFirst(new LinkedProcessorEntity03());
        Entity entity = new Entity();
        entity.setAge(11);
        entity.setName("吕超");
        chain.entry(entity,"参数1","参数2");
    }

}

package com.lvchao.rapid.chain;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * <p>
 * 文件描述（必填！！！）
 * </p>
 *
 * @author lvchao
 * @since 2023/2/27 20:46
 */
@Slf4j
public class LinkedProcessorEntity02 extends AbstractLinkedProcessorEntity<Entity>{
    @Override
    public boolean check(Entity entity) throws Throwable {
        if (Objects.isNull(entity) || entity.getAge() == 10){
            return false;
        }
        return true;
    }

    @Override
    public void entry(Entity entity, Object... args) throws Throwable {
        log.info("entity:{},args:{}", JSON.toJSONString(entity),args);
        entity.setAge(entity.getAge() - 1);
        // 存在下一个节点
        if (Objects.nonNull(getNext())){
            fireNext(entity, args);
        }
    }
}


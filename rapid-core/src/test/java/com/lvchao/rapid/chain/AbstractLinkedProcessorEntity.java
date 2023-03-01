package com.lvchao.rapid.chain;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * <p>
 * 构成抽象链表数据结构
 * </p>
 *
 * @author lvchao
 * @since 2023/2/27 15:18
 */
@Slf4j
public abstract class AbstractLinkedProcessorEntity<T> implements ProcessorEntity<T>{

    /**
     * 初始化下一个节点
     */
    @Getter
    @Setter
    private AbstractLinkedProcessorEntity next;

    @Override
    public void fireNext(T t, Object... args) throws Throwable {
        if (Objects.nonNull(next)){
            // 执行当前请求链
            if (next.check(t)){
                next.entry(t, args);
            }
            // 执行当前节点的下一个节点
            else {
                next.fireNext(t,args);
            }
        }else {
            log.info("目前 next 元素为空");
        }
    }
}

package com.lvchao.rapid.disruptor.quicklystart;

import lombok.Data;

/**
 * <p>
 * 这就是DS的RingBuffer处理的数据模型
 * </p>
 *
 * @author lvchao
 * @since 2023/2/1 16:14
 */
@Data
public class OrderEvent {
    private String name;
}

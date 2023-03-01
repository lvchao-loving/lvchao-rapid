package com.lvchao.rapid.disruptor.multi;

import lombok.Data;

/**
 * <p>
 * Order
 * </p>
 *
 * @author lvchao
 * @since 2023/2/2 11:55
 */
@Data
public class Order {
    private String id;
    private String name;
    private double price;
}

package com.lvchao.rapid.common.util;

import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 一对
 * </p>
 *
 * @author lvchao
 * @since 2023/1/31 21:41
 */
@Setter
@Getter
public class Pair<T1, T2> {
	
    private T1 object1;
    private T2 object2;

    public Pair(T1 object1, T2 object2) {
        this.object1 = object1;
        this.object2 = object2;
    }
}

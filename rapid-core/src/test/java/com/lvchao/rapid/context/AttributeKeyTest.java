package com.lvchao.rapid.context;

import com.lvchao.rapid.core.context.AttributeKey;

/**
 * <p>
 * 文件描述（必填！！！）
 * </p>
 *
 * @author lvchao
 * @since 2023/2/23 17:23
 */
public abstract class AttributeKeyTest<T> {

    public abstract T cast(Object value);

    public static class AttributeKey1<T> extends AttributeKey<T>{

        private final Class<T> valueClass;

        public AttributeKey1(final Class<T> valueClass) {
            this.valueClass = valueClass;
        }

        @Override
        public T cast(Object value) {
            return valueClass.cast(value);
        }
    }
}

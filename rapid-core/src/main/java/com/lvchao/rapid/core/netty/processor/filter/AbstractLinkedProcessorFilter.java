package com.lvchao.rapid.core.netty.processor.filter;

import com.lvchao.rapid.core.context.Context;
import com.lvchao.rapid.core.helper.ResponseHelper;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 抽象的带有链表形式的过滤器
 * </p>
 *
 * @author lvchao
 * @since 2023/2/7 19:07
 */
public abstract class AbstractLinkedProcessorFilter<T> implements ProcessorFilter<Context> {

    /**
     * 可以理解为 下一个元素的指针
     */
    @Getter
    @Setter
    protected AbstractLinkedProcessorFilter<T> next = null;

    // 递归调用
    @Override
    public void fireNext(Context context, Object... args) throws Throwable {

        if (context.isTerminated()){
            return;
        }

        if (context.isWrittened()){
            ResponseHelper.writeResponse(context);
        }

        if(next != null) {
            if(!next.check(context)) {
                next.fireNext(context, args);
            } else {
                // 进入下一个节点的 next
                next.transformEntry(context, args);
            }
        } else {
            //	没有下一个节点了，已经到了链表的最后一个节点
            context.terminated();
            return;
        }
    }

    @Override
    public void transformEntry(Context ctx, Object... args) throws Throwable {
        //	子类调用：这里就是真正执行下一个节点(元素)的操作
        entry(ctx, args);
    }
}

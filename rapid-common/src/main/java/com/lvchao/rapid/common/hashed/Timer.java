package com.lvchao.rapid.common.hashed;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *
 * </p>
 *
 * @author lvchao
 * @since 2023/1/31 21:41
 */
public interface Timer {

    /**
     * Schedules the specified {@link com.lvchao.rapid.common.hashed.TimerTask} for one-time execution after
     * the specified delay.
     *
     * @return a handle which is associated with the specified task
     *
     * @throws IllegalStateException if this timer has been
     *                               {@linkplain #stop() stopped} already
     */
    Timeout newTimeout(TimerTask task, long delay, TimeUnit unit);

    /**
     * Releases all resources acquired by this {@link Timer} and cancels all
     * tasks which were scheduled but not executed yet.
     *
     * @return the handles associated with the tasks which were canceled by
     *         this method
     */
    Set<Timeout> stop();
}
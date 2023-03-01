package com.lvchao.rapid.common.concurrent.queue.flusher;

/**
 * <B>主类名称：</B>Flusher<BR>
 * <B>概要说明：</B>Flusher接口定义<BR>
 * @author JiFeng
 * @since 2021年12月7日 上午12:21:03
 */
public interface Flusher<E> {

	/**
	 * 添加元素方法
	 * @param event
	 */
	void add(E event);

	/**
	 * 添加多个元素
	 * @param event
	 */
	void add(@SuppressWarnings("unchecked") E... event);

	/**
	 * 尝试添加一个元素, 如果添加成功返回true 失败返回false
	 * @param event
	 * @return
	 */
	boolean tryAdd(E event);

	/**
	 * 尝试添加多个元素, 如果添加成功返回true 失败返回false
	 * @param event
	 * @return
	 */
	boolean tryAdd(@SuppressWarnings("unchecked")E... event);

	/**
	 * isShutdown
	 * @return
	 */
	boolean isShutdown();

	/**
	 * start
	 */
	void start();

	/**
	 * shutdown
	 */
	void shutdown();
	
	
}

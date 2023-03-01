package com.lvchao.rapid.common.concurrent.queue.flusher;

import com.alibaba.ttl.threadpool.TtlExecutors;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.*;

/**
 * <B>主类名称：</B>ParallelFlusher<BR>
 * <B>概要说明：</B>并行的Flusher多生产者多消费者工具类，基于disruptor<BR>
 * @author JiFeng
 * @since 2021年12月7日 上午1:42:55
 */
public class ParallelFlusher<E> implements Flusher<E> {

	private RingBuffer<Holder> ringBuffer;

	private EventListener<E> eventListener;

	private WorkerPool<Holder> workerPool;

	private ExecutorService executorService;

	private EventTranslatorOneArg<Holder, E> eventTranslator;

	/**
	 * 私有构造方法，保证对象是通过 建造者模式Builder创建的
	 * @param builder
	 */
	private ParallelFlusher(Builder<E> builder) {
		// 属性赋值
		ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(builder.threads, builder.threads, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>(1000 * 1000), new ThreadFactoryBuilder().setNameFormat("ParallelFlusher-" + builder.namePrefix + "-pool-%d").build(),
				new ThreadPoolExecutor.CallerRunsPolicy());
		this.executorService = TtlExecutors.getTtlExecutorService(threadPoolExecutor);

		this.eventListener = builder.listener;
		this.eventTranslator = new HolderEventTranslator();

		// 创建 RingBuffer
		this.ringBuffer = RingBuffer.create(builder.producerType,new HolderEventFactory(), builder.bufferSize,builder.waitStrategy);

		// 通过 RingBuffer 创建一个屏障
		SequenceBarrier sequenceBarrier = ringBuffer.newBarrier();

		// 创建消费者数组：HolderWorkHandler
		WorkHandler<Holder>[] workHandlers= new WorkHandler[builder.threads];
		for (int i = 0; i < workHandlers.length; i++) {
			workHandlers[i] = new HolderWorkHandler();
		}

		// 构建多消费者工作池
		WorkerPool<Holder> workerPool = new WorkerPool<>(ringBuffer, sequenceBarrier, new HolderExceptionHandler(), workHandlers);

		//	设置多个消费者的sequence序号 用于单独统计消费进度, 并且设置到 ringbuffer 中
		ringBuffer.addGatingSequences(workerPool.getWorkerSequences());

		this.workerPool = workerPool;
	}

	@Override
	public void add(E event) {
		if (ringBuffer == null){
			process(eventListener,new IllegalStateException());
			return;
		}
		try {
			ringBuffer.publishEvent(this.eventTranslator, event);
		} catch (Throwable t) {
			process(this.eventListener, new IllegalStateException("ParallelFlusher is closed"), event);
		}
	}

	@Override
	public void add(E... events) {
		if(ringBuffer == null) {
			process(this.eventListener, new IllegalStateException("ParallelFlusher is closed"), events);
			return;
		}
		try {
			ringBuffer.publishEvents(this.eventTranslator, events);
		} catch (NullPointerException e) {
			process(this.eventListener, new IllegalStateException("ParallelFlusher is closed"), events);
		}
	}

	@Override
	public boolean tryAdd(E event) {
		final RingBuffer<Holder> temp = ringBuffer;
		if(temp == null) {
			return false;
		}
		try {
			return ringBuffer.tryPublishEvent(this.eventTranslator, event);
		} catch (NullPointerException e) {
			return false;
		}
	}


	@Override
	public boolean tryAdd(E... events) {
		final RingBuffer<Holder> temp = ringBuffer;
		if(temp == null) {
			return false;
		}
		try {
			return ringBuffer.tryPublishEvents(this.eventTranslator, events);
		} catch (Throwable t) {
			return false;
		}
	}

	/**
	 * 向 EventListener 中添加异常事件处理
	 *
	 * @param listener
	 * @param t
	 * @param event
	 * @param <E>
	 */
	private <E> void process(EventListener<E> listener,Throwable t,E event){
		listener.onException(t,-1,event);
	}

	/**
	 * 向 EventListener 中添加异常事件处理
	 *
	 * @param listener
	 * @param t
	 * @param events
	 * @param <E>
	 */
	private <E> void process(EventListener<E> listener,Throwable t,E ...events){
		for (E event:events) {
			process(listener, t, event);
		}
	}

	/**
	 * 判断 ringBuffer 队列当中是否有数据
	 * @return
	 */
	@Override
	public boolean isShutdown() {
		return ringBuffer == null;
	}

	@Override
	public void start() {
		this.ringBuffer = workerPool.start(executorService);
	}

	@Override
	public void shutdown() {
		RingBuffer<Holder> temp = ringBuffer;
		ringBuffer = null;
		if(temp == null) {
			return;
		}
		if(workerPool != null) {
			workerPool.drainAndHalt();
		}
		if(executorService != null) {
			executorService.shutdown();
		}
	}

	/**
	 * 事件监听
	 * @param <E>
	 */
	public interface EventListener<E>{

		/**
		 * 事件处理
		 *
		 * @param event
		 * @throws Exception
		 */
		void onEvent(E event) throws Exception;

		/**
		 * 异常处理
		 * @param t
		 * @param sequence
		 * @param event
		 */
		void onException(Throwable t, long sequence,E event);
	}

	/**
	 * 建造者模型, 目的就是为了设置真实对象的属性，在创建真实对象的时候透传过去
	 * @param <E>
	 */
	public static class Builder<E>{

		private ProducerType producerType = ProducerType.MULTI;

		private int bufferSize = 16 * 1024;

		private int threads = 1;

		private String namePrefix = "";

		private WaitStrategy waitStrategy = new BlockingWaitStrategy();

		// 消费者监听
		private EventListener<E> listener;

		public Builder<E> setProducerType(ProducerType producerType) {
			Preconditions.checkNotNull(producerType);
			this.producerType = producerType;
			return this;
		}

		public Builder<E> setThreads(int threads) {
			Preconditions.checkArgument(threads > 0);
			this.threads = threads;
			return this;
		}

		public Builder<E> setBufferSize(int bufferSize) {
			Preconditions.checkArgument(Integer.bitCount(bufferSize) == 1);
			this.bufferSize = bufferSize;
			return this;
		}

		public Builder<E> setNamePrefix(String namePrefix) {
			Preconditions.checkNotNull(namePrefix);
			this.namePrefix = namePrefix;
			return this;
		}

		public Builder<E> setWaitStrategy(WaitStrategy waitStrategy) {
			Preconditions.checkNotNull(waitStrategy);
			this.waitStrategy = waitStrategy;
			return this;
		}

		public Builder<E> setEventListener(EventListener<E> listener) {
			Preconditions.checkNotNull(listener);
			this.listener = listener;
			return this;
		}

		public ParallelFlusher<E> build(){
			return new ParallelFlusher<>(this);
		} 
	}

	private class Holder{

		private E event;

		public void setValue(E event){
			this.event = event;
		}

		@Override
		public String toString() {
			return "Holder event=" + event;
		}
	}

	private class HolderWorkHandler implements WorkHandler<Holder>{

		@Override
		public void onEvent(Holder event) throws Exception {
			eventListener.onEvent(event.event);
			event.setValue(null);
		}

	}

	private class HolderEventFactory implements EventFactory<Holder>{

		@Override
		public Holder newInstance() {
			return new Holder();
		}
	}

	private class HolderExceptionHandler implements ExceptionHandler<Holder>{

		@Override
		public void handleEventException(Throwable ex, long sequence, Holder event) {
			Holder holder = (Holder)event;
			try {
				eventListener.onException(ex, sequence, holder.event);
			} catch (Exception e) {
				//	ignore..
			} finally {
				holder.setValue(null);
			}
		}

		@Override
		public void handleOnStartException(Throwable ex) {
			throw new UnsupportedOperationException(ex);
		}

		@Override
		public void handleOnShutdownException(Throwable ex) {
			throw new UnsupportedOperationException(ex);
		}
	}

	/**
	 * 设置序号、设置对象、发布事件统一一个接口完成 EventTranslatorOneArg
	 */
	private class HolderEventTranslator implements EventTranslatorOneArg<Holder,E>{

		@Override
		public void translateTo(Holder holder, long sequence, E event) {
			holder.setValue(event);
		}
	}
}

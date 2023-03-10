package com.lvchao.rapid.core.context;

import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * <p>
 * 基础上下文实现类
 * </p>
 *
 * @author lvchao
 * @since 2023/2/5 20:58
 */
public abstract class BasicContext implements Context{

    /**
     * 协议
     */
    protected final String protocol;

    /**
     * netty 请求上下文
     */
    protected final ChannelHandlerContext nettyCtx;

    /**
     * 是否保持连接
     */
    protected final boolean keepAlive;

    /**
     * 上下文的status标识
     */
    protected volatile int status = Context.RUNNING;

    /**
     * 保存所有的上下文参数集合
     */
    protected final Map<AttributeKey<?>, Object> attributes = new HashMap<AttributeKey<?>, Object>();

    /**
     * 在请求过程中出现异常则设置异常对象
     */
    protected Throwable throwable;

    /**
     * 定义是否已经释放请求资源
     */
    protected final AtomicBoolean requestReleased = new AtomicBoolean(false);

    /**
     * 存放回调函数的集合
     */
    protected List<Consumer<Context>> completedCallbacks;

    /**
     * 	SR(Server[Rapid-Core] Received):	服务器接收到网络请求
     * 	SS(Server[Rapid-Core] Send):		服务器写回请求
     * 	RS(Route Send):						客户端发送请求
     * 	RR(Route Received): 				客户端收到请求
     */
    protected long SRTime;

    protected long SSTime;

    protected long RSTime;

    protected long RRTime;

    /**
     * 构造函数初始化对象
     *
     * @param protocol
     * @param nettyCtx
     * @param keepAlive
     */
    public BasicContext(String protocol, ChannelHandlerContext nettyCtx, boolean keepAlive) {
        this.protocol = protocol;
        this.nettyCtx = nettyCtx;
        this.keepAlive = keepAlive;
    }

    @Override
    public String getProtocol() {
        return this.protocol;
    }

    @Override
    public ChannelHandlerContext getNettyCtx() {
        return this.nettyCtx;
    }

    @Override
    public boolean isKeepAlive() {
        return this.keepAlive;
    }

    @Override
    public void runned() {
        status = Context.RUNNING;
    }

    @Override
    public void writtened(){
        status = Context.WRITTEN;
    }

    @Override
    public void completed(){
        status = Context.COMPLETED;
    }

    @Override
    public void terminated(){
        status = Context.TERMINATED;
    }

    @Override
    public boolean isRunning(){
        return status == Context.RUNNING;
    }

    @Override
    public boolean isWrittened(){
        return status == Context.WRITTEN;
    }

    @Override
    public boolean isCompleted(){
        return status == Context.COMPLETED;
    }

    @Override
    public boolean isTerminated(){
        return status == Context.TERMINATED;
    }

    @Override
    public <T> T getAttribute(AttributeKey<T> key) {
        return (T) attributes.get(key);
    }

    @Override
    public <T> T putAttribute(AttributeKey<T> key, T value) {
        return (T) attributes.put(key, value);
    }

    @Override
    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
    public Throwable getThrowable() {
        return this.throwable;
    }

    @Override
    public void releaseRequest() {
        this.requestReleased.compareAndSet(false, true);
    }

    @Override
    public void completedCallback(Consumer<Context> consumer) {
        if(completedCallbacks == null) {
            completedCallbacks = new ArrayList<>();
        }
        completedCallbacks.add(consumer);
    }

    @Override
    public void invokeCompletedCallback() {
        if(completedCallbacks != null) {
            completedCallbacks.forEach(call -> call.accept(this));
        }
    }

    @Override
    public long getSRTime() {
        return SRTime;
    }

    @Override
    public void setSRTime(long SRTime) {
        this.SRTime = SRTime;
    }

    @Override
    public long getSSTime() {
        return SSTime;
    }

    @Override
    public void setSSTime(long SSTime) {
        this.SSTime = SSTime;
    }

    @Override
    public long getRSTime() {
        return RSTime;
    }

    @Override
    public void setRSTime(long RSTime) {
        this.RSTime = RSTime;
    }

    @Override
    public long getRRTime() {
        return this.RRTime;
    }

    @Override
    public void setRRTime(long RRTime) {
        this.RRTime = RRTime;
    }
}

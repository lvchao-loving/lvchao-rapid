package com.lvchao.rapid.core.netty.processor;

import com.lvchao.rapid.common.enums.ResponseCode;
import com.lvchao.rapid.common.exception.RapidNotFoundException;
import com.lvchao.rapid.common.exception.RapidPathNoMatchedException;
import com.lvchao.rapid.common.exception.RapidResponseException;
import com.lvchao.rapid.core.context.HttpRequestWrapper;
import com.lvchao.rapid.core.context.RapidContext;
import com.lvchao.rapid.core.helper.RequestHelper;
import com.lvchao.rapid.core.helper.ResponseHelper;
import com.lvchao.rapid.core.netty.processor.filter.DefaultProcessorFilterFactory;
import com.lvchao.rapid.core.netty.processor.filter.ProcessorFilterFactory;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 核心流程的主执行逻辑
 * </p>
 *
 * @author lvchao
 * @since 2023/1/31 21:41
 */

@Slf4j
public class NettyCoreProcessor implements NettyProcessor{

    private ProcessorFilterFactory processorFilterFactory = DefaultProcessorFilterFactory.getInstance();

    @Override
    public void process(HttpRequestWrapper wrapper) {
        try {
            //	1.解析FullHttpRequest, 把他转换为我们自己想要的内部对象：Context
            RapidContext rapidContext = RequestHelper.doContext(wrapper);

            //	2. 执行整个的过滤器逻辑：FilterChain
            processorFilterFactory.doFilterChain(rapidContext);

        } catch (RapidPathNoMatchedException e) {
            log.error("#NettyCoreProcessor# process 网关资指定路径为匹配异常，快速失败： code: {}, msg: {}",
                    e.getCode().getCode(), e.getCode().getMessage(), e);
            FullHttpResponse response = ResponseHelper.getHttpResponseJSONServerError(e.getCode());
            //	释放资源写回响应
            doWriteAndRelease(wrapper, response);
        }
        catch(RapidNotFoundException e) {
            log.error("#NettyCoreProcessor# process 网关资源未找到异常： code: {}, msg: {}",
                    e.getCode().getCode(), e.getCode().getMessage(), e);
            FullHttpResponse response = ResponseHelper.getHttpResponseJSONServerError(e.getCode());
            //	释放资源写回响应
            doWriteAndRelease(wrapper, response);

        } catch(RapidResponseException e) {
            log.error("#NettyCoreProcessor# process 网关内部未知错误异常： code: {}, msg: {}",
                    e.getCode().getCode(), e.getCode().getMessage(), e);
            FullHttpResponse response = ResponseHelper.getHttpResponseJSONServerError(e.getCode());
            //	释放资源写回响应
            doWriteAndRelease(wrapper, response);

        } catch (Throwable t) {
            log.error("#NettyCoreProcessor# process 网关内部未知错误异常", t);
            FullHttpResponse response = ResponseHelper.getHttpResponseJSONServerError(ResponseCode.INTERNAL_ERROR);
            //	释放资源写回响应
            doWriteAndRelease(wrapper, response);
        }
    }

    /**
     * 写回响应信息并释放资源
     * @param wrapper
     * @param response
     */
    private void doWriteAndRelease(HttpRequestWrapper wrapper, FullHttpResponse response) {
        wrapper.getCtx().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        boolean release = ReferenceCountUtil.release(wrapper.getFullHttpRequest());
        if(!release) {
            log.warn("#NettyCoreProcessor# doWriteAndRelease release fail 释放资源失败， request:{}, release:{}",
                    wrapper.getFullHttpRequest().uri(),
                    release);
        }
    }

}

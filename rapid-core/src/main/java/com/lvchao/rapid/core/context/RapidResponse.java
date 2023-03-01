package com.lvchao.rapid.core.context;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lvchao.rapid.common.enums.ResponseCode;
import com.lvchao.rapid.common.util.JSONUtil;
import io.netty.handler.codec.http.*;
import lombok.Data;
import org.asynchttpclient.Response;

/**
 * <p>
 * 网关响应封装类
 * </p>
 *
 * @author lvchao
 * @since 2023/2/5 10:59
 */
@Data
public class RapidResponse {

    /**
     * 返回响应头
     */
    private HttpHeaders responseHeaders = new DefaultHttpHeaders();

    /**
     * 额外的响应头结果
     */
    private final HttpHeaders extraResponseHeaders = new DefaultHttpHeaders();

    /**
     * 返回响应内容
     */
    private String content;

    /**
     * 返回响应状态码
     */
    private HttpResponseStatus httpResponseStatus;

    /**
     * 相应对象
     */
    private Response futureResponse;

    /**
     * 私有构造方法
     */
    private RapidResponse() {
    }


    /**
     * 通过 asynchttpclient的response 对象，构建 自定义的 response 对象
     *
     * @param futureResponse
     * @return
     */
    public static RapidResponse buildRapidResponse(Response futureResponse) {
        RapidResponse rapidResponse = new RapidResponse();
        rapidResponse.setFutureResponse(futureResponse);
        rapidResponse.setHttpResponseStatus(HttpResponseStatus.valueOf(futureResponse.getStatusCode()));
        return rapidResponse;
    }

    /**
     * 返回一个json类型的响应信息，失败时候使用
     *
     * @param responseCode
     * @return
     */
    public static RapidResponse buildRapidResponse(ResponseCode responseCode) {
        ObjectNode objectNode = JSONUtil.createObjectNode();
        objectNode.put(JSONUtil.STATUS, responseCode.getStatus().code());
        objectNode.put(JSONUtil.CODE, responseCode.getCode());
        objectNode.put(JSONUtil.MESSAGE, responseCode.getMessage());

        RapidResponse rapidResponse = new RapidResponse();
        rapidResponse.setHttpResponseStatus(responseCode.getStatus());
        rapidResponse.putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON + ";charset=utf-8");
        rapidResponse.setContent(JSONUtil.toJSONString(objectNode));

        return rapidResponse;
    }

    /**
     * 返回一个json类型的响应信息，成功时使用
     *
     * @return
     */
    public static RapidResponse buildRapidResponseObj(Object data) {
        ObjectNode objectNode = JSONUtil.createObjectNode();
        objectNode.put(JSONUtil.STATUS, ResponseCode.SUCCESS.getStatus().code());
        objectNode.put(JSONUtil.CODE, ResponseCode.SUCCESS.getCode());
        objectNode.putPOJO(JSONUtil.DATA, data);

        RapidResponse rapidResponse = new RapidResponse();
        rapidResponse.setHttpResponseStatus(ResponseCode.SUCCESS.getStatus());
        rapidResponse.putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON + ";charset=utf-8");
        rapidResponse.setContent(JSONUtil.toJSONString(objectNode));
        return rapidResponse;
    }

    /**
     * 设置响应头信息
     *
     * @param key
     * @param value
     */
    public void putHeader(CharSequence key, CharSequence value) {
        responseHeaders.add(key, value);
    }
}

package com.lvchao.rapid.common.metric;

/**
 * <p>
 *
 * </p>
 *
 * @author lvchao
 * @since 2023/1/31 21:41
 */
public enum MetricCodeEnum {
	
    UNKNOWN_ERROR(-1, "系统错误"),
    
    SUCCESS(200, "成功");
	
    private Integer code;
    
    private String msg;

    MetricCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
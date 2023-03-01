package com.lvchao.rapid.common.config;

import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * <p>
 * 服务实例：一个服务定义会对应多个服务实例
 * </p>
 *
 * @author lvchao
 * @since 2023/1/31 21:41
 */
@Data
public class ServiceInstance implements Serializable {

	/**
	 * 	服务实例ID: ip:port
	 */
	protected String serviceInstanceId;
	
	/**
	 * 	服务定义唯一id： uniqueId
	 */
	protected String uniqueId;
	
	/**
	 * 	服务实例地址： ip:port
	 */
	protected String address;
	
	/**
	 * 	标签信息
	 */
	protected String tags;
	
	/**
	 * 	权重信息
	 */
	protected Integer weight;
	
	/**
	 * 	服务注册的时间戳：后面我们做负载均衡，warmup预热
	 */
	protected long registerTime;
	
	/**
	 * 	服务实例启用禁用
	 */
	protected boolean enable = true;
	
	/**
	 * 	服务实例对应的版本号
	 */
	protected String version;

	public ServiceInstance() {
		super();
	}

	public ServiceInstance(String serviceInstanceId, String uniqueId, String address, String tags, Integer weight,
			long registerTime, boolean enable, String version) {
		super();
		this.serviceInstanceId = serviceInstanceId;
		this.uniqueId = uniqueId;
		this.address = address;
		this.tags = tags;
		this.weight = weight;
		this.registerTime = registerTime;
		this.enable = enable;
		this.version = version;
	}

	@Override
	public boolean equals(Object o) {
		if(this == o) {
			return true;
		}
		if(this == null || getClass() != o.getClass()) {
			return false;
		}
		ServiceInstance serviceInstance = (ServiceInstance)o;
		return Objects.equals(serviceInstanceId, serviceInstance.serviceInstanceId);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(serviceInstanceId);
	}
	
}

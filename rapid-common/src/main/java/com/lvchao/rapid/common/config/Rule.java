package com.lvchao.rapid.common.config;

import com.lvchao.rapid.common.util.CollectionUtils;
import lombok.Data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * <p>
 * 规则模型
 * </p>
 *
 * @author lvchao
 * @since 2023/2/5 20:31
 */
public class Rule implements Comparable<Rule>, Serializable {

	/**
	 * 规则ID 全局唯一
	 */
	private String id;

	/**
	 * 规则名称
	 */
	private String name;

	/**
	 * 规则对应的协议
	 */
	private String protocol;

	/**
	 * 规则排序，用于以后万一有需求做一个路径绑定多种规则，但是只能最终执行一个规则（按照该属性做优先级判断）
	 */
	private Integer order;

	/**
	 * 规则集合定义
	 */
	private Set<FilterConfig> filterConfigs = new HashSet<>();
	
	public Rule() {}

	public Rule(String id, String name, String protocol, Integer order, Set<FilterConfig> filterConfigs) {
		this.id = id;
		this.name = name;
		this.protocol = protocol;
		this.order = order;
		this.filterConfigs = filterConfigs;
	}

	/**
	 * 向规则里面添加指定的过滤器
	 * @param filterConfig
	 * @return
	 */
	public boolean addFilterConfig(FilterConfig filterConfig) {
		return filterConfigs.add(filterConfig);
	}

	/**
	 * 通过一个指定的filterId 获取getFilterConfig
	 * @param id
	 * @return
	 */
	public FilterConfig getFilterConfig(String id){
		if (CollectionUtils.isEmpty(filterConfigs)){
			return null;
		}
		for(FilterConfig filterConfig : filterConfigs) {
			if(filterConfig.getId().equalsIgnoreCase(id)) {
				return filterConfig;
			}
		}
		return null;
	}

	public Set<FilterConfig> getFilterConfigs() {
		return filterConfigs;
	}

	public void setFilterConfigs(Set<FilterConfig> filterConfigs) {
		this.filterConfigs = filterConfigs;
	}
	/**
	 * 根据传入的filterId 判断当前Rule中是否存在
	 * @param id
	 * @return
	 */
	public boolean hashId(String id) {
		for(FilterConfig filterConfig : filterConfigs) {
			if(filterConfig.getId().equalsIgnoreCase(id)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int compareTo(Rule o) {
		int orderCompare = Integer.compare(getOrder(), o.getOrder());
		if(orderCompare == 0) {
			return getId().compareTo(o.getId());
		}
		return orderCompare;
	}
	
	@Override
	public boolean equals(Object o) {
		if(this == o) {
			return true;
		}
		if((o == null) || getClass() != o.getClass()) {
			return false;
		}
		Rule that = (Rule)o;
		return id.equals(that.id);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

	/**
	 * 过滤器的配置类
	 */
	@Data
	public static class FilterConfig {

		/**
		 * 过滤器的唯一ID
		 */
		private String id;

		/**
		 * 过滤器的配置信息描述：json string  {timeout: 500}  {balance: rr}
		 */
		private String config;

		@Override
		public boolean equals(Object o) {
			if(this == o) {
				return true;
			}
			if((o == null) || getClass() != o.getClass()) {
				return false;
			}
			FilterConfig that = (FilterConfig)o;
			return id.equals(that.id);
		}

		@Override
		public int hashCode() {
			return Objects.hash(id);
		}
	}

	public static void main(String[] args) {
		Rule rule = new Rule();
	}
}

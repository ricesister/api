package com.sen.api.configs;

import java.util.Map;

/**
 * 
 * @description
 * @author fs
 * @2018年8月22日
 *
 */
public class ConfigBean {
	
	private String description;
	private String rootUrl;
	private String project_name;
	
	private Map<String,String> headers;
	private Map<String,String> params;
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getRootUrl() {
		return rootUrl;
	}
	public void setRootUrl(String rootUrl) {
		this.rootUrl = rootUrl;
	}
	public String getProject_name() {
		return project_name;
	}
	public void setProject_name(String project_name) {
		this.project_name = project_name;
	}
	public Map<String, String> getHeaders() {
		return headers;
	}
	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}
	public Map<String, String> getParams() {
		return params;
	}
	public void setParams(Map<String, String> params) {
		this.params = params;
	}
	
	

}

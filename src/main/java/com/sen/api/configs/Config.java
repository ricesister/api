package com.sen.api.configs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * 
 * @description 读取配置，针对多环境多配置
 * @author fs
 * @2018年8月22日
 *
 */
public class Config {
	/**
	 * api-config.xml配置参数数据，届时以描述可以去筛选想要的配置信息
	 */
	private List<ConfigBean> configs= new ArrayList<>();

	@SuppressWarnings("unchecked")
	public Config(String configFilePath) throws DocumentException {
		super();
		SAXReader reader = new SAXReader();
		Document document = reader.read(configFilePath);
		Element rootElement = document.getRootElement();
		List<Element> environments = rootElement.element("environments").
				elements("environment");
		for(Element environment:environments) {
			ConfigBean config = new ConfigBean();
			config.setDescription(environment.element("description").getTextTrim());
			//获取headers
			getHeaders(environment, config);
			getParams(environment, config);
			config.setProject_name(environment.element("project_name").getTextTrim());
			config.setRootUrl(environment.element("rootUrl").getTextTrim());
			configs.add(config);
		}
	}
	
	
	/**
	 * 获取公共头信息
	 * @param environment
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public void getHeaders(Element environment,ConfigBean config){
		Map<String,String> headers = new HashMap<String,String>();
		List<Element> headersElements = environment.element("headers")
				.elements("header");
		for(Element header:headersElements) {
			headers.put(header.attributeValue("name").trim(), 
					header.attributeValue("value")).trim();
		}
		config.setHeaders(headers);
	}
	
	
	/**
	 * 获取公共参数
	 * @param environment
	 * @param config
	 */
	@SuppressWarnings("unchecked")
	public void getParams(Element environment,ConfigBean config){
		Map<String,String> params = new HashMap<String,String>();
		List<Element> paramsElements = environment.element("params")
				.elements("param");
		for(Element param:paramsElements) {
			params.put(param.attributeValue("name").trim(), 
					param.attributeValue("value").trim());
		}
		config.setHeaders(params);
	}


	public List<ConfigBean> getConfigs() {
		return configs;
	}
	
	
	public static void main(String[] args) throws DocumentException {
		List<ConfigBean> list = new Config("api-config.xml").getConfigs();
		System.out.println(list.get(0).getDescription());
		System.out.println(list.get(0).getHeaders());
	}
	
	
	
	
	
	
	

}

package test.com.sen.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.dom4j.DocumentException;
import org.testng.Assert;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONPath;
import com.sen.api.beans.ApiDataBean;
import com.sen.api.beans.BaseBean;
import com.sen.api.configs.Config;
import com.sen.api.utils.AssertUtil;
import com.sen.api.utils.ExcelUtil;
import com.sen.api.utils.FileUtil;
import com.sen.api.utils.FunctionUtil;
import com.sen.api.utils.RandomUtil;
import com.sen.api.utils.ReportUtil;
import com.sen.api.utils.SSLClient;
import com.sen.api.utils.StringUtil;

public class TestBase {

	/**
	 * 公共参数数据池（全局可用）
	 */
	private static Map<String, String> saveDatas = new HashMap<String, String>();

	/**
	 * 替换符，如果数据中包含“${}”则会被替换成公共参数中存储的数据
	 */
	protected Pattern replaceParamPattern = Pattern.compile("\\$\\{(.*?)\\}");

	/**
	 * 截取自定义方法正则表达式：__xxx(ooo)
	 */
	protected Pattern funPattern = Pattern
			.compile("__(\\w*?)\\((([\\w\\\\\\/:\\.\\$]*,?)*)\\)");// __(\\w*?)\\((((\\w*)|(\\w*,))*)\\)
																	// __(\\w*?)\\(((\\w*,?\\w*)*)\\)

	
	
	/**
	 * 存入公共参数池
	 * @param map
	 */
	protected void setSaveDates(Map<String, String> map) {
		if(map == null)
			return;
		saveDatas.putAll(map);
		
	}

	/**
	 * 组件预参数（处理__fucn()以及${xxxx}）
	 * 
	 * @param apiDataBean
	 * @return
	 */
	protected String buildParam(String param) {
		// 处理${}
		param = getCommonParam(param);
		// Pattern pattern = Pattern.compile("__(.*?)\\(.*\\)");// 取__开头的函数正则表达式
		// Pattern pattern =
		// Pattern.compile("__(\\w*?)\\((\\w*,)*(\\w*)*\\)");// 取__开头的函数正则表达式
		Matcher m = funPattern.matcher(param);
		while (m.find()) {
			String funcName = m.group(1);
			String args = m.group(2);
			String value;
			// bodyfile属于特殊情况，不进行匹配，在post请求的时候进行处理
			if (FunctionUtil.isFunction(funcName)
					&& !funcName.equals("bodyfile")) {
				// 属于函数助手，调用那个函数助手获取。
				value = FunctionUtil.getValue(funcName, args.split(","));
				// 解析对应的函数失败
				Assert.assertNotNull(value,
						String.format("解析函数失败：%s。", funcName));
				param = StringUtil.replaceFirst(param, m.group(), value);
			}
		}
		return param;
	}

	
	/**
	 * 讲xx=xx;xx=xx加入公共参数池saveDatas
	 * @param preParam
	 */
	protected void savePreParam(String preParam) {
		// 通过';'分隔，将参数加入公共参数map中
		if (StringUtil.isEmpty(preParam)) {
			return;
		}
		String[] preParamArr = preParam.split(";");
		String key, value;
		for (String prepar : preParamArr) {
			if (StringUtil.isEmpty(prepar)) {
				continue;
			}
			key = prepar.split("=")[0];
			value = prepar.split("=")[1];
			ReportUtil.log(String.format("存储%s参数，值为：%s。", key, value));
			saveDatas.put(key, value);
		}
	}

	/**
	 * 取公共参数的值  并替换参数
	 *
	 * 
	 * @param param
	 * @return
	 */
	protected String getCommonParam(String param) {
		if (StringUtil.isEmpty(param)) {
			return "";
		}
		Matcher m = replaceParamPattern.matcher(param);// 取公共参数正则
		while (m.find()) {
			String replaceKey = m.group(1);
			String value;
			// 从公共参数池中获取值
			value = getSaveData(replaceKey);
			// 如果公共参数池中未能找到对应的值，该用例失败。
			Assert.assertNotNull(value,
					String.format("格式化参数失败，公共参数中找不到%s。", replaceKey));
			param = param.replace(m.group(), value);
		}
		return param;
	}

	/**
	 * 根据key获取公共数据池中的数据
	 * 
	 * @param key
	 *            公共数据的key
	 * @return 对应的value
	 */
	protected String getSaveData(String key) {
		if ("".equals(key) || !saveDatas.containsKey(key)) {
			return null;
		} else {
			return saveDatas.get(key);
		}
	}

	
	/**
	 * 断言结果$.ddd=xx
	 * @param sourchData
	 * @param verifyStr
	 * @param contains
	 */
	protected void verifyResult(String sourchData, String verifyStr,
			boolean contains) {
		if (StringUtil.isEmpty(verifyStr)) {
			return;
		}
		String allVerify = getCommonParam(verifyStr);
		ReportUtil.log("验证数据：" + allVerify);
		if (contains) {
			// 验证结果包含
			AssertUtil.contains(sourchData, allVerify);
		} else {
			// 通过';'分隔，通过jsonPath进行一一校验
			Pattern pattern = Pattern.compile("([^;]*)=([^;]*)");
			Matcher m = pattern.matcher(allVerify.trim());
			while (m.find()) {
				String actualValue = getBuildValue(sourchData, m.group(1));
				String exceptValue = getBuildValue(sourchData, m.group(2));
				ReportUtil.log(String.format("验证转换后的值%s=%s", actualValue,
						exceptValue));
				Assert.assertEquals(actualValue, exceptValue, "验证预期结果失败。");
			}
		}
	}

	/**
	 * 获取格式化后的值
	 * @param sourchJson
	 * @param key
	 * @return
	 */
	private String getBuildValue(String sourchJson, String key) {
		key = key.trim();
		Matcher funMatch = funPattern.matcher(key);
		if (key.startsWith("$.")) {// jsonpath
			key = JSONPath.read(sourchJson, key).toString();
		} else if (funMatch.find()) {
			// String args;
			// if (funMatch.group(2).startsWith("$.")) {
			// args = JSONPath.read(sourchJson, funMatch.group(2)).toString();
			// } else {
			// args = funMatch.group(2);
			// }
			String args = funMatch.group(2);
			String[] argArr = args.split(",");
			for (int index = 0; index < argArr.length; index++) {
				String arg = argArr[index];
				if (arg.startsWith("$.")) {
					argArr[index] = JSONPath.read(sourchJson, arg).toString();
				}
			}
			String value = FunctionUtil.getValue(funMatch.group(1), argArr);
			key = StringUtil.replaceFirst(key, funMatch.group(), value);

		}
		return key;
	}

	/**
	 * 提取json串中的值保存至公共池中
	 * 
	 * @param json
	 *            将被提取的json串。
	 * @param allSave
	 *            所有将被保存的数据：xx=$.jsonpath.xx;oo=$.jsonpath.oo，将$.jsonpath.
	 *            xx提取出来的值存放至公共池的xx中，将$.jsonpath.oo提取出来的值存放至公共池的oo中
	 */
	protected void saveResult(String json, String allSave) {
		if (null == json || "".equals(json) || null == allSave
				|| "".equals(allSave)) {
			return;
		}
		allSave = getCommonParam(allSave);
		String[] saves = allSave.split(";");
		String key, value;
		for (String save : saves) {
			// key = save.split("=")[0].trim();
			// value = JsonPath.read(json,
			// save.split("=")[1].trim()).toString();
			// ReportUtil.log(String.format("存储公共参数   %s值为：%s.", key, value));
			// saveDatas.put(key, value);

			Pattern pattern = Pattern.compile("([^;=]*)=([^;]*)");
			Matcher m = pattern.matcher(save.trim());
			while (m.find()) {
				key = getBuildValue(json, m.group(1));
				value = getBuildValue(json, m.group(2));
				if(value.contains("oncetoken")) {
					value = value.split("oncetoken=")[1];
				}

				ReportUtil.log(String.format("存储公共参数   %s值为：%s.", key, value));
				saveDatas.put(key, value);
			}
		}
	}

	/**
	 * 根据配置读取测试用例
	 * 
	 * @param clz
	 *            需要转换的类
	 * @param excelPaths
	 *            所有excel的路径配置
	 * @param excelName
	 *            本次需要过滤的excel文件名
	 * @param sheetName
	 *            本次需要过滤的sheet名
	 * @return 返回数据
	 * @throws DocumentException
	 */
	protected <T extends BaseBean> List<T> readExcelData(Class<T> clz,
			String[] excelPathArr, String[] sheetNameArr)
			throws DocumentException {
		List<T> allExcelData = new ArrayList<T>();// excel文件數組
		List<T> temArrayList = new ArrayList<T>();
		for (String excelPath : excelPathArr) {
			File file = Paths.get(System.getProperty("user.dir"),
					excelPath).toFile();
			temArrayList.clear();
			if (sheetNameArr.length == 0 || sheetNameArr[0] == "") {
				temArrayList = ExcelUtil.readExcel(clz, file.getAbsolutePath());
			} else {
				for (String sheetName : sheetNameArr) {
					temArrayList.addAll(ExcelUtil.readExcel(clz,
							file.getAbsolutePath(), sheetName));
				}
			}
			temArrayList.forEach((bean) -> {
				bean.setExcelName(file.getName());
			});
			allExcelData.addAll(temArrayList); // 将excel数据添加至list
		}
		return allExcelData;
	}
	
	/**
	 * 确认睡眠时间
	 * @param apiDataBean
	 * @throws InterruptedException
	 */
	protected void checkSleep(ApiDataBean apiDataBean) throws InterruptedException {
		if(apiDataBean.getSleep() > 0) {
			// sleep休眠时间大于0的情况下进行暂停休眠
			ReportUtil.log(String.format("sleep %s seconds",
					apiDataBean.getSleep()));
			Thread.sleep(apiDataBean.getSleep() * 1000);
		}
	} 
	
	
	
	/**
	 * 拼接Url
	 * @param shortUrl
	 * @return
	 */
	protected String parseUrl(String rootUrl,String shortUrl,boolean rooUrlEndWithSlash) {
		// 替换url中的参数
		shortUrl = getCommonParam(shortUrl);
		if (shortUrl.startsWith("http")) {
			return shortUrl;
		}
		if (rooUrlEndWithSlash == shortUrl.startsWith("/")) {
			if (rooUrlEndWithSlash) {
				shortUrl = shortUrl.replaceFirst("/", "");
			} else {
				shortUrl = "/" + shortUrl;
			}
		}
		return rootUrl + shortUrl;
	}
	
	
	/**
	 * 读取配置
	 * @param pathName
	 * @return
	 * @throws DocumentException 
	 */
	protected Config getConfig(String pathName) throws DocumentException {
		String configFilePath = Paths.get(System.getProperty("user.dir"), pathName).toString();
		ReportUtil.log("api config path:" + configFilePath);
		System.out.println("api config path:" +configFilePath);
		Config config = new Config(configFilePath);
		return config;
	}
	
	/**
	 * 获取连接客户端
	 * @param client
	 * @return
	 * @throws Exception
	 */
	protected HttpClient getClient(HttpClient client) throws Exception {
		client = new SSLClient();
		client.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 60000); // 请求超时
		client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 60000); // 读取超时
		return client;
	}
	
	
	protected Iterator<Object[]> getRunList(List<ApiDataBean> dataList){
		List<Object[]> dataProvider = new ArrayList<Object[]>();
		for (ApiDataBean data : dataList) {
			if (data.isRun()) {
				dataProvider.add(new Object[] { data });
			}
		}
		return dataProvider.iterator();
	}
	
	/**
	 * 保存预存参数 用于后面接口参数中使用和接口返回验证中;处理接口请求参数
	 * @param apiDataBean
	 * @return
	 */
	protected String buildRequestParam(ApiDataBean apiDataBean) {
		// 分析处理预参数 （函数生成的参数）
		String preParam = buildParam(apiDataBean.getPreParam());
		savePreParam(preParam);// 保存预存参数 用于后面接口参数中使用和接口返回验证中
		// 处理参数
		String apiParam = buildParam(apiDataBean.getParam());
		return apiParam;

	
	}
	
	
	/**
	 * 封装请求方法
	 *
	 * @param url
	 *            请求路径
	 
	 * @throws UnsupportedEncodingException
	 */
	protected HttpUriRequest parseHttpRequest(String url, String method, String param
			,String rootUrl,boolean rooUrlEndWithSlash
			,Header[] publicHeaders,boolean requestByFormData ) throws UnsupportedEncodingException {
		// 处理url
		url = parseUrl(rootUrl,url,rooUrlEndWithSlash);
		ReportUtil.log("请求方式:" + method);
		ReportUtil.log("请求路径:" + url);
		ReportUtil.log("请求参数:" + param.replace("\r\n", "").replace("\n", ""));
		//upload表示上传，也是使用post进行请求
		if ("post".equalsIgnoreCase(method) || "upload".equalsIgnoreCase(method)) {
			// 封装post方法
			HttpPost postMethod = new HttpPost(url);
			postMethod.setHeaders(publicHeaders);
			//如果请求头的content-type的值包含form-data 或者 请求方法为upload(上传)时采用MultipartEntity形式
			HttpEntity entity  = parseEntity(param,requestByFormData || "upload".equalsIgnoreCase(method));
			postMethod.setEntity(entity);
			return postMethod;
		} else if ("put".equalsIgnoreCase(method)) {
			// 封装put方法
			HttpPut putMethod = new HttpPut(url);
			putMethod.setHeaders(publicHeaders);
			HttpEntity entity  = parseEntity(param,requestByFormData );
			putMethod.setEntity(entity);
			return putMethod;
		} else if ("delete".equalsIgnoreCase(method)) {
			// 封装delete方法
			HttpDelete deleteMethod = new HttpDelete(url);
			deleteMethod.setHeaders(publicHeaders);
			return deleteMethod;
		} else {
			// 封装get方法
			HttpGet getMethod = new HttpGet(url);
			getMethod.setHeaders(publicHeaders);
			return getMethod;
		}
	}
	
	
	/**
	 * 格式化参数，如果是form-data格式则将参数封装到MultipartEntity否则封装到StringEntity
	 * @param param 参数
	 * @param formData 是否使用form-data格式
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@SuppressWarnings("unchecked")
	protected HttpEntity parseEntity(String param,boolean formData) throws UnsupportedEncodingException{
		if(formData){
			Map<String, String> paramMap = JSON.parseObject(param,
					HashMap.class);
			MultipartEntity multiEntity = new MultipartEntity();
			for (String key : paramMap.keySet()) {
				String value = paramMap.get(key);
				Matcher m = funPattern.matcher(value);
				if (m.matches() && m.group(1).equals("bodyfile")) {
					value = m.group(2);
					multiEntity.addPart(key, new FileBody(new File(value)));
				} else {
					multiEntity.addPart(key, new StringBody(paramMap.get(key)));
				}
			}
			return multiEntity;
		}else{
			return new StringEntity(param, "UTF-8");
		}
	}
	
	/**
	 * 判断返回代码
	 * @param response
	 */
	protected void checkStatus(HttpResponse response,ApiDataBean apiDataBean) {
		int responseStatus = response.getStatusLine().getStatusCode();
		ReportUtil.log("返回状态码："+responseStatus);
		if (apiDataBean.getStatus()!= 0) {
			Assert.assertEquals(responseStatus, apiDataBean.getStatus(),
					"返回状态码与预期不符合!");
		} 
	}
	
	
	protected String getResponseData(HttpResponse response) throws IllegalStateException, IOException {
		String responseData;
		HttpEntity respEntity = response.getEntity();
		Header respContentType = response.getFirstHeader("Content-Type");
		if (respContentType != null && respContentType.getValue() != null 
				&&  (respContentType.getValue().contains("download") || respContentType.getValue().contains("octet-stream"))) {
			String conDisposition = response.getFirstHeader(
					"Content-disposition").getValue();
			String fileType = conDisposition.substring(
					conDisposition.lastIndexOf("."),
					conDisposition.length());
			String filePath = "download/" + RandomUtil.getRandom(8, false)
					+ fileType;
			InputStream is = response.getEntity().getContent();
			Assert.assertTrue(FileUtil.writeFile(is, filePath), "下载文件失败。");
			// 将下载文件的路径放到{"filePath":"xxxxx"}进行返回
			responseData = "{\"filePath\":\"" + filePath + "\"}";
			
		} else {
			responseData=EntityUtils.toString(respEntity, "UTF-8");
		}
		// 输出返回数据log
		ReportUtil.log("返回结果:" + responseData);
		return responseData;
	}
	
	/**
	 * 处理断言及保存save
	 * @param responseData
	 * @param apiDataBean
	 */
	protected void verify(String responseData,ApiDataBean apiDataBean) {
		// 验证预期信息
		verifyResult(responseData, apiDataBean.getVerify(),
				apiDataBean.isContains());
		// 对返回结果进行提取保存。
		saveResult(responseData, apiDataBean.getSave());
	}
}

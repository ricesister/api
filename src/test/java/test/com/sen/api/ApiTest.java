package test.com.sen.api;

import com.alibaba.fastjson.JSON;
import com.sen.api.beans.ApiDataBean;
import com.sen.api.configs.ApiConfig;
import com.sen.api.configs.Config;
import com.sen.api.excepions.ErrorRespStatusException;
import com.sen.api.listeners.AutoTestListener;
import com.sen.api.listeners.RetryListener;
import com.sen.api.utils.*;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.dom4j.DocumentException;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.*;
import org.testng.annotations.Optional;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

@Listeners({ AutoTestListener.class, RetryListener.class })
public class ApiTest extends TestBase {
	/**
	 * 跟路径是否以‘/’结尾
	 */
	private static boolean rooUrlEndWithSlash = false;
	private static CookieStore cookieStore;
	/**
	 * api请求跟路径
	 */
	private static String rootUrl;
	/**
	 * 所有公共header，会在发送请求的时候添加到http header上
	 */
	private static Header[] publicHeaders;
	/**
	 * 是否使用form-data传参 会在post与put方法封装请求参数用到
	 */
	private static boolean requestByFormData = false;
	/**
	 * 配置
	 */
	private static Config config;
	/**
	 * 所有api测试用例数据
	 */
	protected List<ApiDataBean> dataList = new ArrayList<ApiDataBean>();

	
	@Test(dataProvider = "cpsDatas")
	public void apiTest(ApiDataBean apiDataBean) throws Exception {
		ClientConnectionManager connManager = new PoolingClientConnectionManager();
		/**
		 * 新增新的client
		 */
	    DefaultHttpClient client = new DefaultHttpClient(connManager);
		ReportUtil.log("--- API test start ---");
		checkSleep(apiDataBean);
		String apiParam = buildRequestParam(apiDataBean);
		// 封装请求方法
		HttpUriRequest method = parseHttpRequest(apiDataBean.getUrl(),
				apiDataBean.getMethod(), apiParam,rootUrl,rooUrlEndWithSlash
				,publicHeaders,requestByFormData);
		String responseData;
		try {
			client.setCookieStore(cookieStore);
		      // 执行
		    HttpResponse response = client.execute(method);
		      //获取响应头
		    cookieStore= client.getCookieStore();
			checkStatus(response, apiDataBean);
			responseData = getResponseData(response);
		} catch (Exception e) {
			throw e;
		} finally {
			method.abort();
		}
		verify(responseData, apiDataBean);
	}

	/**
	 * 初始化测试数据
	 *
	 * @throws Exception
	 */
	@Parameters("envName")
	@BeforeSuite
	public void init(@Optional("api-config.xml") String envName) throws Exception {
		config = getConfig(envName);
		/**
		 * 获取团购环境下的配置信息
		 */
		rootUrl = config.getConfigs().get(0).getRootUrl();
		rooUrlEndWithSlash = rootUrl.endsWith("/");

		// 读取 param，并将值保存到公共数据map
		Map<String, String> params = config.getConfigs().get(0).getParams();
		setSaveDates(params);
		/**
		 * 封装httpheader公共头
		 */
		List<Header> headers = new ArrayList<Header>();
		config.getConfigs().get(1).getHeaders().forEach((key, value) -> {
			Header header = new BasicHeader(key, value);
			if(!requestByFormData && key.equalsIgnoreCase("content-type") && value.toLowerCase().contains("form-data")){
				requestByFormData=true;
			}
			headers.add(header);
		});
		publicHeaders = headers.toArray(new Header[headers.size()]);
		
	}

	@Parameters({ "excelPath", "sheetName" })
	@BeforeTest
	public void readData(@Optional("case/api-data.xls") String excelPath, @Optional("Sheet1") String sheetName) throws DocumentException {
		dataList = readExcelData(ApiDataBean.class, excelPath.split(";"),
				sheetName.split(";"));
	}

	//TODO 实体excel变化，对于apidata这种咋写
	/**
	 * 过滤数据，run标记为Y的执行。
	 *
	 * @return
	 * @throws DocumentException
	 */
	@DataProvider(name = "cpsDatas")
	public Iterator<Object[]> getApiData(ITestContext context)
			throws DocumentException {
		return getRunList(dataList);
	}

}

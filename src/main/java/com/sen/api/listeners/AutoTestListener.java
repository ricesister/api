package com.sen.api.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;


public class AutoTestListener extends TestListenerAdapter {
	
	/**
     * 输出正文字段
     */
    public static int failCount;
    public static int successCount;
    public static int count;
    public static int skipCount;

	@Override
	public void onTestSuccess(ITestResult tr) {
		TestngRetry.resetRetryCount();
		super.onTestSuccess(tr);
	}

	public void onTestFailure(ITestResult tr) {
		saveResult(tr);
		super.onTestFailure(tr);
	}

	public void onTestSkipped(ITestResult tr) {
		saveResult(tr);
		super.onTestSkipped(tr);
	}

	private void saveResult(ITestResult tr) {
		Throwable throwable = tr.getThrowable();
		if (null == throwable) {
			return;
		}
		// String imgPath = WebdriverUtil.captureEntirePageScreenshot();
		// log.error("用例执行错误截图：" + imgPath);
		// Reporter.setCurrentTestResult(tr);
		// Reporter.log("path path path path");
	}

	@Override
	public void onFinish(ITestContext testContext) {
		super.onFinish(testContext);

		// List of test results which we will delete later
		ArrayList<ITestResult> testsToBeRemoved = new ArrayList<ITestResult>();
		// collect all id's from passed test
		Set<Integer> passedTestIds = new HashSet<Integer>();
		for (ITestResult passedTest : testContext.getPassedTests()
				.getAllResults()) {
			// logger.info("PassedTests = " + passedTest.getName());
			passedTestIds.add(getId(passedTest));
		}

		Set<Integer> failedTestIds = new HashSet<Integer>();
		for (ITestResult failedTest : testContext.getFailedTests()
				.getAllResults()) {
			// logger.info("failedTest = " + failedTest.getName());
			// id = class + method + dataprovider
			int failedTestId = getId(failedTest);

			// if we saw this test as a failed test before we mark as to be
			// deleted
			// or delete this failed test if there is at least one passed
			// version
			if (failedTestIds.contains(failedTestId)
					|| passedTestIds.contains(failedTestId)) {
				testsToBeRemoved.add(failedTest);
			} else {
				failedTestIds.add(failedTestId);
			}
		}

		// finally delete all tests that are marked
		for (Iterator<ITestResult> iterator =

		testContext.getFailedTests().getAllResults().iterator(); iterator
				.hasNext();) {
			ITestResult testResult = iterator.next();

			if (testsToBeRemoved.contains(testResult)) {
				// logger.info("Remove repeat Fail Test: " +
				// testResult.getName());
				iterator.remove();
			}
		}
		
		/**
		 * 邮件处理
		 *//*
		AutoTestListener.count = testContext.getAllTestMethods().length;
		System.out.println("onfinsh总数----------"+AutoTestListener.count);
		AutoTestListener.failCount = testContext.getFailedTests().getAllResults().size();
		System.out.println("onfinsh失败----------"+AutoTestListener.failCount);
		AutoTestListener.successCount = testContext.getPassedTests().getAllResults().size();
		System.out.println("onfinsh成功---------"+AutoTestListener.successCount);
		AutoTestListener.skipCount = testContext.getSkippedTests().getAllResults().size();
		System.out.println("onfinsh略过----------"+AutoTestListener.skipCount);
		
		String content= "<p>本次测试执行结果：成功<font color=\"#3F9F00\">"+successCount +"</font> 失败：<font color=\"#FF252D\">"+
		failCount +"</font>略过： <font color=\"#0078D7\">"
    			+skipCount +"</font> 通过率：<font color=\"#000000\">"+successCount/count*100+" %</font></p>";
		try {
			MailUtil.sendMail(content);
		} catch (AddressException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (javax.mail.MessagingException e) {
			e.printStackTrace();
		}*/
	}

	private int getId(ITestResult result) {
		int id = result.getTestClass().getName().hashCode();
		id = id + result.getMethod().getMethodName().hashCode();
		id = id
				+ (result.getParameters() != null ? Arrays.hashCode(result
						.getParameters()) : 0);
		return id;
	}
}

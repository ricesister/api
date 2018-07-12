package cbt.atm.core;

import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

public class SeleniumDriver {

	//��ģ��ʹ��ͳһ��driver
	private static WebDriver driver = null;
	public static boolean isInitiated;
	
	
	public SeleniumDriver(String browserType){
		if(!isInitiated){
			if(browserType.equals("ie")){
				System.setProperty("webdriver.ie.driver", 
						".\\Tools\\IEDriverServer.exe");
				DesiredCapabilities.internetExplorer().setCapability("ignoreProtectedModeSettings",
						true);
				driver = new InternetExplorerDriver(DesiredCapabilities.internetExplorer());
			}else if(browserType.equals("chrome")){
				System.setProperty("webdriver.chrome.driver", 
						".\\Tools\\chromedriver.exe");
				driver = new ChromeDriver();
			}else{
				System.setProperty("webdriver.firefox.bin", 
						"C:\\Program Files\\Mozilla Firefox\\firefox.exe");
				System.setProperty("webdriver.gecko.driver", 
						".\\Tools\\geckodriver.exe");
				driver = new FirefoxDriver();
			}
		}
		isInitiated = true;
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
		driver.manage().timeouts().setScriptTimeout(10, TimeUnit.SECONDS);
		driver.manage().window().maximize();
	}
	
	//����
	public WebDriver getWebDriver(){
		return driver;
	}
	
	//����Ԫ�صȴ�����
	public void waitForElementPresent(By by){
		for(int i=1;i<=10;i++){
			try {
				Thread.sleep(1000);
				driver.findElement(by);
				break;
			} catch (InterruptedException e) {
				System.out.println("����Ѱ��Ԫ�أ���"+i+"�顣����");
				e.printStackTrace();
			}
			
		}
	}
	
	//�ж�Ԫ���Ƿ����
	public boolean isElementPresent(By by){
		try {
			driver.findElement(by);
			return true;
		} catch (NoSuchElementException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	//������������
	public void deleteCookies(){
		if(driver == null) return;
		driver.manage().deleteAllCookies();
	}
	
	
	//�ر������
	public void closeBrowser(){
		try {
			driver.quit();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	
}

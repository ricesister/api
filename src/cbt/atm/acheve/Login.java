package cbt.atm.acheve;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import cbt.atm.core.Common;
import cbt.atm.core.SeleniumDriver;

public class Login {
	
	private Common common = new Common();
	
	private SeleniumDriver seld = new SeleniumDriver("firefox");
	private WebDriver driver = seld.getWebDriver();
	
	//����ǰ׼��
	private void prepare(){
		driver.navigate().to("https:\\www.baidu.com");
	}
	
	//������β
	private void finish(){
		seld.closeBrowser();  //�����в��ԣ��˴����ر�
	}
	
	//action���ʵ�ֵ�¼
	private void doLogin(String username,String pwd){
		driver.findElement(By.linkText("��¼")).click();
		driver.findElement(By.xpath("//*[@id=\"TANGRAM__PSP_10__userName\"]")).sendKeys(username);
		driver.findElement(By.id("TANGRAM__PSP_10__password")).sendKeys(pwd);
		driver.findElement(By.id("TANGRAM__PSP_10__submit")).click();
		common.sleepRandom(3, 5);
	}
	
	//test������Ե�¼ʧ�����
	public void testLoginFail(){
		this.doLogin("admin", "123");
		
	}

}

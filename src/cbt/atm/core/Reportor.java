package cbt.atm.core;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

public class Reportor {
	
	public static String version="1.0.0";
	public static String module="oms";
	public static String folder = "20180708";
	
	//ͳһ���Խ�����
	public static final String PASS="�ɹ�";
	public static final String FAIL="ʧ��";
	public static final String ERROR="����";
	public static final String IGNORE="����";
	
	//����ֶθ���ʵ�ʶ���������ݿ�
	public void writeLog(String sql,String caseid,String result,String error,String screenshot){
		String runtime = Common.getFormatter("yyyy-MM-dd HH:mm:ss");
		
		try {
			Connection conn = DBUtil.getConn();
			
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, version);
			//����ʵ�����
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	static{
		
	}
	
	public static void createFolder(){
		String folder = Common.getFormatter("yyyyMMdd");
		
		String userDir = System.getProperty("user.dir");
		File file = new File(userDir+"\\report"+folder);
		if(!file.exists()){
			file.mkdir();
			System.out.println(folder+"��ͼ�����ļ��д����ɹ���");
		}else{
			System.out.println(folder+"��ͼ�����ļ����Ѿ����ڣ�");
		}
		
		Reportor.folder = folder;
	}
	
	//�����쳣ʱ�ֳ���ͼ
	public String captureScreen(){
		String image = Common.getFormatter("HHmmss")+".png";
		String userDir = System.getProperty("user.dir");
		String path = userDir+"\\report\\"+folder+"\\"+image;
		
		//����java��ͼ
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		int width = (int) dim.getWidth();
		int height = (int) dim.getHeight();
		File file = new File(path);
		
		try {
			Robot robot = new Robot();
			BufferedImage screen = robot.createScreenCapture(new Rectangle(0, 0, width, height));
			ImageIO.write(screen, "png", file);
		} catch (AWTException e) {
			System.err.println(e.getMessage());
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		
		return image;
	}
	
	

}

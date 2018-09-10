package com.sen.api.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;

/**
 * 
 * @author fs
 * @version 1.0.0
 * @description  邮件发送工具
 * @date 2018年7月31日 下午2:33:50
 */
public class MailUtil {
	private static String host;  
    private static String username;  
    private static String password;  
    private static String from;  
    private static String nick; 
    private static String start;
    private static String port;
    private static String sendToMails;
    private static String send_file;
  
    static {  
    	Properties prop = new Properties();
    	 // 通过输入缓冲流进行读取配置文件
    	// 加载输入流
    	// 根据关键字获取value值
    	String file = "mailConfig.properties";
        try {  
        	InputStream InputStream = MailUtil.class.getClassLoader().getResourceAsStream(file);
        	prop.load(InputStream);
            // Test Data  
            host = prop.getProperty("host");;  
            username = prop.getProperty("username"); 
            password = prop.getProperty("password");  
            from = prop.getProperty("mailFrom"); 
            nick = "ZDHinfo";  
            start = prop.getProperty("start");
            port = prop.getProperty("mailPort");
            sendToMails =  prop.getProperty("toMails");
            send_file = prop.getProperty("files");
            // nick + from 组成邮箱的发件人信息  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }  
    
    
    
  
    /** 
     * 发送邮件 
     *  
     * @param to 
     *            收件人列表，以","分割 
     * @param subject 
     *            标题 
     * @param body 
     *            内容 
     * @param filepath 
     *            附件列表,无附件传递null 
     * @return 
     * @throws MessagingException 
     * @throws AddressException 
     * @throws UnsupportedEncodingException 
     */  
    public static boolean sendMail(String to, String subject, String body,  
            List<String> filepath) throws AddressException, MessagingException,  
            UnsupportedEncodingException {  
        // 参数修饰  
        if (body == null) {  
            body = "";  
        }  
        if (subject == null) {  
            subject = "无主题";  
        }  
        // 创建Properties对象  
        Properties props = System.getProperties();  
        // 创建信件服务器  
        props.put("mail.smtp.host", host);  
        props.put("mail.smtp.auth", "true"); // 通过验证  
        props.put("mail.smtp.port", port);
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.ssl.enable", "true");
       //props.put("mail.debug", "true");
        // 得到默认的对话对象  
        Session session = Session.getDefaultInstance(props, null);  
        // 创建一个消息，并初始化该消息的各项元素  
        MimeMessage msg = new MimeMessage(session);  
        nick = MimeUtility.encodeText(nick);  
        msg.setFrom(new InternetAddress(nick + "<" + from + ">"));  
        // 创建收件人列表  
        if (to != null && to.trim().length() > 0) {  
            String[] arr = to.split(",");  
            int receiverCount = arr.length;  
            if (receiverCount > 0) {  
                InternetAddress[] address = new InternetAddress[receiverCount];  
                for (int i = 0; i < receiverCount; i++) {  
                    address[i] = new InternetAddress(arr[i]);  
                }  
                msg.addRecipients(Message.RecipientType.TO, address);  
                msg.setSubject(subject);  
                // 后面的BodyPart将加入到此处创建的Multipart中  
                Multipart mp = new MimeMultipart("related");  
                // 附件操作  
                if (filepath != null && filepath.size() > 0) {  
                    for (String filename : filepath) {  
                        MimeBodyPart mbp = new MimeBodyPart();  
                        // 得到数据源  
                        FileDataSource fds = new FileDataSource(filename);  
                        mbp.setContent(body, "text/html; charset=utf-8");
                        // 得到附件本身并至入BodyPart  
                        mbp.setDataHandler(new DataHandler(fds));  
                        // 得到文件名同样至入BodyPart  
                        mbp.setFileName(fds.getName());  
                        mp.addBodyPart(mbp);  
                    }  
                    MimeBodyPart mbp = new MimeBodyPart();  
                    mbp.setText(body);  
                    mbp.setContent(body, "text/html; charset=utf-8");
                    mp.addBodyPart(mbp);  
                    // 移走集合中的所有元素  
                    filepath.clear();  
                    // Multipart加入到信件  
                    msg.setContent(mp);  
                } else {  
                    // 设置邮件正文  
                    msg.setText(body);  
                }  
                // 设置信件头的发送日期  
                msg.setSentDate(new Date());  
                msg.saveChanges();  
                // 发送信件  
                Transport transport = session.getTransport("smtp");  
                transport.connect(username, password);
                transport.sendMessage(msg,  
                        msg.getRecipients(Message.RecipientType.TO));  
                transport.close();  
                return true;  
            } else {  
                System.out.println("None receiver!");  
                return false;  
            }  
        } else {  
            System.out.println("None receiver!");  
            return false;  
        }  
    }  
  
   /* public static void main(String[] args) throws AddressException,  
    	UnsupportedEncodingException, MessagingException {  
		if(!"true".equals(start) && "false".equals(start)) {
			System.out.println("-------------------------邮件服务未开启-----------------------------------------------");
			return;
		}
		List<String> sendPath = new ArrayList<String>();
		String[] sendPaths = getSendFiles();
		if(sendPaths.length == 1) {
			sendPath.add(System.getProperty("user.dir")+sendPaths[0]);
		}else {
			for(int i=0;i<sendPaths.length;i++) {
				sendPath.add(System.getProperty("user.dir")+sendPaths[i]);
			}
		}
		sendMail(sendToMails, DateUtil.getDate()+"cps东经接口测试报告", "API自动化测试报告，请下载后查看！", sendPath);
		System.out.println("-------------------------"+DateUtil.getDate()+"sendMail success!\n发送邮件列表："+sendToMails.toString());
	}*/


	public static String[] getSendFiles() {
		String[] sendFiles = send_file.split(",");
		return sendFiles;
	}
	
	 public static void sendMail(String content) throws 
	    AddressException, UnsupportedEncodingException, MessagingException {
	    	System.out.println("接收到正文内容："+content);
	    	if(!"true".equals(start) && "false".equals(start)) {
	    		System.out.println("-------------------------邮件服务未开启-----------------------------------------------");
	    		return;
	    	}
	    	List<String> sendPath = new ArrayList<String>();
	    	String[] sendPaths = getSendFiles();
	    	if(sendPaths.length == 1) {
	    		sendPath.add(System.getProperty("user.dir")+sendPaths[0]);
	    	}else {
	    		for(int i=0;i<sendPaths.length;i++) {
	    			sendPath.add(System.getProperty("user.dir")+sendPaths[i]);
	    		}
	    	}
	        sendMail(sendToMails, DateUtil.getDate()+"cps东经API测试报告", "API自动化测试报告，请下载后查看！\n"
	    			+content, sendPath);
	        System.out.println("-------------------------"+DateUtil.getDate()+"sendMail success!\n发送邮件列表："+sendToMails.toString());
	    	
	    }
	    

}

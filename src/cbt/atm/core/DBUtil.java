package cbt.atm.core;

import java.sql.*;

//���ݿ⹤���ࣺ�����������������ݿ⡢�ر����ݿ�
public class DBUtil {
	//�������ݿ�Ĳ���
	public static final String URL = "jdbc:mysql://localhost:3306/school4";
	public static final String USER = "root";
	public static final String PASSWORD = "mysql";
	//��������
	//ע�⣬��̬����������һ��ʹ�õ�ʱ������һ��
	static{
		try {
			Class.forName("com.mysql.jdbc.Driver");
			System.out.println("���������ɹ���");
		} catch (ClassNotFoundException e) {
			System.err.println("��������ʧ�ܣ�"); 
			e.printStackTrace();
		}		
	}	
	//�������ݿ�
	public static Connection getConn() throws SQLException{
		Connection conn = null;		
		conn = DriverManager.getConnection(URL, USER, PASSWORD); 
		System.out.println("���ӳɹ�"); 
		return conn;
	}	
	//�ر�����
	public static void close(ResultSet rs,Statement stat,Connection conn){
		try {
			if(rs!=null){
				rs.close();
				rs = null;
			}
			if(stat!=null){
				stat.close();
				stat=null;
			}
			if(conn!=null){
				conn.close();
				conn = null;
			}
			System.out.println("���ݿ�رճɹ���");
		} catch (SQLException e) {
			System.err.println("���ݿ�ر�ʧ�ܣ�"); 
			e.printStackTrace();
		}
	}

}

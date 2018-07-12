package cbt.atm.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class DDL {
	
	/*public static void main(String[] args) {
		String[][] s =  new DDL().readExcel("C:\\Users\\admin\\Desktop\\fs\\ddl\\findkey.xls", 
				"Sheet1");
		System.out.println(s[2][0]);
	}*/
	
	//ʹ������ʵ��
	public String getRandomByArray(String[] myString){
		int ranindex = Math.abs(new Random().nextInt(myString.length));
		return myString[ranindex];
	}
	
	//�����
	//ָ�������
	public int getRandomByInt(int min,int max){
		return new Random().nextInt(min)+(max-min)+1;
	}
	
	//���������ڶ������ַ���
	
	
	//��ȡ����excel
	public String[][] readExcel(String fileName,String sheetName){
		File file = new File(fileName);
		String[][] data = null;
		try {
			FileInputStream fis = new FileInputStream(file);
			Workbook rwb = Workbook.getWorkbook(fis);
			Sheet sheet = rwb.getSheet(sheetName);
			//��ȡ�ܵ�����
			int rowCount = sheet.getRows();
			int colCount = sheet.getColumns();
			System.out.println(rowCount+"----->"+colCount);
			data = new String[rowCount-1][colCount];
			
			//����
			for(int i=1;i<rowCount;i++){
				Cell[] cells = sheet.getRow(i);
				for(int j=0;j<cells.length;j++){
					data[i-1][j] = cells[j].getContents();
				}
			}
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (BiffException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}
	
	//ʵ��jdbc����ֵ

}

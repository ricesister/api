package com.sen.api.beans;

/**
 * 
 * @description 外部驱动excel基类
 * @author fs
 * @2018年8月22日
 *
 */
public class BaseBean {

	private String excelName;

	private String sheetName;

	public String getSheetName() {
		return sheetName;
	}

	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}

	public String getExcelName() {
		return excelName;
	}

	public void setExcelName(String excelName) {
		this.excelName = excelName;
	}

}


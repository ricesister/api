package com.sen.api.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 
 * @description 读取excel文件
 * @author fs
 * @2018年8月22日
 *
 */
public class ExcelUtil {

	/**
	 * 获取excel表所有sheet数据
	 * @param clz
	 * @param path
	 * @return
	 */
	public static <T> List<T> readExcel(Class<T> clz, String path) {
		System.out.println(path);
		if (null == path || "".equals(path)) {
			return null;
		}
		InputStream is;
		Workbook xssfWorkbook;
		try {
			is = new FileInputStream(path);
			if (path.endsWith(".xls")) {
				xssfWorkbook = new HSSFWorkbook(is);
			} else {
				xssfWorkbook = new XSSFWorkbook(is);
			}
			is.close();
			int sheetNumber = xssfWorkbook.getNumberOfSheets();
			List<T> allData = new ArrayList<T>();
			for (int i = 0; i < sheetNumber; i++) {
				allData.addAll(transToObject(clz, xssfWorkbook,
						xssfWorkbook.getSheetName(i)));
			}
			return allData;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("转换excel文件失败：" + e.getMessage());
		}
	}
	
	/**
	 * 获取excel表指定sheet表数据
	 * @param clz
	 * @param path
	 * @param sheetName
	 * @return
	 */
	public static <T> List<T> readExcel(Class<T> clz, String path,
			String sheetName) {
		if (null == path || "".equals(path)) {
			return null;
		}
		InputStream is;
		Workbook xssfWorkbook;
		try {
			is = new FileInputStream(path);
			if (path.endsWith(".xls")) {
				xssfWorkbook = new HSSFWorkbook(is);
			} else {
				xssfWorkbook = new XSSFWorkbook(is);
			}
			is.close();
			return transToObject(clz, xssfWorkbook, sheetName);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("转换excel文件失败：" + e.getMessage());
		}

	}

	
	/**
	 * excel数据转换成实体
	 * @param clz 实体class对象
	 * @param xssfWorkbook
	 * @param sheetName sheet名称
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private static <T> List<T> transToObject(Class<T> clz,
			Workbook xssfWorkbook, String sheetName)
			throws InstantiationException, IllegalAccessException,
			InvocationTargetException {
		List<T> list = new ArrayList<T>();
		Sheet xssfSheet = xssfWorkbook.getSheet(sheetName);
		/**
		 * 行头
		 */
		Row firstRow = xssfSheet.getRow(0);
		if(null ==firstRow){
			return list;
		}
		List<Object> heads = getRow(firstRow);
		//添加sheetName字段，用于封装至bean中，与bean中的字段相匹配。
		heads.add("sheetName");
		Map<String, Method> headMethod = getSetMethod(clz, heads);
		for (int rowNum = 1; rowNum <= xssfSheet.getLastRowNum(); rowNum++) {
			try {
				Row xssfRow = xssfSheet.getRow(rowNum);
				if (xssfRow == null) {
					continue;
				}
				T t = clz.newInstance();
				List<Object> data = getRow(xssfRow);
				/**
				 * 对列内容校验
				 */
				while(data.size()+1<heads.size()){
					//TODO
					data.add("");
				}
				data.add(sheetName);
				setValue(t, data, heads, headMethod);
				list.add(t);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	
	/**
	 * run对应的setxx方法
	 * @param clz
	 * @param heads 行头List，有sheetname
	 * @return
	 */
	private static Map<String, Method> getSetMethod(Class<?> clz,
			List<Object> heads) {
		Map<String, Method> map = new HashMap<String, Method>();
		Method[] methods = clz.getMethods();
		/**
		 * 遍历头，反射赋值
		 */
		for (Object head : heads) {
			// boolean find = false;
			for (Method method : methods) {
				if (method.getName().toLowerCase()
						.equals("set" + head.toString().toLowerCase())
						&& method.getParameterTypes().length == 1) {
					map.put(head.toString(), method);
					// find = true;
					break;
				}
			}
			// if (!find) {
			// map.put(head, null);
			// }
		}
		return map;
	}

	/**
	 * 针对set方法输入参数类型不同，对行中每一列的参数转换后输入对象
	 * @param obj 实体类
	 * @param data 一整行的列值
	 * @param heads 行头
	 * @param methods 行对应set方法
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private static void setValue(Object obj, List<Object> data,
			List<Object> heads, Map<String, Method> methods)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		for (Map.Entry<String, Method> entry : methods.entrySet()) {
			Object value = "";
			int dataIndex = heads.indexOf(entry.getKey());
			if (dataIndex < data.size()) {
				value = data.get(heads.indexOf(entry.getKey()));
			}
			Method method = entry.getValue();
			Class<?> param = method.getParameterTypes()[0];
			if (String.class.equals(param)) {
				method.invoke(obj, value);
			} else if (Integer.class.equals(param) || int.class.equals(param)) {
				if(value.toString()==""){
					value=0;
				}
				method.invoke(obj, new BigDecimal(value.toString()).intValue());
			} else if (Long.class.equals(param) || long.class.equals(param)) {	
				if(value.toString()==""){
					value=0;
				}
				method.invoke(obj, new BigDecimal(value.toString()).longValue());
			} else if (Short.class.equals(param) || short.class.equals(param)) {
				if(value.toString()==""){
					value=0;
				}
				method.invoke(obj, new BigDecimal(value.toString()).shortValue());
			} else if (Boolean.class.equals(param)
					|| boolean.class.equals(param)) {
				method.invoke(obj, Boolean.valueOf(value.toString())
						|| value.toString().toLowerCase().equals("y"));
			} else if (JSONObject.class.equals(param)
					|| JSONObject.class.equals(param)) {
				method.invoke(obj, JSONObject.parseObject(value.toString()));
			}else {
				// Date
				method.invoke(obj, value);
			}
		}
	}

	
	/**
	 * 根据行返回列的内容，返回一整行内容
	 * @param xssfRow
	 * @return
	 */
	private static List<Object> getRow(Row xssfRow) {
		List<Object> cells = new ArrayList<Object>();
		if (xssfRow != null) {
			for (short cellNum = 0; cellNum < xssfRow.getLastCellNum(); cellNum++) {
				Cell xssfCell = xssfRow.getCell(cellNum);
				cells.add(getValue(xssfCell));
			}
		}
		return cells;
	}

	/**
	 * 获取列的值，根据类型返回不同值
	 * @param cell
	 * @return
	 */
	private static String getValue(Cell cell) {
		if (null == cell) {
			return "";			
		} else if (cell.getCellTypeEnum() == CellType.BOOLEAN) {
			// 返回布尔类型的值
			return String.valueOf(cell.getBooleanCellValue());
		} else if (cell.getCellTypeEnum() == CellType.NUMERIC) {
			// 返回数值类型的值
			return String.valueOf(cell.getNumericCellValue());
		} else {
			// 返回字符串类型的值
			return String.valueOf(cell.getStringCellValue());
		}
	}
}

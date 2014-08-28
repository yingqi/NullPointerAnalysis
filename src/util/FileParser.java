package util;

import java.io.File;

import bean.MethodPlus;

public class FileParser {
	public static boolean isLineInMethod(String fileName,String className, int lineNumber, MethodPlus methodPlus) throws ClassNotFoundException{
		boolean isLineInMethod = false;
		Class<?> classType = Class.forName(className);
		String packageName = classType.getPackage().getName();
		File file = new File(System.getProperty("user.dir")+"\\src\\"+packageName+"\\"+fileName);
		return isLineInMethod;
	}
	
	public static void main(String args[]){
		System.out.println(System.getProperty("user.dir"));
	}
}

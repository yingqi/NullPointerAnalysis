package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import bean.MethodPlus;


public class FileParser {
	public static boolean isLineInMethod(String fileName,String className, int lineNumber, MethodPlus methodPlus) throws ClassNotFoundException, FileNotFoundException{
		boolean isLineInMethod = false;
		Class<?> classType = Class.forName(className);
		String packageName = classType.getPackage().getName();
		File file = new File(System.getProperty("user.dir")+"\\src\\"+packageName+"\\"+fileName);
		Scanner scanner = new Scanner(file);
		while(scanner.hasNext()){
			
		}
		return isLineInMethod;
	}
	
	public static void main(String args[]){
		System.out.println(System.getProperty("user.dir"));
	}
}

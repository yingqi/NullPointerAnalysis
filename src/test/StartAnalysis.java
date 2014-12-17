package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

/**
 * Start the analysis on a document and parse it to StackTrace.
 * @author leo
 *
 */


public class StartAnalysis {
	
	/**
	 * main method
	 * @param args
	 * @throws FileNotFoundException
	 * @throws ClassNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException{
		List<String> sourceStrings = new ArrayList<>();

		String fileString = "/home/leo/output.txt";
		sourceStrings.add("/home/leo/RunTimeException/NPAException/bin");
		// 0 stands for jar type, 1 stands for class type
		
//		String fileString = "/home/leo/jfc/bug.txt";
//		sourceStrings.add("/home/leo/jfc/jfc");
		int fileType = 1;
		
		
		File file = new File(fileString);
		Scanner fileScanner = new Scanner(file);
		ArrayList<StackTraceElement> stackTraceElements = new ArrayList<StackTraceElement>();
		
		while (fileScanner.hasNextLine()) {
			String lineString = fileScanner.nextLine();
			Scanner lineScanner = new Scanner(lineString);
			if(lineScanner.next().equals("at")){
				while(lineScanner.hasNext()){
					String packageName = "";
					String className = "";
					String methodName = "";
					String fileName = "";

					int lineNumber = 0;
					String lineSpec = lineScanner.next();
					//split the line up
					String[] specs = lineSpec.split("[.)]");
					int index = 0;
					//get package name
					while((index+3)<specs.length){
						packageName+="."+specs[index];
						index++;
					}
					//if package is default
					if(packageName.length()!=0){
						packageName = packageName.substring(1);
					}
					//get class name
					className = specs[index];
					//get method name
					int indexOfMethodEnd = 0;
					while(specs[index+1].charAt(indexOfMethodEnd)!='('){
						indexOfMethodEnd++;
					}
					methodName = specs[index+1].substring(0, indexOfMethodEnd);
					//get the line number
					int indexOfLineNumber = 0;
					while(specs[index+2].charAt(indexOfLineNumber)!=':'){
						indexOfLineNumber++;
					}
					lineNumber = Integer.parseInt(specs[index+2].substring(indexOfLineNumber+1));
					//get the file name
					fileName = specs[index+1].substring(indexOfMethodEnd+1)+"."+specs[index+2].substring(0,indexOfLineNumber);
					System.out.println("Package: "+packageName+"\tClass: "+className+"\tMethod: "+methodName+"\tLineNumber: "+lineNumber+"\tFileName: "+fileName);
					//combine to get the class name
					className = packageName+"."+className;
					StackTraceElement ste = new StackTraceElement(className, methodName, fileName, lineNumber);
					stackTraceElements.add(ste);
				}
			}
			lineScanner.close();
		}
		StackTraceElement[] stackTrace = (StackTraceElement[]) stackTraceElements.toArray(new StackTraceElement[stackTraceElements.size()]);
		fileScanner.close();
		 Analysis analysis = new Analysis(stackTrace,sourceStrings,fileType);
		 // create the CFG first and then do analysis
		 analysis.showCFG();
		 analysis.doAnalysis(stackTrace);
		
	}
}

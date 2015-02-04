package analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

/**
 * Start the analysis on a document and parse it to StackTrace.
 * 
 * @author leo
 * 
 */

public class StartAnalysis {

	/**
	 * main method
	 * 
	 * @param args
	 * @throws FileNotFoundException
	 * @throws ClassNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException {
		long time = System.currentTimeMillis();
		// 1 stands for class type, 2 stands for only classes on the stack trace
		int fileType = 1;
		String fileString = args[1];
		String sourceString = args[2];

		File file = new File(fileString);
		Scanner fileScanner = new Scanner(file);
		ArrayList<StackTraceElement> stackTraceElements = new ArrayList<StackTraceElement>();

		while (fileScanner.hasNextLine()) {
			String lineString = fileScanner.nextLine();
			Scanner lineScanner = new Scanner(lineString);
			if (lineScanner.next().equals("at")) {
				while (lineScanner.hasNext()) {
					String packageName = "";
					String className = "";
					String methodName = "";
					String fileName = "";

					int lineNumber = 0;
					String lineSpec = lineScanner.next();
					// split the line up
					String[] specs = lineSpec.split("[.)]");
					int index = 0;
					// get package name
					while ((index + 3) < specs.length) {
						packageName += "." + specs[index];
						index++;
					}
					// if package is default
					if (packageName.length() != 0) {
						packageName = packageName.substring(1);
					}
					// get class name
					className = specs[index];
					// get method name
					int indexOfMethodEnd = 0;
					while (specs[index + 1].charAt(indexOfMethodEnd) != '(') {
						indexOfMethodEnd++;
					}
					methodName = specs[index + 1].substring(0, indexOfMethodEnd);
					// get the line number
					int indexOfLineNumber = 0;
					while (specs[index + 2].charAt(indexOfLineNumber) != ':') {
						indexOfLineNumber++;
					}
					lineNumber = Integer.parseInt(specs[index + 2].substring(indexOfLineNumber + 1));
					// get the file name
					fileName = specs[index + 1].substring(indexOfMethodEnd + 1) + "."
							+ specs[index + 2].substring(0, indexOfLineNumber);
					// combine to get the class name
					if (packageName.length() != 0) {
						className = packageName + "." + className;
					}
					System.out.println("Class: " + className + "\tMethod: " + methodName + "\tLineNumber: "
							+ lineNumber + "\tFileName: " + fileName);
					StackTraceElement ste = new StackTraceElement(className, methodName, fileName, lineNumber);
					stackTraceElements.add(ste);
				}
			}
			lineScanner.close();
		}
		StackTraceElement[] stackTrace = (StackTraceElement[]) stackTraceElements
				.toArray(new StackTraceElement[stackTraceElements.size()]);
		fileScanner.close();
		System.out.println(new Date().getTime() - time);
		Analysis analysis = new Analysis(stackTrace, sourceString, fileType, time);
		System.out.println("CFG created! " + (new Date().getTime() - time));
		analysis.doAnalysis(stackTrace, time);
		System.out.println(new Date().getTime() - time);

	}
}

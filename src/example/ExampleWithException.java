package example;

import java.beans.PropertyChangeEvent;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import test.Analysis;


public class ExampleWithException {
	String string1, string2;
	int val;

	public static void main(String[] args) throws ClassNotFoundException, FileNotFoundException {
		try {
			ExampleWithException rTEExampleException = new ExampleWithException(args[0].length());
			if (args.length == 1) {
				rTEExampleException.method2();
				rTEExampleException.method4();
			} else {
				int num = Integer.parseInt(args[1]);
				rTEExampleException.method1(num);
			}
			rTEExampleException.method4(1);
		} catch (NullPointerException e) {
			 e.printStackTrace();
			 System.out.println("************");
			 System.out.println(e.getStackTrace()[0]);
			 List<String> paths = new ArrayList<>();
			 paths.add("/home/leo/RunTimeException/NullPointerAnalysis/bin");
			 Analysis analysis = new Analysis(e.getStackTrace(), paths, 1);
			 analysis.showCFG();
			 analysis.doAnalysis(e.getStackTrace());
		}
	}

	public ExampleWithException(int i) {
		val = i;
		string1 = null;
		string2 = null;
		if (val > 1) {
			string2 = new String("abc");
		}
	}

	public void method1(int j) {
		if (j == 0) {
			String tempStr = null;
			string1 = tempStr;
		} else {
			string1 = null;
		}
	}

	public void method2() {
		string1 = string2;
	}

	public void method4(int i) {
		if (val > 1) {
			string2.charAt(0);
		}
		string1.charAt(0);
	}

	public void method4() {

		method4(1);
	}
}

package example;

public class Example {
	String string1, string2;
	int val;

	public static void main(String[] args) {
		Example rteExample = new Example(args[0].length());
		if (args.length == 1) {
			rteExample.method2();
			rteExample.method4();
		} else {
			int num = Integer.parseInt(args[1]);
			rteExample.method1(num);
		}
		rteExample.method3();
	}

	public Example(int i) {
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

//	public void method1() {
//		String tempStr = null;
//		string1 = tempStr;
//	}

	public void method2() {

		string1 = string2;
	}

	public void method3() {
		if (val > 1) {
			string2.charAt(0);
		}
		string1.charAt(0);
	}

	public void method4() {

		method3();
	}
}

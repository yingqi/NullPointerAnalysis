package example;


public class ExceptionWithMultipleAssignments {
	public Integer bigInt1, bigInt2;
	public int i;
	
	public static void main(String[] args) {
		ExceptionWithMultipleAssignments example1 = new ExceptionWithMultipleAssignments(args.length);
		if(args.length==0){
			example1.method0();
		}else{
			example1.method1((args.length)/2);
		}
	}
	
	public ExceptionWithMultipleAssignments(int i) {
		bigInt1 = null;
		bigInt2 = null;	
		this.i=i;
	}
	
	public void method0(){
		bigInt1 = new Integer(i);
		method2();
	}
	
	public void method1(int number){
		if(number>1){
			bigInt1 = new Integer(number);
		}else{
			bigInt2 = null;
		}
		method2();
	}
	
	public void method2(){
		bigInt1 = bigInt2;
		method3();
	}
	
	public void method3(){
		bigInt1.intValue();
	}
}

//无输入运行
//Exception in thread "main" java.lang.NullPointerException
//at example.ExceptionWithMultipleAssignments.method3(ExceptionWithMultipleAssignments.java:43)
//at example.ExceptionWithMultipleAssignments.method2(ExceptionWithMultipleAssignments.java:39)
//at example.ExceptionWithMultipleAssignments.method0(ExceptionWithMultipleAssignments.java:25)
//at example.ExceptionWithMultipleAssignments.main(ExceptionWithMultipleAssignments.java:11)
//引起空指针异常的是第9行
//输入为x，运行
//Exception in thread "main" java.lang.NullPointerException
//at example.ExceptionWithMultipleAssignments.method3(ExceptionWithMultipleAssignments.java:43)
//at example.ExceptionWithMultipleAssignments.method2(ExceptionWithMultipleAssignments.java:39)
//at example.ExceptionWithMultipleAssignments.method1(ExceptionWithMultipleAssignments.java:34)
//at example.ExceptionWithMultipleAssignments.main(ExceptionWithMultipleAssignments.java:13)
//引起空指针异常的是第19行或者第9行

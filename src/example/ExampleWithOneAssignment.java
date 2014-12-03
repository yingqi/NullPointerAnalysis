package example;


public class ExampleWithOneAssignment {
	public Integer bigInt1, bigInt2;
	public int i;
	
	public static void main(String[] args) {
		ExampleWithOneAssignment example1 = new ExampleWithOneAssignment();
		if(args.length/2==0){
			example1.method1();
		}else{
			example1.method1(example1.i);
		}
		
		if (args.length == 0) {
			example1.method2();
		}
	}
	
	public ExampleWithOneAssignment() {
		bigInt1 = null;
		bigInt2 = null;	
	}
	
	public void method1(){
		bigInt1 = new Integer(i);
	}
	
	public void method1(int number){
		bigInt2 = new Integer(number);
		if(number>1){
			bigInt1 = null;
		}
	}
	
	public void method2(){
		bigInt1 = bigInt2;
		method3();
	}
	
	public void method3(){
		bigInt1.intValue();
	}
	
}

//Exception in thread "main" java.lang.NullPointerException
//at example.ExampleWithOneAssignment.method3(ExampleWithOneAssignment.java:43)
//at example.ExampleWithOneAssignment.method2(ExampleWithOneAssignment.java:39)
//at example.ExampleWithOneAssignment.main(ExampleWithOneAssignment.java:17)
//引起空指针异常的赋值点在23行

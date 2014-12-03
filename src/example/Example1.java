package example;

import java.util.Random;

public class Example1 {
	public BigInt bigInt1, bigInt2;
	public int i;
	
	public static void main(String[] args) {
		Example1 example1 = new Example1();
		if(args.length/2==0){
			example1.method1();
		}else{
			example1.method1(example1.i);
		}
		
		if (args.length == 0) {
			example1.method2();
		}
	}
	
	public Example1() {
		bigInt1 = null;
		bigInt2 = null;
		Random random = new Random();
		i = random.nextInt();
	}
	
	public void method1(){
		bigInt1 = new BigInt(i);
	}
	
	public void method1(int number){
		bigInt2 = new BigInt(number);
	}
	
	public void method2(){
		bigInt1 = bigInt2;
		method3();
	}
	
	public void method3(){
		bigInt1.getBigInt();
	}
	 
}

package internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import soot.Body;
import soot.Local;
import soot.SootMethod;
import soot.Type;
import soot.util.Chain;

/**
 * class MethodPlus is a class represents method which is compatible with both
 * sootMethod and the method we use.
 * 
 * @author Yingqi
 * 
 */
public class MethodPlus {
	private String methodName;
	private List<Type> parameterSootTypes;
	private String className;
	private List<Local> parameterLocals;
	private int parameterCount;
//	private SootMethod sootMethod;
	//Q4: can sootmethod abandoned?

	/**
	 * initiate method
	 * 
	 * @param methodName
	 * @param parameterSootTypes
	 */
	public MethodPlus(String methodName, String className,
			List<Type> parameterSootTypes,List<Local> parameterLocals, int parameterCount) {
		this.methodName = methodName;
		this.className = className;
		this.parameterSootTypes = parameterSootTypes;
		this.parameterLocals = parameterLocals;
		this.parameterCount = parameterCount;
	}
	

	/**
	 * get method name.
	 * 
	 * @return method name
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * get class name.
	 * 
	 * @return class name
	 */
	public String getclassName() {
		return className;
	}

	/**
	 * get parameters in soot types.
	 * 
	 * @return parameters in soot types
	 */
	public List<Type> getParameterSootTypes() {
		return parameterSootTypes;
	}
	
//	public SootMethod getSootmethod(){
//		return sootMethod;
//	}
	
	public List<Local> getParameterLocals(){
		return parameterLocals;
	}
	
	public int getParameterCount(){
		return parameterCount;
	}
	
	public Local getParameterLocal(int i){
		return parameterLocals.get(i);
	}

	@Override
	public String toString() {
		String methodString =className+"."+ methodName;
		for (Type parameterSootType : parameterSootTypes) {
			methodString += '\t' + parameterSootType.toString();
		}
		return methodString;
	}
	
	@Override
	public boolean equals(Object object){
		if(! (object instanceof MethodPlus)){
			return false;
		}else {
			MethodPlus methodPlus = (MethodPlus) object;
			return equalTypes(methodPlus.getParameterSootTypes())&&this.className.equals(methodPlus.className)&&this.methodName.equals(methodPlus.getMethodName());
		}
	}
	
	public boolean euqalTo(SootMethod sootMethod){
		return equalTypes(sootMethod.getParameterTypes())&&this.methodName.equals(sootMethod.getName());
	}
	
	private boolean equalTypes(List<Type> parameterSootTypes){
		boolean equalTypes = this.parameterSootTypes.size()==parameterSootTypes.size();
		if(equalTypes){
			for(int i =0;i<parameterSootTypes.size();i++){
				if(!this.parameterSootTypes.get(i).toString().equals(parameterSootTypes.get(i).toString())){
					equalTypes = false;
				}
			}
		}
		return equalTypes;
	}
}

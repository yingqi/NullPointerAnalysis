package internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import soot.Body;
import soot.Local;
import soot.SootClass;
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
	private SootClass sootClass;
	private UnitPlus headPlus;
	private UnitPlus tailPlus;
	private Body body;

	public UnitPlus getHeadPlus() {
		return headPlus;
	}


	public void setHeadPlus(UnitPlus headPlus) {
		this.headPlus = headPlus;
	}


	public UnitPlus getTailPlus() {
		return tailPlus;
	}


	public void setTailPlus(UnitPlus tailPlus) {
		this.tailPlus = tailPlus;
	}


	/**
	 * initiate method
	 * 
	 * @param methodName
	 * @param parameterSootTypes
	 */
	public MethodPlus(String methodName, String className,
			List<Type> parameterSootTypes,SootClass sootClass, Body body) {
		this.methodName = methodName;
		this.className = className;
		this.parameterSootTypes = parameterSootTypes;
		this.sootClass = sootClass;
		this.body = body;
	}
	

	public Body getBody() {
		return body;
	}


	public void setBody(Body body) {
		this.body = body;
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
	
	@Override
	public String toString() {
		String methodString =className+"."+ methodName;
		for (Type parameterSootType : parameterSootTypes) {
			methodString += "  Parameter: " + parameterSootType.toString();
		}
		return methodString+'\t';
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
	
	/**
	 * If we cannot track the class, we just compare soot method and its parameter name
	 * @param sootMethod
	 * @return
	 */
	public boolean equalTo(SootMethod sootMethod){
		return equalTypes(sootMethod.getParameterTypes())&&this.methodName.equals(sootMethod.getName());
	}
	
	/**
	 * helper method to check whether the types are equal
	 * @param parameterSootTypes
	 * @return
	 */
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


	public SootClass getSootClass() {
		return sootClass;
	}


	public void setSootClass(SootClass sootClass) {
		this.sootClass = sootClass;
	}
}

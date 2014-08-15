package bean;

import java.util.List;
import soot.Type;

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

	/**
	 * initiate method
	 * 
	 * @param methodName
	 * @param parameterSootTypes
	 */
	public MethodPlus(String methodName, String className,
			List<Type> parameterSootTypes) {
		this.methodName = methodName;
		this.className = className;
		this.parameterSootTypes = parameterSootTypes;
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
		String methodString = methodName;
		for (Type parameterSootType : parameterSootTypes) {
			methodString = methodString + " " + parameterSootType.toString();
		}
		return methodString;
	}
}

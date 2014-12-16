package internal;

import soot.Value;


/**
 * This class is the state of a node
 */
/**
 * @version 2014-08-06
 * @author Yingqi
 *
 */
public class State
{
	private Value variable;
	private String attribute;
	private MethodPlus methodPlus;
	
	/**
	 * constructor
	 * @param initValue
	 */
	public State(Value initValue, MethodPlus methodPlus){
		variable = initValue;
		attribute = "";
		this.methodPlus = methodPlus;
	}
	
	/**
	 * replace the value in a state
	 * Remember to check NPA before we encounter replaceValue
	 * @param value
	 */
	public void replaceValue(Value value, MethodPlus methodPlus){
		variable = value;
		this.methodPlus = methodPlus;
	}
	
	/**
	 * get the value
	 * @return
	 */
	public Value getValue(){
		return variable;
	}
	
	/**
	 * get the method
	 * @return
	 */
	public MethodPlus getmethod(){
		return methodPlus;
	}
	
	@Override
	public String toString(){
		return methodPlus.toString()+"\tValue: "+variable.toString()+" "+attribute;
	}
	
	@Override
	public boolean equals(Object object){
		if(! (object instanceof State)){
			return false;
		}else {
			State state = (State) object;
			//same as Q2
			return this.getValue().toString().equals(state.getValue().toString())
					&&this.getmethod().equals(state.getmethod())
					;
		}
	}

	public String getAttribute() {
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
	
}
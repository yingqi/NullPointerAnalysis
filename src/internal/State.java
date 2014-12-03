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
	
	/**
	 * constructor
	 * @param initValue
	 */
	public State(Value initValue){
		variable = initValue;
		attribute = "";
	}
	
	/**
	 * replace the value in a state
	 * @param value
	 */
	public void replaceValue(Value value){
		variable = value;
	}
	
	/**
	 * get the value
	 * @return
	 */
	public Value getValue(){
		return variable;
	}
	
	@Override
	public String toString(){
		return variable.toString()+" "+attribute;
	}
	
	@Override
	public boolean equals(Object object){
		if(! (object instanceof State)){
			return false;
		}else {
			State state = (State) object;
			//same as Q2
			return this.getValue().toString().equals(state.getValue().toString());
		}
	}

	public String getAttribute() {
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
	
}
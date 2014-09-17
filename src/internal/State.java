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
	
	public State(Value initValue){
		variable = initValue;
	}
	
	public void replaceValue(Value value){
		variable = value;
	}
	
	public Value getValue(){
		return variable;
	}
	
	
	
}
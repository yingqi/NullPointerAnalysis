package internal;

import dispatcher.LightDispatcher;
import soot.Local;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.Expr;
import soot.jimple.InstanceFieldRef;
/**
 * This class is the state of a node
 */
/**
 * @version 2014-08-06
 * @author Yingqi
 * 
 */
public class State {
	private Value variable;
	private Value baseValue;
	private MethodPlus methodPlus;
	private MethodPlus returnInMethodPlus;
	private boolean isNormalValue;
	private boolean isReturnValue;
	private boolean isArrayBaseValue;
	private boolean isArrayValue;
	private boolean isBaseValue;
	private boolean hasBaseValue;

	public Value getVariable() {
		return variable;
	}


	/**
	 * constructor
	 * 
	 * @param initValue
	 */
	public State(State state) {
		isNormalValue = state.isNormalValue();
		isArrayValue = state.isArrayValue();
		isArrayBaseValue = state.isArrayBaseValue();
		isReturnValue = state.isReturnValue();
		isBaseValue = state.isBaseValue();
		hasBaseValue = state.hasBaseValue();
		variable = state.getVariable();
		methodPlus = state.getmethod();
		returnInMethodPlus = state.getReturnInMethodPlus();
		baseValue = state.baseValue;
	}


	public State(Value initValue, MethodPlus methodPlus) {
		isNormalValue = true;
		isArrayValue = false;
		isArrayBaseValue = false;
		isReturnValue = false;
		isBaseValue = false;
		hasBaseValue=false;
		variable = initValue;
		this.methodPlus = methodPlus;
		baseValue = null;
	}
	
	public State(Value initValue, MethodPlus methodPlus, State state) {
		isNormalValue = state.isNormalValue();
		isArrayValue = state.isArrayValue();
		isArrayBaseValue = state.isArrayBaseValue();
		isBaseValue = state.isBaseValue();
		
		variable = initValue;
		this.methodPlus = methodPlus;
//		if(state.isReturnValue()){
//			isReturnValue = state.isReturnValue();
//			returnInMethodPlus = state.getReturnInMethodPlus();
//		}
		if(state.hasBaseValue){
			hasBaseValue=state.hasBaseValue();
			baseValue = state.getBaseValue();
		}
	}

	public MethodPlus getReturnInMethodPlus() {
		return returnInMethodPlus;
	}

	public void setReturnInMethodPlus(MethodPlus returnInMethodPlus) {
		this.returnInMethodPlus = returnInMethodPlus;
	}

	/**
	 * replace the value in a state Remember to check NPA before we encounter replaceValue
	 * 
	 * @param value
	 */
	public void replaceValue(Value value, UnitPlus unitPlus) {
		if (value instanceof Expr) {
			System.out.println("Error value cannot be replaced with Expr: " + value);
		} else {
			System.out.println("Value Replace In: " + unitPlus);
			System.out.println("Value Replace: " + this.methodPlus + " " + variable + "\tto "
					+ unitPlus.getMethodPlus() + " " + value);
			variable = value;
			this.methodPlus = unitPlus.getMethodPlus();
		}
	}

	public boolean equalValue(Value value, MethodPlus methodPlus){
		return LightDispatcher.equalTwoValues(variable, value);
	}
	
	/**
	 * get the method
	 * 
	 * @return
	 */
	public MethodPlus getmethod() {
		return methodPlus;
	}

	@Override
	public String toString() {
		if(isNormalValue){
			return  variable.toString() + " ";
		}else {
			return " ";
		}
	}

	@Override
	public boolean equals(Object object) {
		return equalTo(object);
	}
	
	public boolean completeEqual(Object object) {
		boolean equals = false;
		if (!(object instanceof State)) {
			equals = false;
		} else {
			State state = (State) object;
			if (state.isReturnValue) {
				equals = isReturnValue;
			} else {
				if (equalValueType(state)) {
					equals = equalValue(state.getVariable(), state.getmethod());
				} else {
					equals = false;
				}
			}
		}
		return equals;
	}
	
	public void mergeState(State state){
		isArrayBaseValue = state.isArrayBaseValue()||isArrayBaseValue;
		isBaseValue = state.isArrayBaseValue()||isBaseValue;
		isArrayValue = state.isArrayValue()||isArrayValue;
		hasBaseValue = state.hasBaseValue()||hasBaseValue;
		isNormalValue = state.isNormalValue()||isNormalValue;
	}

	public boolean equalTo(Object object) {
		boolean equals = false;
		if (!(object instanceof State)) {
			equals = false;
		} else {
			State state = (State) object;
			if (state.isReturnValue()||isReturnValue) {
				if(state.isReturnValue()&&isReturnValue){
					equals = returnInMethodPlus.equals(state.getReturnInMethodPlus());
				}else {
					equals =false;
				}
			} else {
					equals = equalValue(state.getVariable(), state.getmethod());
			}
		}
		return equals;
	}

	private boolean equalValueType(State state) {
		return (isArrayBaseValue == state.isArrayBaseValue) && (isArrayValue == state.isArrayValue)
				&& (isNormalValue == state.isNormalValue) && (isReturnValue == state.isReturnValue);
	}

	public boolean allValueFalse() {
		return !(isNormalValue || isReturnValue);
	}

	public Value getArrayBaseValue() {
		if (variable instanceof ArrayRef) {
			ArrayRef arrayRef = (ArrayRef) variable;
			return arrayRef.getBase();
		}else {
			System.out.println("Error No array base");
			return null;
		}
	}
	
	public Value getBaseValue() {
		return baseValue;
	}


	public boolean isBaseValue() {
		return isBaseValue;
	}

	public boolean isNormalValue() {
		return isNormalValue;
	}

	public boolean isReturnValue() {
		return isReturnValue;
	}

	public boolean isArrayBaseValue() {
		return isArrayBaseValue;
	}

	public boolean isArrayValue() {
		return isArrayValue;
	}

	public boolean hasBaseValue() {
		return hasBaseValue;
	}
	
	public void setReturnValue(MethodPlus methodPlus) {
		isReturnValue = true;
		this.returnInMethodPlus = methodPlus;
	}
	
	public void desetReturnValue() {
		isReturnValue = false;
		this.returnInMethodPlus = null;
		isNormalValue = true;
	}
	
	public void setHasBaseState(Value baseValue) {
		hasBaseValue = true;
		this.baseValue = baseValue;
	}
	
	public void setIsBaseState(){
		isBaseValue = true;
	}
	
	public void deSetIsBaseState(){
		isBaseValue = false;
	}
	
	public void deSetHasBaseState() {
		hasBaseValue = false;
		baseValue = null;
	}

	public void desetArrayBaseValue() {
		isArrayBaseValue = false;
	}
	
	public void desetArrayValue() {
		isArrayValue = false;
	}

	public void desetNormalValue() {
		isNormalValue = false;
	}

	public void setNormalValue() {
		isNormalValue = true;
	}

	public void setArrayBaseValue() {
		isArrayBaseValue = true;
	}

	public void updateFieldBase(Value base){
		baseValue = base;
	}
	
	public void updateArrayBase(Value base) {
		if (variable instanceof ArrayRef) {
			ArrayRef arrayRef = (ArrayRef) variable;
			if (base instanceof Local) {
				Local local = (Local) base;
				arrayRef.setBase(local);
			}else {
				System.out.println("Error in Update Array Base!\nBase is not local!");
			}
		}
	}

	public void setArrayValue() {
		isArrayValue = true;
	}

}
package internal;

import dispatcher.LightDispatcher;
import soot.Local;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.Expr;
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
	// private String attribute;
	private LightDispatcher lightDispatcher;
	private MethodPlus methodPlus;
	private MethodPlus returnInMethodPlus;
//	private Value baseValue;
//	private Value arrayBaseValue;
//	private Value arrayIndexValue;
//	private SootClass baseSootClass;
//	private SootField field;
	private boolean isNormalValue;
	private boolean isReturnValue;
	private boolean isArrayBaseValue;
	private boolean isArrayValue;

	public Value getVariable() {
		return variable;
	}

	public MethodPlus getMethodPlus() {
		return methodPlus;
	}

//	public Value getBaseValue() {
//		return baseValue;
//	}

	public Value getArrayBaseValue() {
		if (variable instanceof ArrayRef) {
			ArrayRef arrayRef = (ArrayRef) variable;
			return arrayRef.getBase();
		}else {
			return null;
		}
	}
//
//	public Value getArrayIndexValue() {
//		return arrayIndexValue;
//	}

//	public SootClass getBaseSootClass() {
//		return baseSootClass;
//	}
//
//	public SootField getField() {
//		return field;
//	}

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

	/**
	 * constructor
	 * 
	 * @param initValue
	 */
	public State(State state) {
		lightDispatcher = new LightDispatcher();
		isNormalValue = state.isNormalValue();
		isArrayValue = state.isArrayValue();
		isArrayBaseValue = state.isArrayBaseValue();
		isReturnValue = state.isReturnValue();
		variable = state.getVariable();
		methodPlus = state.getmethod();
		returnInMethodPlus = state.getReturnInMethodPlus();
//		field = state.getField();
//		baseSootClass = state.getBaseSootClass();
//		baseValue = state.getBaseValue();
//		arrayBaseValue = state.getArrayBaseValue();
//		arrayIndexValue = state.getArrayIndexValue();
	}

	public State(Value initValue, MethodPlus methodPlus) {
		lightDispatcher = new LightDispatcher();
		isNormalValue = true;
		isArrayValue = false;
		isArrayBaseValue = false;
		isReturnValue = false;
		variable = initValue;
		this.methodPlus = methodPlus;
//		baseValue = null;
//		field = null;
//		baseSootClass = null;
//		arrayBaseValue = null;
//		arrayIndexValue = null;
//		if (variable instanceof Ref) {
//			Ref ref = (Ref) variable;
//			if (ref instanceof InstanceFieldRef) {
//				InstanceFieldRef instanceFieldRef = (InstanceFieldRef) ref;
//				this.field = instanceFieldRef.getField();
//				this.baseValue = instanceFieldRef.getBase();
//				if (baseValue.getType() instanceof RefType) {
//					RefType refType = (RefType) baseValue.getType();
//					this.baseSootClass = refType.getSootClass();
//				}
//			} else if (ref instanceof ArrayRef) {
//				ArrayRef arrayRef = (ArrayRef) ref;
//				arrayBaseValue = arrayRef.getBase();
//				arrayIndexValue = arrayRef.getIndex();
//			}
//		}
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
		return lightDispatcher.equalTwoValues(variable, this.methodPlus, value, methodPlus);
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
		return methodPlus.toString() + "\tValue: " + variable.toString() + " ";
	}

	@Override
	public boolean equals(Object object) {
		return equalTo(object);
	}

	public boolean equalTo(Object object) {
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

	private boolean equalValueType(State state) {
		return (isArrayBaseValue == state.isArrayBaseValue) && (isArrayValue == state.isArrayValue)
				&& (isNormalValue == state.isNormalValue) && (isReturnValue == state.isReturnValue);
	}

	public boolean allValueFalse() {
		return !(isArrayBaseValue || isNormalValue || isReturnValue);
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

	public void desetArrayBaseValue() {
		isArrayBaseValue = false;
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

	public void updateArrayBase(Value base) {
//		this.arrayBaseValue = base;
		if (variable instanceof ArrayRef) {
			ArrayRef arrayRef = (ArrayRef) variable;
			if (base instanceof Local) {
				Local local = (Local) base;
				arrayRef.setBase(local);
			}
		}
	}

	public void setArrayValue(Value base, Value index) {
//		this.arrayBaseValue = base;
//		this.arrayIndexValue = index;
		isArrayValue = true;
	}

}
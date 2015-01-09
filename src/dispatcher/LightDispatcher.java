package dispatcher;

import internal.MethodPlus;
import internal.State;
import internal.UnitPlus;

import java.util.HashSet;
import java.util.Set;

import soot.Immediate;
import soot.RefType;
import soot.SootClass;
import soot.SootField;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.Expr;
import soot.jimple.InstanceFieldRef;
import soot.jimple.Ref;
import soot.jimple.internal.AbstractDefinitionStmt;
import soot.jimple.internal.BeginStmt;

public class LightDispatcher {
	public static void stateReplace(State state, Value value, UnitPlus unitPlus, Set<State> states, Set<State> removeStates,
			Set<State> addStates) {
		State tempState = stateReplaceHelper(state, value, unitPlus, states, removeStates);
		if (tempState != null) {
			addStates.add(tempState);
		}
	}

	private static State stateReplaceHelper(State state, Value value, UnitPlus unitPlus, Set<State> states,
			Set<State> removeStates) {
		State addState = null;
		boolean isNewStateInStates = false;
		State newState = new State(value, unitPlus.getMethodPlus());
		for (State stateInSet : states) {
			if (stateInSet.equalTo(newState)) {
				if (stateInSet.isArrayBaseValue()||stateInSet.isReturnValue()) {
					stateInSet.setNormalValue();
				}
				isNewStateInStates = true;
				removeStates.add(state);
			}
		}
		if (!isNewStateInStates) {
			if (state.isArrayValue()) {
				if (value instanceof ArrayRef) {
					ArrayRef arrayRef = (ArrayRef) value;
					Value base = arrayRef.getBase();
					boolean isBaseInStates = false;
					for(State state2:states){
						if(state2.isArrayBaseValue()&&state2.equalValue(base, unitPlus.getMethodPlus())){
							isBaseInStates = true;
						}
					}
					if(!isBaseInStates){
						addState = new State(arrayRef.getBase(), unitPlus.getMethodPlus());
						addState.desetNormalValue();
						addState.setArrayBaseValue();
						for(State state2:states){
							if(state2.equalTo(addState)){
								state2.setArrayBaseValue();
								addState = null;
							}
						}
						state.setArrayValue(arrayRef.getBase(), arrayRef.getIndex());
					}
				} else {
					for (State state2 : states) {
						if (equalTwoValues(state2.getVariable(), state2.getmethod(), state.getArrayBaseValue(), state.getmethod())
						) {
							if (state2.isArrayBaseValue()) {
								state2.desetArrayBaseValue();
								if(state2.allValueFalse()){
									removeStates.add(state2);
								}
							}
						}
					}
				}
			}else {
				if (value instanceof ArrayRef) {
					ArrayRef arrayRef = (ArrayRef) value;
					addState = new State(arrayRef.getBase(), unitPlus.getMethodPlus());
					addState.desetNormalValue();
					addState.setArrayBaseValue();
					for(State state2:states){
						if(state2.equalTo(addState)){
							state2.setArrayBaseValue();
							addState = null;
						}
					}
					state.setArrayValue(arrayRef.getBase(), arrayRef.getIndex());
				}
				if (state.isArrayBaseValue()) {
					for (State state2 : states) {
						if (state2.isArrayValue()) {
							if (equalTwoValues(state2.getArrayBaseValue(), state2.getmethod(), state.getVariable(), state.getmethod())
							) {
								state2.updateArrayBase(value);
							}
						}
					}
				}
			}
			state.replaceValue(value, unitPlus);
		}
		return addState;
	}
	
	
	public static void AddNPA(State state, UnitPlus unitPlus, Set<UnitPlus> NPA, Set<State> states, Set<State> removeStates){
		if(!NPA.contains(unitPlus)){
			NPA.add(unitPlus);
			System.out.println("Possible NPA: " + unitPlus);
		}
		removeStates.add(state);
		if(state.isArrayValue()){
			for(State state2:states){
				if(state2.isArrayBaseValue()&&state2.equalValue(state.getArrayBaseValue(), unitPlus.getMethodPlus())){
					state2.desetArrayBaseValue();
					if(state2.allValueFalse()){
						removeStates.add(state2);
					}
				}
			}
		}
	}
	
	
	public static boolean equalTwoValues(Value value1, MethodPlus methodPlus1, Value value2, MethodPlus methodPlus2) {
		boolean equalValue = false;
		if (value2 instanceof Expr) {
			if (value2.toString().equals(value1.toString())) {
				System.out.println("Error Expr: " + value2);
			}
			equalValue = false;
		} else if (value2 instanceof Immediate) {
			equalValue = value2.equals(value1) && methodPlus1.equals(methodPlus2);
		} else if (value2 instanceof Ref) {
			Ref ref2 = (Ref) value2;
			if (value1 instanceof Ref) {
				Ref ref1 = (Ref) value1;
				equalValue = equalRef(ref1, ref2, methodPlus1, methodPlus2);
			} else {
				equalValue = false;
			}
		}
		return equalValue;
	}
	
	private static boolean equalRef(Ref ref1, Ref ref2, MethodPlus methodPlus1, MethodPlus methodPlus2) {
		boolean equalRef = false;
		if (ref2 instanceof ArrayRef) {
			if (ref1 instanceof ArrayRef) {
				ArrayRef arrayRef1 = (ArrayRef) ref1;
				ArrayRef arrayRef2 = (ArrayRef) ref2;
				equalRef = equalArrayValue(arrayRef1.getBase(), arrayRef2.getBase(), arrayRef1.getIndex(), arrayRef2.getIndex(), methodPlus1, methodPlus2);
			} else {
				equalRef = false;
			}
		} else {
			equalRef = ref2.toString().equals(ref1.toString());
			if (!equalRef) {
				if (ref2 instanceof InstanceFieldRef) {
					if (ref1 instanceof InstanceFieldRef) {
						InstanceFieldRef instanceFieldRef1 = (InstanceFieldRef) ref1;
						SootField valueField1 = instanceFieldRef1.getField();
						InstanceFieldRef instanceFieldRef2 = (InstanceFieldRef) ref2;
						SootField valueField2 = instanceFieldRef2.getField();
						if (valueField1.equals(valueField2)) {
							Value base1 = instanceFieldRef1.getBase();
							Value base2 = instanceFieldRef2.getBase();
							if (base1.getType() instanceof RefType) {
								if (base2.getType() instanceof RefType) {
									RefType refType1 = (RefType) base1.getType();
									SootClass valueBaseSootClass1 = refType1.getSootClass();
									RefType refType2 = (RefType) base2.getType();
									SootClass valueBaseSootClass2 = refType2.getSootClass();
									if ((!valueBaseSootClass2.getFields().contains(valueField1))
											|| (!valueBaseSootClass1.getFields().contains(valueField2))) {
										equalRef = isParent(valueBaseSootClass1, valueBaseSootClass2)
												|| isParent(valueBaseSootClass2, valueBaseSootClass1);
									}else {
										equalRef = false;
									}
								}else {
									equalRef = false;
								}
							}else {
								equalRef = false;
							}
						}else {
							equalRef = false;
						}
						
					}else {
						equalRef = false;
					}
				}
			}
		}
		return equalRef;
	}

	private static boolean equalArrayValue(Value base1, Value base2, Value index1, Value index2, MethodPlus methodPlus1, MethodPlus methodPlus2) {;
		return equalTwoValues(base1, methodPlus1, base2, methodPlus2)
				&&index1.toString().equals(index2.toString());
	}

	private static boolean isParent(SootClass childClass, SootClass fatherClass) {
		boolean isParent = false;
		SootClass tempSootClass = childClass;
		while (tempSootClass.hasSuperclass()) {
			if (tempSootClass.equals(fatherClass)) {
				isParent = true;
				break;
			}
			tempSootClass = tempSootClass.getSuperclass();
		}
		return isParent;
	}
	
	public static Set<State> copyStates(Set<State> originalStates) {
		Set<State> newStates = new HashSet<>();
		for(State state : originalStates){
			newStates.add(new State(state));
		}
		return newStates;
	}

	public static boolean isTransform(UnitPlus unitPlus) {
		boolean isTransform = false;
		if (unitPlus.getUnit() instanceof AbstractDefinitionStmt) {
			isTransform = true;
//			AbstractDefinitionStmt abstractDefinitionStmt = (AbstractDefinitionStmt) unitPlus.getUnit();
//			Value rightValue = abstractDefinitionStmt.getRightOp();
			// Regardless of whether right value is invoke expression, as long as it is not call
			// the unit is a transform
			if(unitPlus.isCall()){
				isTransform = false;
			}
		}
		return isTransform;
	}
	
	public static boolean isEntry(UnitPlus unitPlus) {
		boolean isEntry = false;
		if (unitPlus.getUnit() instanceof BeginStmt) {
			isEntry = true;
		}
		return isEntry;
	}

	
}

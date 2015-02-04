package dispatcher;

import internal.MethodPlus;
import internal.State;
import internal.UnitPlus;

import java.util.HashSet;
import java.util.Set;

import soot.Immediate;
import soot.Local;
import soot.PointsToAnalysis;
import soot.RefType;
import soot.SootClass;
import soot.SootField;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.Expr;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.Ref;
import soot.jimple.internal.AbstractDefinitionStmt;
import soot.jimple.internal.BeginStmt;
import soot.jimple.toolkits.pointer.FullObjectSet;

public class LightDispatcher {

	public static PointsToAnalysis pta;

	public static void stateReplace(State state, Value value, UnitPlus unitPlus, Set<State> states,
			Set<State> removeStates, Set<State> addStates) {
		boolean isNewStateInStates = false;
		State newState = new State(value, unitPlus.getMethodPlus(), state);
		for (State stateInSet : states) {
			if (stateInSet.equalTo(newState)) {
				stateInSet.mergeState(newState);
				isNewStateInStates = true;
				deleteState(state, states, removeStates);
			}
		}
		if (!isNewStateInStates) {
			State tempState1 = replaceArrayState(state, value, unitPlus, states, removeStates);
			if (tempState1 != null) {
				if (!stateSetContains(addStates, tempState1)) {
					addStates.add(tempState1);
				}
			}

			State tempState2 = replaceFieldState(state, value, unitPlus, states, removeStates);
			if (tempState2 != null) {
				if (!stateSetContains(addStates, tempState2)) {
					addStates.add(tempState2);
				}
			}
			state.replaceValue(value, unitPlus);
		}
	}

	private static State replaceFieldState(State state, Value value, UnitPlus unitPlus, Set<State> states,
			Set<State> removeStates) {
		State addState = null;
		if (state.hasBaseValue()) {
			if (value instanceof InstanceFieldRef) {
				InstanceFieldRef instanceFieldRef = (InstanceFieldRef) value;
				Value base = instanceFieldRef.getBase();
				boolean isBaseInStates = false;
				for (State state2 : states) {
					if (state2.isBaseValue() && state2.equalValue(base, unitPlus.getMethodPlus())) {
						isBaseInStates = true;
					}
				}
				if (!isBaseInStates) {
					addState = new State(base, unitPlus.getMethodPlus());
					addState.desetNormalValue();
					addState.setIsBaseState();
					for (State state2 : states) {
						if (state2.equalTo(addState)) {
							state2.setIsBaseState();
							addState = null;
						}
					}
				} else {
					for (State state2 : states) {
						if (equalTwoValues(state2.getVariable(), state.getBaseValue())) {
							if (state2.isBaseValue()) {
								state2.replaceValue(base, unitPlus);
							}
						}
					}
				}
			} else {
				deleteBase(state, states, removeStates);
			}
		} else {
			if (value instanceof InstanceFieldRef) {
				InstanceFieldRef instanceFieldRef = (InstanceFieldRef) value;
				Value base = instanceFieldRef.getBase();
				boolean isBaseInStates = false;
				for (State state2 : states) {
					if (state2.isBaseValue() && state2.equalValue(base, unitPlus.getMethodPlus())) {
						isBaseInStates = true;
					}
				}
				if (!isBaseInStates) {
					addState = new State(base, unitPlus.getMethodPlus());
					addState.desetNormalValue();
					addState.setIsBaseState();
					for (State state2 : states) {
						if (state2.equalTo(addState)) {
							state2.setIsBaseState();
							addState = null;
						}
					}
				}
				state.setHasBaseState(base);
			}
		}
		if (state.isBaseValue()) {
			for (State state2 : states) {
				if (state2.hasBaseValue()) {
					if (equalTwoValues(state2.getBaseValue(), state.getVariable())) {
						state2.updateFieldBase(value);
						state.replaceValue(value, unitPlus);
					}
				}
			}
		}
		return addState;
	}

	private static void deleteBase(State state, Set<State> states, Set<State> removeStates) {
		for (State state2 : states) {
			if (equalTwoValues(state2.getVariable(), state.getBaseValue())) {
				if (state2.isBaseValue()) {
					boolean baseForMultipleVariables = false;
					for (State state3 : states) {
						if (state3.hasBaseValue() && equalTwoValues(state2.getVariable(), state3.getBaseValue())
								&& !state3.equalTo(state)) {
							baseForMultipleVariables = true;
						}
					}
					if (!baseForMultipleVariables) {
						state2.deSetIsBaseState();
						if (state2.allValueFalse()) {
							deleteState(state2, states, removeStates);
						}
					}
				}
			}
		}
		state.deSetHasBaseState();
	}

	private static State replaceArrayState(State state, Value value, UnitPlus unitPlus, Set<State> states,
			Set<State> removeStates) {
		State addState = null;
		if (state.isArrayValue()) {
			if (value instanceof ArrayRef) {
				ArrayRef arrayRef = (ArrayRef) value;
				Value base = arrayRef.getBase();
				boolean isBaseInStates = false;
				for (State state2 : states) {
					if (state2.isArrayBaseValue() && state2.equalValue(base, unitPlus.getMethodPlus())) {
						isBaseInStates = true;
					}
				}
				if (!isBaseInStates) {
					addState = new State(arrayRef.getBase(), unitPlus.getMethodPlus());
					addState.desetNormalValue();
					addState.setArrayBaseValue();
					for (State state2 : states) {
						if (state2.equalTo(addState)) {
							state2.setArrayBaseValue();
							addState = null;
						}
					}
				}
			} else {
				deleteArrayBase(state, states, removeStates);
			}
		} else {
			if (value instanceof ArrayRef) {
				ArrayRef arrayRef = (ArrayRef) value;
				boolean isBaseInStates = false;
				for (State state2 : states) {
					if (state2.isArrayBaseValue() && state2.equalValue(arrayRef.getBase(), unitPlus.getMethodPlus())) {
						isBaseInStates = true;
					}
				}
				if (!isBaseInStates) {
					addState = new State(arrayRef.getBase(), unitPlus.getMethodPlus());
					addState.desetNormalValue();
					addState.setArrayBaseValue();
					for (State state2 : states) {
						if (state2.equalTo(addState)) {
							state2.setArrayBaseValue();
							addState = null;
						}
					}
				}
				state.setArrayValue();
			}
		}
		if (state.isArrayBaseValue()) {
			for (State state2 : states) {
				if (state2.isArrayValue()) {
					if (equalTwoValues(state2.getArrayBaseValue(), state.getVariable())) {
						state2.updateArrayBase(value);
					}
				}
			}
		}
		return addState;
	}

	private static void deleteArrayBase(State state, Set<State> states, Set<State> removeStates) {
		for (State state2 : states) {
			if (equalTwoValues(state2.getVariable(), state.getArrayBaseValue())) {
				if (state2.isArrayBaseValue()) {
					boolean baseForMultipleVariables = false;
					for (State state3 : states) {
						if (state3.isArrayValue() && equalTwoValues(state2.getVariable(), state3.getArrayBaseValue())
								&& !state3.equalTo(state)) {
							baseForMultipleVariables = true;
						}
					}
					if (!baseForMultipleVariables) {
						state2.desetArrayBaseValue();
						if (state2.allValueFalse()) {
							deleteState(state2, states, removeStates);
						}
					}
				}
			}
			state.desetArrayValue();
		}
	}

	public static void AddNPA(State state, UnitPlus unitPlus, Set<UnitPlus> NPA, Set<State> states,
			Set<State> removeStates) {
		if (state.isArrayValue()) {
			deleteArrayBase(state, states, removeStates);
		}

		if (state.hasBaseValue()) {
			deleteBase(state, states, removeStates);
		}
		if (!NPA.contains(unitPlus)&&!state.allValueFalse()) {
			NPA.add(unitPlus);
			System.out.println("Possible NPA: " + unitPlus);
		}
		removeStates.add(state);
	}
	
	public static void deleteState(State state, Set<State> states, Set<State> removeStates) {
		if (state.isArrayValue()) {
			deleteArrayBase(state, states, removeStates);
		}

		if (state.hasBaseValue()) {
			deleteBase(state, states, removeStates);
		}
		removeStates.add(state);
	}

	public static boolean equalTwoValues(Value value1, Value value2) {
		boolean equalValue = false;
		if (value1 != null && value2 != null) {
			if (value2 instanceof Expr) {
				if (value2.toString().equals(value1.toString())) {
					System.out.println("Error Expr: " + value2);
				}
				equalValue = false;
			} else {
				equalValue = ptaEqual(value1, value2);
				if(!equalValue){
					if (value2 instanceof Ref) {
						Ref ref2 = (Ref) value2;
						if (value1 instanceof Ref) {
							Ref ref1 = (Ref) value1;
							equalValue = equalRef(ref1, ref2);
						} else {
							equalValue = false;
						}
					}
				}
			}
		}
		return equalValue;
	}

	private static boolean equalRef(Ref ref1, Ref ref2) {
		boolean equalRef = false;
		if (ref2 instanceof ArrayRef) {
			if (ref1 instanceof ArrayRef) {
				ArrayRef arrayRef1 = (ArrayRef) ref1;
				ArrayRef arrayRef2 = (ArrayRef) ref2;
				equalRef = equalArrayValue(arrayRef1.getBase(), arrayRef2.getBase(), arrayRef1.getIndex(),
						arrayRef2.getIndex());
			} else {
				equalRef = false;
			}
		} else {
			equalRef = ref1.toString().equals(ref2.toString());
//			equalRef = ref1.equals(ref2);
			if (!equalRef) {
					if (ref2 instanceof InstanceFieldRef) {
						if (ref1 instanceof InstanceFieldRef) {
							InstanceFieldRef instanceFieldRef1 = (InstanceFieldRef) ref1;
							SootField valueField1 = instanceFieldRef1.getField();
							InstanceFieldRef instanceFieldRef2 = (InstanceFieldRef) ref2;
							SootField valueField2 = instanceFieldRef2.getField();
							if (valueField1.toString().equals(valueField2.toString())) {
								Value base1 = instanceFieldRef1.getBase();
								Value base2 = instanceFieldRef2.getBase();
								equalRef = equalTwoValues(base1, base2)
										|| equalDifferentClassesRef(base1, base2, valueField1, valueField2);
							} else {
								equalRef = false;
							}
						} else {
							equalRef = false;
						}
					}
				}
		}
		return equalRef;
	}
	
	
	private static boolean ptaEqual(Value value1, Value value2) {
		if(value1.equals(value2)){
			return true;
		}else {
			boolean ptaEqual = false;
			try {
				if (value1 instanceof Local) {
					Local local1 = (Local) value1;
					if (value2 instanceof Local) {
						Local local2 = (Local) value2;
						ptaEqual = pta.reachingObjects(local1).hasNonEmptyIntersection(pta.reachingObjects(local2));
					}else if(value2 instanceof FieldRef){
						FieldRef fieldRef2 = (FieldRef) value2;
						if(fieldRef2 instanceof InstanceFieldRef){
							InstanceFieldRef instanceFieldRef2 = (InstanceFieldRef) fieldRef2;
							ptaEqual = pta.reachingObjects(local1).hasNonEmptyIntersection(pta.reachingObjects((Local)instanceFieldRef2.getBase(), instanceFieldRef2.getField()));
						}else {
							ptaEqual = pta.reachingObjects(local1).hasNonEmptyIntersection(pta.reachingObjects(fieldRef2.getField()));
						}
					}
				}else if(value1 instanceof FieldRef){
					FieldRef fieldRef1 = (FieldRef) value1;
					if (fieldRef1 instanceof InstanceFieldRef) {
						InstanceFieldRef instanceFieldRef1 = (InstanceFieldRef) fieldRef1;
						if (value2 instanceof Local) {
							Local local2 = (Local) value2;
							ptaEqual = pta.reachingObjects((Local)instanceFieldRef1.getBase(), instanceFieldRef1.getField()).hasNonEmptyIntersection(pta.reachingObjects(local2));
						}else if (value2 instanceof FieldRef) {
							FieldRef FieldRef2 = (FieldRef) value2;
							if (FieldRef2 instanceof InstanceFieldRef) {
								InstanceFieldRef instanceFieldRef2 = (InstanceFieldRef) FieldRef2;
								ptaEqual = pta.reachingObjects((Local)instanceFieldRef1.getBase(), instanceFieldRef1.getField()).hasNonEmptyIntersection(pta.reachingObjects((Local)instanceFieldRef2.getBase(), instanceFieldRef2.getField()));
							}else {
								ptaEqual = pta.reachingObjects((Local)instanceFieldRef1.getBase(), instanceFieldRef1.getField()).hasNonEmptyIntersection(pta.reachingObjects(FieldRef2.getField()));
							}
						}
					}else {
						if (value2 instanceof Local) {
							Local local2 = (Local) value2;
							ptaEqual = pta.reachingObjects(fieldRef1.getField()).hasNonEmptyIntersection(pta.reachingObjects(local2));
						}else if (value2 instanceof FieldRef) {
							FieldRef FieldRef2 = (FieldRef) value2;
							ptaEqual = pta.reachingObjects(fieldRef1.getField()).hasNonEmptyIntersection(pta.reachingObjects(FieldRef2.getField()));
						}
					}
				}
			} catch (Exception e) {
				ptaEqual= false;
			}
			return ptaEqual;
		}
	}

	private static boolean equalDifferentClassesRef(Value base1, Value base2, SootField valueField1,
			SootField valueField2) {
		boolean equalRef = false;
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
				} 
			} 
		} 
		return equalRef;
	}

	private static boolean equalArrayValue(Value base1, Value base2, Value index1, Value index2) {
		;
		return equalTwoValues(base1, base2) && index1.toString().equals(index2.toString());
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
		for (State state : originalStates) {
			newStates.add(new State(state));
		}
		return newStates;
	}

	public static boolean isTransform(UnitPlus unitPlus) {
		boolean isTransform = false;
		if (unitPlus.getUnit() instanceof AbstractDefinitionStmt) {
			isTransform = true;
			// Regardless of whether right value is invoke expression, as long as it is not call
			// the unit is a transform
			if (unitPlus.isCall()) {
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

	public static boolean unitPlusSetContains(Set<UnitPlus> units, UnitPlus pred) {
		boolean setContains = false;
		for (UnitPlus unitPlus : units) {
			if (unitPlus.equalTo(pred)) {
				setContains = true;
				break;
			}
		}
		return setContains;
	}

	public static boolean stateSetContains(Set<State> states, State predState) {
		boolean setContains = false;
		for (State state : states) {
			if (state.equalTo(predState)) {
				setContains = true;
				break;
			}
		}
		return setContains;
	}

	public static void addAll(Set<State> statesToBeAdded, Set<State> statesToAdd) {
		for (State state : statesToAdd) {
			boolean contains = false;
			for (State state2 : statesToBeAdded) {
				if (state.equalTo(state2)) {
					contains = true;
					state.mergeState(state2);
				}
			}
			if (!contains) {
				statesToBeAdded.add(state);
			}
		}
	}
	
}
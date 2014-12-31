package internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Immediate;
import soot.RefType;
import soot.Value;
import soot.jimple.CastExpr;
import soot.jimple.Constant;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.NullConstant;
import soot.jimple.Ref;
import soot.jimple.internal.AbstractDefinitionStmt;

/**
 * element=UnitPlus connected with state
 */
/**
 * @version 2014-08-06
 * @author Lind, Yingqi
 *
 */
public class Element
{
	private UnitPlus unitPlus;
	private Set<State> states;
//	private boolean isVisited;
	
	/**
	 * constructor for multiple states
	 * @param unitPlus
	 * @param states
	 */
	public Element(UnitPlus unitPlus, Set<State> states)
	{
		this.unitPlus=unitPlus;
		this.states = new HashSet<>();
		for(State state:states){
			this.states.add(new State(state.getValue(), state.getmethod(), state.getAttribute(), state.getReturnInMethodPlus()));
		}
//		isVisited =false;
	}
	

	/**
	 * get the unitplus
	 * @return
	 */
	public UnitPlus getUnitPlus()
	{
		return unitPlus;
	}
	
	/**
	 * get the states
	 * @return
	 */
	public Set<State> getStates()
	{
		return states;
	}
	
	/**
	 * transform a definition statement
	 * @return
	 */
	public void transform(Set<UnitPlus> NPA, Set<UnitPlus> PossibleNPAs)
	{
		if(unitPlus.getUnit() instanceof AbstractDefinitionStmt){
			AbstractDefinitionStmt ads  = (AbstractDefinitionStmt) unitPlus.getUnit();
			Value leftValue = ads.getLeftOp();
			Value rightOp = ads.getRightOp();
			Set<State> addStates = new HashSet<>();
			Set<State> removeStates = new HashSet<>();
//			State tempState = null;
//			State transformedState = null;
			for(State state: states){
				if(state.equalValue(leftValue, unitPlus.getMethodPlus())) {
//					System.out.println(states);
					if(rightOp instanceof NullConstant){
						if(!NPA.contains(unitPlus)){
							NPA.add(unitPlus);
							System.out.println("NullAssignNPA:  " + unitPlus);
						}
						removeStates.add(state);
					}else {
						if(rightOp instanceof Constant){
//							PossibleNPAs.add(unitPlus);
							removeStates.add(state);
						}else if((rightOp instanceof Ref) ||(rightOp instanceof Immediate)){
							stateReplace(state, rightOp, unitPlus, removeStates);
						}else if(rightOp instanceof InvokeExpr){
							// for those invokes which methods are not analyzed
							InvokeExpr InvokeExpr = (InvokeExpr) rightOp;
							if(!InvokeExpr.getMethod().isJavaLibraryMethod()){
								if(!PossibleNPAs.contains(unitPlus)){
									PossibleNPAs.add(unitPlus);
									System.out.println("PossibleNPA:  " + unitPlus);
								}
							}
//							removeStates.add(state);
//							addStates(addStates, states, instanceInvokeExpr.getBase(), unitPlus.getMethodPlus());
						}else if(rightOp instanceof InstanceFieldRef){
//							InstanceFieldRef instanceFieldRef = (InstanceFieldRef) rightOp;
							stateReplace(state, rightOp, unitPlus, removeStates);
//							addStates(addStates, states, instanceFieldRef.getBase(), unitPlus.getMethodPlus());
						}else if((rightOp instanceof NewExpr)){
							NewExpr newExpr = (NewExpr) rightOp;
							removeStates.add(state);
							if(!newExpr.getBaseType().getSootClass().isJavaLibraryClass()){
								if(!PossibleNPAs.contains(unitPlus)){
									PossibleNPAs.add(unitPlus);
									System.out.println("PossibleNPA:  " + unitPlus);
								}
							}
						}else if((rightOp instanceof NewArrayExpr)||(rightOp instanceof NewMultiArrayExpr)){
								removeStates.add(state);
						}else if(rightOp instanceof CastExpr){
							CastExpr castExpr = (CastExpr) rightOp;
							stateReplace(state, castExpr.getOp(), unitPlus, removeStates);
						}else {
							System.out.println("Error: Expr in Trasform Statement "+rightOp);
						}
					}
				}else{
					if(rightOp instanceof NullConstant){
						//for increase in alias analysis
//						possibleStates.put(new State(leftValue, unitPlus.getMethodPlus()), unitPlus);
					}
				}
			}
			
			for(State state:addStates){
				states.add(state);
			}
			for(State state:removeStates){
				states.remove(state);
			}
		}
	}
	
	private void stateReplace(State state, Value value, UnitPlus unitPlus, Set<State> removeStates){
//		Set<State> newStates = new HashSet<>();
//		if (state.replaceValue(value, unitPlus.getMethodPlus())==null) {
//			removeStates.add(state);
//		}else {
//			newStates.add(new State(value, methodPlus));
//			for(State state2:states){
//				if(!state2.equalTo(state)){
//					newStates.add(state2);
//				}
//			}
//		}
//		states = newStates;
		state.replaceValue(value, unitPlus);
	}
	
//	private void addStates(Set<State> addStates, Set<State> states, Value base, MethodPlus methodPlus){
//		boolean baseAnalyzed = false;
//		for(State state:states){
//			if(state.equalValue(base, methodPlus)){
//				baseAnalyzed = true;
//			}
//		}
//		if(!baseAnalyzed){
//			addStates.add(new State(base, methodPlus));
//		}
//	}
	
	@Override
	public String toString(){
		String toString ="UnitPlus: "+unitPlus.getNumber()+unitPlus.getAttribute()+'\t'+unitPlus.getMethodPlus().getclassName()+"."+unitPlus.getMethodPlus().getMethodName()+" "+unitPlus.getUnit()+'\t'+"State: "+states.size()+" ";
		
		for(State state:states){
			toString+=state.getValue().toString()+" "+state.getAttribute()+" ";
		}
		return toString;
	}
	

	@Override
	public boolean equals(Object object){
		if(! (object instanceof Element)){
			return false;
		}else {
			Element element = (Element) object;
			boolean equals = this.getStates().size()==element.getStates().size()&&this.getUnitPlus().equals(element.getUnitPlus());
			if(equals){
				for(State state:states){
					if(!element.getStates().contains(state)){
						equals = false;
						break;
					}
				}
				if(equals){
					for(State state:element.getStates()){
						if(!states.contains(state)){
							equals = false;
							break;
						}
					}
				}
			}
			return equals;
		}
	}


	public void setUnitPlus(UnitPlus unitPlus) {
		this.unitPlus = unitPlus;
	}


	public void setStates(Set<State> states) {
		this.states = states;
	}

}
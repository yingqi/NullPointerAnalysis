package internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dispatcher.LightDispatcher;
import soot.Immediate;
import soot.RefType;
import soot.Value;
import soot.jimple.ArrayRef;
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
			this.states.add(new State(state));
		}
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
			for(State state: states){
				if(state.equalValue(leftValue, unitPlus.getMethodPlus())) {
					if(rightOp instanceof NullConstant){
						LightDispatcher.AddNPA(state, unitPlus, NPA, states, removeStates);
					}else {
						if(rightOp instanceof Constant){
							removeStates.add(state);
						}else if((rightOp instanceof Ref) ||(rightOp instanceof Immediate)){
							LightDispatcher.stateReplace(state, rightOp, unitPlus, states, removeStates, addStates);
						}else if(rightOp instanceof InvokeExpr){
							// for those invokes which methods are not analyzed
							InvokeExpr InvokeExpr = (InvokeExpr) rightOp;
							if(!InvokeExpr.getMethod().isJavaLibraryMethod()){
								LightDispatcher.AddNPA(state, unitPlus, PossibleNPAs, states, removeStates);
							}else {
								removeStates.add(state);
							}
//							addStates(addStates, states, instanceInvokeExpr.getBase(), unitPlus.getMethodPlus());
						}else if(rightOp instanceof InstanceFieldRef){
//							InstanceFieldRef instanceFieldRef = (InstanceFieldRef) rightOp;
							LightDispatcher.stateReplace(state, rightOp, unitPlus, states, removeStates, addStates);
//							addStates(addStates, states, instanceFieldRef.getBase(), unitPlus.getMethodPlus());
						}else if((rightOp instanceof NewExpr)){
							NewExpr newExpr = (NewExpr) rightOp;
							if(!newExpr.getBaseType().getSootClass().isJavaLibraryClass()){
								LightDispatcher.AddNPA(state, unitPlus, PossibleNPAs, states, removeStates);
							}else {
								removeStates.add(state);
							}
						}else if((rightOp instanceof NewArrayExpr)||(rightOp instanceof NewMultiArrayExpr)){
							LightDispatcher.AddNPA(state, unitPlus, PossibleNPAs, states, removeStates);
						}else if(rightOp instanceof CastExpr){
							CastExpr castExpr = (CastExpr) rightOp;
							if(castExpr.getOp() instanceof NullConstant){
								LightDispatcher.AddNPA(state, unitPlus, NPA, states, removeStates);
							}else {
								LightDispatcher.stateReplace(state, castExpr.getOp(), unitPlus, states, removeStates, addStates);
							}
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
	
	
	@Override
	public String toString(){
		String toString ="UnitPlus: "+unitPlus.getNumber()+unitPlus.getAttribute()+'\t'+unitPlus.getMethodPlus().getclassName()+"."+unitPlus.getMethodPlus().getMethodName()+" "+unitPlus.getUnit()+'\t'+"State: "+states.size()+" ";
		
		for(State state:states){
			toString+=state.getVariable().toString()+" "+" ";
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
	
	public boolean equalTo(Object object){
		boolean equalTo = false;
		if(! (object instanceof Element)){
			equalTo = false;
		}else {
			Element element = (Element) object;
			equalTo = states.size()==element.getStates().size()
					&&unitPlus.equals(element.getUnitPlus());
			if(equalTo){
				for(State state:states){
					boolean isStateInElement = false;
					for(State stateInElement:element.getStates()){
						if(state.equalTo(stateInElement)){
							isStateInElement = true;
							break;
						}
					}
					if(!isStateInElement){
						equalTo = false;
						break;
					}
				}
				if(equalTo){
					for(State state:element.getStates()){
						boolean isStateInValue = false;
						for(State stateInValue:states){
							if(state.equalTo(stateInValue)){
								isStateInValue = true;
								break;
							}
						}
						if(!isStateInValue){
							equalTo = false;
							break;
						}
					}
				}
			}
		}
		return equalTo;
	}


	public void setUnitPlus(UnitPlus unitPlus) {
		this.unitPlus = unitPlus;
	}


	public void setStates(Set<State> states) {
		this.states = states;
	}

}
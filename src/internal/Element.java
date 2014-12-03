package internal;

import java.util.ArrayList;
import java.util.List;

import java_cup.reduce_action;
import soot.Value;
import soot.ValueBox;
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
	private List<State> states;
	private boolean isVisited;
	
//	public Element(UnitPlus unitPlus, State state)
//	{
//		this.unitPlus=unitPlus;
//		states = new ArrayList<>();
//		states.add(state);
//		isVisited =false;
//	}
	
	/**
	 * constructor for multiple states
	 * @param unitPlus
	 * @param states
	 */
	public Element(UnitPlus unitPlus, List<State> states)
	{
		this.unitPlus=unitPlus;
		this.states = states;
		isVisited =false;
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
	public List<State> getStates()
	{
		return states;
	}

	/**
	 * return whether the element is visited
	 * @return
	 */
	public boolean isVisited()
	{
		return isVisited;
	}
	
	/**
	 * set the element as visited
	 */
	public void setVisited()
	{
		isVisited =true;
	}
	
	/**
	 * transform a definition statement
	 * @return
	 */
	public void transform()
	{	
		List<State> newStates =new ArrayList<>();
		if(unitPlus.getUnit() instanceof AbstractDefinitionStmt){
			AbstractDefinitionStmt ads  = (AbstractDefinitionStmt) unitPlus.getUnit();
			Value leftValue = ads.getLeftOp();
			for(State state:states){
				if(leftValue.toString().equals(state.getValue().toString())){
					// Q2: how to euqal these two better than toString?
					State newState = new State(ads.getRightOp());
					newStates.add(newState);
				}else{
					newStates.add(state);
				}
			}
		}
		states = newStates;
	}
	
//	public Element transform()
//	{	
//		List<State> newStates =new ArrayList<>();
//		Element newElement = new Element(unitPlus, newStates);
//		if(unitPlus.getUnit() instanceof AbstractDefinitionStmt){
//			AbstractDefinitionStmt ads  = (AbstractDefinitionStmt) unitPlus.getUnit();
//			Value leftValue = ads.getLeftOp();
//			for(State state:states){
//				if(leftValue.toString().equals(state.getValue().toString())){
//					// Q2: how to euqal these two better than toString?
//					State newState = new State(ads.getRightOp());
//					newStates.add(newState);
//				}else{
//					newStates.add(state);
//				}
//			}
//		}
//		return newElement;
//	}
	
	/**
	 * to see whether an element is a predicate
	 * @return
	 */
	public boolean isPredicate()
	{
		boolean isPredicate = false;
		AbstractDefinitionStmt ads  = (AbstractDefinitionStmt) unitPlus.getUnit();
		Value leftValue = ads.getLeftOp();
		for(State state:states){
			if(leftValue.toString().equals(state.getValue().toString())&&ads.getRightOp().toString().equals("null")){
				// same as Q2
				isPredicate = true;
			}
		}
		return isPredicate;
	}
	
	@Override
	public String toString(){
		String toString = "UnitPlus: "+unitPlus+'\t'+"State: ";
		for(State state:states){
			toString+=state.toString()+" ";
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
				int i = 0;
				for(State state:states){
					if(state.equals(element.getStates().get(i))){
						equals = false;
						break;
					}
					i++;
				}
			}
			return equals;
		}
	}


	public void setUnitPlus(UnitPlus unitPlus) {
		this.unitPlus = unitPlus;
	}


	public void setStates(List<State> states) {
		this.states = states;
	}

}
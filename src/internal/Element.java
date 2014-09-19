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
 * @author Lind
 *
 */
public class Element
{
	private UnitPlus unitPlus;
	private List<State> states;
	private boolean isVisited;
	
	public Element(UnitPlus unitPlus, State state)
	{
		this.unitPlus=unitPlus;
		states = new ArrayList<>();
		states.add(state);
		isVisited =false;
	}
	public Element(UnitPlus unitPlus, List<State> states)
	{
		this.unitPlus=unitPlus;
		this.states = states;
		isVisited =false;
	}
	public void setUnitPlus(UnitPlus unitPlus){
		this.unitPlus = unitPlus;
	}
	public UnitPlus getUnitPlus()
	{
		return unitPlus;
	}
	public List<State> getStates()
	{
		return states;
	}
	public void setStates(List<State> states){
		this.states = states;
	}
	public boolean isVisited()//TO BE revised
	{
		return isVisited;
	}
	public void setVisited()
	{
		isVisited =true;
	}
	public Element transform()
	{	
		List<State> newStates =new ArrayList<>();
		Element newElement = new Element(unitPlus, newStates);
		AbstractDefinitionStmt ads  = (AbstractDefinitionStmt) unitPlus.getUnit();
		Value leftValue = ads.getLeftOp();
//		System.out.print("State: "+state.getValue()+"       Stmt: ");
//		String methodString = String.format("%-30s", unitPlus.getMethodPlus().toString());
//		System.out.println("unit" + '\t' + unitPlus.getNumber() + '\t'
//				+ methodString 
//				+ unitPlus.getUnit().toString());
//		System.out.print("Rightvalue: "+ads.getRightOp()+"\t\t\t");
//		System.out.println("Leftvalue: "+ads.getLeftOp());
		for(State state:states){
			if(leftValue.toString().equals(state.getValue().toString())){
//				state.replaceValue(ads.getRightOp());
				State newState = new State(ads.getRightOp());
				newStates.add(newState);
			}else{
				newStates.add(state);
			}
		}
//		System.out.println("After: "+state.getValue());
		return newElement;
	}
	public boolean isPredicate()
	{
		boolean isPredicate = false;
		AbstractDefinitionStmt ads  = (AbstractDefinitionStmt) unitPlus.getUnit();
		Value leftValue = ads.getLeftOp();
		for(State state:states){
			if(leftValue.toString().equals(state.getValue().toString())&&ads.getRightOp().toString().equals("null")){
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

}
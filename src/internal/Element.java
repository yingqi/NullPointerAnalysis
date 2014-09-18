package internal;

import java.util.List;

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
	private State state;
	private boolean isVisited;
	
	public Element(UnitPlus unitPlus,State state)
	{
		this.unitPlus=unitPlus;
		this.state=state;
		isVisited =false;
	}
	public void setUnitPlus(UnitPlus unitPlus){
		this.unitPlus = unitPlus;
	}
	public UnitPlus getUnitPlus()
	{
		return unitPlus;
	}
	public State getState()
	{
		return state;
	}
	public boolean isVisited()//TO BE revised
	{
		return isVisited;
	}
	public void setVisited()
	{
		isVisited =true;
	}
	public void transform()
	{
		AbstractDefinitionStmt ads  = (AbstractDefinitionStmt) unitPlus.getUnit();
		Value leftValue = ads.getLeftOp();
		System.out.print("State: "+state.getValue()+"       Stmt: ");
		String methodString = String.format("%-30s", unitPlus.getMethodPlus().toString());
		System.out.println("unit" + '\t' + unitPlus.getNumber() + '\t'
				+ methodString 
				+ unitPlus.getUnit().toString());
//		System.out.print("Rightvalue: "+ads.getRightOp()+"\t\t\t");
//		System.out.println("Leftvalue: "+ads.getLeftOp());
		if(leftValue.toString().equals(state.getValue().toString())){
			state.replaceValue(ads.getRightOp());
		}
//		System.out.println("After: "+state.getValue());
	}
	public boolean isPredicate()
	{
		boolean isPredicate = false;
		AbstractDefinitionStmt ads  = (AbstractDefinitionStmt) unitPlus.getUnit();
		Value leftValue = ads.getLeftOp();
		if(leftValue.toString().equals(state.getValue().toString())&&ads.getRightOp().toString().equals("null")){
			isPredicate = true;
		}
		
		return isPredicate;
	}
	
	@Override
	public String toString(){
		return "UnitPlus: "+unitPlus+'\t'+"State: "+state;
	}
	
	@Override
	public boolean equals(Object object){
		if(! (object instanceof Element)){
			return false;
		}else {
			Element element = (Element) object;
			return this.getState().equals(element.getState())&&this.getUnitPlus().equals(element.getUnitPlus());
		}
	}

}
package internal;

import soot.Value;
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
	public Element(UnitPlus unitPlus,State state)
	{
		this.unitPlus=unitPlus;
		this.state=state;
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
		return unitPlus.isVisited();
	}
	public void setVisited()
	{
		unitPlus.setVisited(true);
	}
	public void transform()
	{
		boolean containsValueInState = false;
		AbstractDefinitionStmt ads  = (AbstractDefinitionStmt) unitPlus.getUnit();
		Value rightValue = ads.getRightOp();
		if(rightValue.equals(state.getValue())){
			state.replaceValue(ads.getLeftOp());
		}
	}
	public boolean isPredicate()
	{
		return unitPlus.isPredicate();
	}
	private UnitPlus unitPlus;
	private State state;
}
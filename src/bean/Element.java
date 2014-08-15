package bean;

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
	public boolean isVisited()
	{
		return unitPlus.isVisited();
	}
	public void setVisited()
	{
		//TO DO 
		unitPlus.setVisited(true);
	}
	public void transform()
	{
		//TO DO		
	}
	public boolean isPredicate()
	{
		return unitPlus.isPredicate();
	}
	private UnitPlus unitPlus;
	private State state;
}
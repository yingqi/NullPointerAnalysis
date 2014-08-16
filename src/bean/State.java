package bean;


/**
 * This class is the state of a node
 */
/**
 * @version 2014-08-06
 * @author Lind
 *
 */
public class State
{
	public State(String variable,String thisStatement)
	{
		this.variable=variable;
		this.thisStatement=thisStatement;
	}
//	public State map(UnitPlus s)
//	{
//		//TO DO
//		return null;
//	}
	private String variable;//root predicate:<variable=null>
	private String thisStatement;//<thisStatement!=null>
	public static final State EMPTY= new State(null,null);
}
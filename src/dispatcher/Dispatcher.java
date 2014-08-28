package dispatcher;

import java.util.List;






import java_cup.internal_error;
import bean.MethodPlus;
import bean.UnitPlus;
import soot.Value;

/**
 * 
 * @author Yingqi
 *
 */
public interface Dispatcher {
	/**
	 * gets the predecessors of a unit in a complete control flow graph with all
	 * methods
	 * 
	 * @param unit
	 * @return
	 */
	public List<UnitPlus> getPredecessors(UnitPlus unitPlus);

	/**
	 * gets the call site based on the stack trace
	 * @param unit
	 * @param stackTrace
	 * @return
	 * @throws ClassNotFoundException 
	 */
	public UnitPlus getStackTraceCallSite(UnitPlus unitPlus,
			StackTraceElement[] stackTrace, int indexOfStackTrace) throws ClassNotFoundException;

	/**
	 * gets all the call sites
	 * 
	 * @param unit
	 * @return
	 */
	public List<UnitPlus> getAllCallSites(UnitPlus unitPlus);

	/**
	 * gets the exit units of a method
	 * 
	 * @param methodName
	 * @return
	 */
	public List<UnitPlus> getExitUnitPlus(MethodPlus rteMethod);

	/**
	 * get method name where the unit belongs
	 * 
	 * @return
	 */
	public MethodPlus getMethod(UnitPlus unitPlus);

	/**
	 * return true if the unit is a unit in other methods that call this method.
	 * As there is no entry in the unit graph, so we have to use call sites(the
	 * first call sites) to represent entries i.e. 24a, 10a.
	 * 
	 * @param unit
	 * @return
	 */
	public boolean isEntry(UnitPlus unitPlus);

	/**
	 * return true if the unit is a unit that call other methods. We have to use
	 * call sites(the second call sites) to represent entries i.e. 24b, 10b.
	 * 
	 * @param unit
	 * @return
	 */
	public boolean isCall(UnitPlus unitPlus);

	/**
	 * map the definition value to the use value. return the last use value.
	 * 
	 * @param defValue
	 * @return
	 */
	public Value valueMap(Value defValue, UnitPlus unitPlus);

}

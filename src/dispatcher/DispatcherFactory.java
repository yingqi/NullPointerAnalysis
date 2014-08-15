package dispatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bean.MethodPlus;
import bean.UnitPlus;

import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.toolkits.graph.UnitGraph;

/**
 * class which implements the interface Dispathcer.
 * Warning the 
 * 
 * @author Yingqi
 *
 */
public class DispatcherFactory implements Dispatcher {
	private Map<UnitPlus, List<UnitPlus>> completeCFG;
	private StackTraceElement[] stackTrace;
	private int indexOfStackTrace;
	private Map<MethodPlus, UnitGraph> methodToUnitGraph;
	
	public DispatcherFactory(Map<UnitPlus, List<UnitPlus>> completeCFG,StackTraceElement[] stackTrace, Map<MethodPlus, UnitGraph> methodToUnitGraph){
		this.completeCFG = completeCFG;
		this.stackTrace = stackTrace;
		indexOfStackTrace = 0;
		this.methodToUnitGraph = methodToUnitGraph;
	}

	@Override
	public List<UnitPlus> getPredecessors(UnitPlus unitPlus) {
		List<UnitPlus> preds = null;
		Set<UnitPlus> keys = completeCFG.keySet();
		for(UnitPlus key:keys){
			if (key.getAttribute().equals(unitPlus.getAttribute())&&key.getNumber()==unitPlus.getNumber()) {
				preds = completeCFG.get(key);
			}
		}
		return preds;
	}

	@Override
	public UnitPlus getStackTraceCallSite(UnitPlus unitPlus,
			StackTraceElement stackTraceElement) {
		String methodName = stackTraceElement.getMethodName();
		
		return null;
	}

	@Override
	public List<UnitPlus> getAllCallSites(UnitPlus unitPlus) {
		return this.getPredecessors(unitPlus);
	}

	@Override
	public List<UnitPlus> getExitUnitPlus(MethodPlus Method) {
		List<Unit> tailUnits = new ArrayList<>();
		List<UnitPlus> tailUnitPlus = new ArrayList<>();
		tailUnits = methodToUnitGraph.get(Method).getTails();
		Set<UnitPlus> keys = completeCFG.keySet();
		for(UnitPlus key:keys){
			for(Unit tailUnit:tailUnits){
				if(key.getUnit().equals(tailUnit))
					tailUnitPlus.add(key);
			}
		}
		return tailUnitPlus;
	}

	@Override
	public MethodPlus getMethod(UnitPlus unitPlus) {
		return unitPlus.getMethod();
	}

	@Override
	public boolean isEntry(UnitPlus unitPlus) {
		return unitPlus.getAttribute().equals("a");
	}

	@Override
	public boolean isCall(UnitPlus unitPlus) {
		return unitPlus.getAttribute().equals("b");
	}

	@Override
	public Value valueMap(Value defValue, UnitPlus unitPlus) {
		List<ValueBox> useValueBoxs = unitPlus.getUnit().getUseBoxes();
		ValueBox useValueBox = useValueBoxs.get(useValueBoxs.size()-1);
		return useValueBox.getValue();
	}

}

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
import soot.toolkits.graph.UnitGraphPlus;

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
	private Map<MethodPlus, UnitGraphPlus> methodToUnitGraph;
	
	public DispatcherFactory(Map<UnitPlus, List<UnitPlus>> completeCFG,StackTraceElement[] stackTrace, Map<MethodPlus, UnitGraphPlus> methodToUnitGraph){
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
			StackTraceElement[] stackTrace, int indexOfStackTrace) throws ClassNotFoundException {
		UnitPlus callSite = null;
		StackTraceElement stackTraceElement = stackTrace[indexOfStackTrace];
		String methodName = stackTraceElement.getMethodName();
		String className = stackTraceElement.getClassName();
		String fileName = stackTraceElement.getFileName();
		int lineNumber = stackTraceElement.getLineNumber();
		List<UnitPlus> callSites = this.getPredecessors(unitPlus);
		for(UnitPlus pred:callSites){
			if(pred.getMethodPlus().getMethodName().equals(methodName)){
				if(util.FileParser.isLineInMethod(fileName, className, lineNumber, pred.getMethodPlus())){
					callSite = pred;
				}
			}
		}
		return callSite;
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
		return unitPlus.getMethodPlus();
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

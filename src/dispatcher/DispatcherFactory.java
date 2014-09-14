package dispatcher;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bean.MethodPlus;
import bean.UnitPlus;
import soot.Body;
import soot.PatchingChain;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.dava.toolkits.base.AST.structuredAnalysis.StructuredAnalysis;
import soot.jimple.internal.AbstractDefinitionStmt;
import soot.tagkit.LineNumberTag;
import soot.tagkit.Tag;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.graph.UnitGraphPlus;
import soot.util.Chain;

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
	boolean isDistinguishedOverload;
	
	public DispatcherFactory(Map<UnitPlus, List<UnitPlus>> completeCFG,StackTraceElement[] stackTrace, Map<MethodPlus, UnitGraphPlus> methodToUnitGraph){
		this.completeCFG = completeCFG;
		this.stackTrace = stackTrace;
		indexOfStackTrace = 0;
		this.methodToUnitGraph = methodToUnitGraph;
		isDistinguishedOverload=false;
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
			StackTraceElement[] stackTrace, int indexOfStackTrace) throws ClassNotFoundException, FileNotFoundException {
		UnitPlus callSite = null;
		StackTraceElement stackTraceElement = stackTrace[indexOfStackTrace];
		String methodName = stackTraceElement.getMethodName();
		String className = stackTraceElement.getClassName();
		String fileName = stackTraceElement.getFileName();
		int lineNumber = stackTraceElement.getLineNumber();
		List<UnitPlus> preds = this.getPredecessors(unitPlus);
		for(UnitPlus pred:preds){
			if(isDistinguishedOverload){
				if(pred.getMethodPlus().getMethodName().equals(methodName)){
					//The isLineInMethod has not been down.
					if(util.FileParser.isLineInMethod(fileName, className, lineNumber, pred.getMethodPlus())){
						callSite = pred;
					}
				}
			}
		}
		return callSite;
	}
	
	@Override
	public List<UnitPlus> getStackTraceCallSites(UnitPlus unitPlus,
			StackTraceElement[] stackTrace, int indexOfStackTrace)
			throws ClassNotFoundException, FileNotFoundException {
		List<UnitPlus> sTCallSites = new ArrayList<>();
		StackTraceElement stackTraceElement = stackTrace[indexOfStackTrace];
		String methodName = stackTraceElement.getMethodName();
		List<UnitPlus> callSites = this.getPredecessors(unitPlus);
		for(UnitPlus pred:callSites){
			if(pred.getMethodPlus().getMethodName().equals(methodName)){
				sTCallSites.add(pred);
			}
		}
		return sTCallSites;
	}

	@Override
	public List<UnitPlus> getAllCallSites(UnitPlus unitPlus) {
		return this.getPredecessors(unitPlus);
	}

	@Override
	public UnitPlus getExitUnitPlus(MethodPlus Method) {
		List<Unit> tailUnits = new ArrayList<>();
		UnitPlus tailUnitPlus = null;
		tailUnits = methodToUnitGraph.get(Method).getTails();
		Unit tailUnit = tailUnits.get(0);
		Set<UnitPlus> keys = completeCFG.keySet();
		for(UnitPlus key:keys){
				if(key.getUnit().equals(tailUnit))
					tailUnitPlus=key;
		}
		return tailUnitPlus;
	}
	
	@Override
	public UnitPlus getEntryUnitPlus(MethodPlus Method) {
		List<Unit> headUnits = new ArrayList<>();
		UnitPlus headUnitPlus = null;
		headUnits = methodToUnitGraph.get(Method).getTails();
		Unit headUnit = headUnits.get(0);
		Set<UnitPlus> keys = completeCFG.keySet();
		for(UnitPlus key:keys){
				if(key.getUnit().equals(headUnit))
					headUnitPlus=key;
		}
		return headUnitPlus;
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

	@Override
	public List<Unit> StackTraceElementToUnit(
			StackTraceElement[] stackTrace, int indexOfStackTrace) {
		List<Unit> units = new ArrayList<>();
		StackTraceElement ste = stackTrace[indexOfStackTrace];
		String className = ste.getClassName();
		String methodName = ste.getMethodName();
		int lineNumber = ste.getLineNumber();
		SootClass sootClass = Scene.v().loadClassAndSupport(className);
		List<SootMethod> sootMethods = sootClass.getMethods();
//		List<SootMethod> matchedMehods = new ArrayList<>();
		for(SootMethod sootMethod:sootMethods){
			if(sootMethod.getName().equals(methodName)){
//				matchedMehods.add(sootMethod);
				units.addAll(methodInUnit(sootMethod,lineNumber));
			}
		}
		return units;
	}

	private List<Unit> methodInUnit(SootMethod sootMethod, int lineNumber){
		List<Unit> units = new ArrayList<>();
		Body body = sootMethod.retrieveActiveBody();
		PatchingChain<Unit> unitPatchingChain = body.getUnits();
		for(Unit unit:unitPatchingChain){
			List<Tag> tags = unit.getTags();
			for(Tag tag:tags){
				if(tag instanceof LineNumberTag){
					LineNumberTag lineNumberTag = (LineNumberTag) tag;
					if(lineNumber==lineNumberTag.getLineNumber()){
						units.add(unit);
					}
				}
			}
		}
		return units;
	}

	@Override
	public boolean isTransform(UnitPlus unitPlus) {
		boolean isTransform = false;
		if(unitPlus.getUnit() instanceof AbstractDefinitionStmt){
			isTransform = true;
		}
		return isTransform;
	}

	@Override
	public Map<MethodPlus, UnitGraphPlus> getMethodToUnitGraphPlus() {
		return methodToUnitGraph;
	}




}

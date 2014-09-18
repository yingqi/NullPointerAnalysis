package dispatcher;

import internal.MethodPlus;
import internal.UnitPlus;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.PatchingChain;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.internal.AbstractDefinitionStmt;
import soot.jimple.internal.BeginStmt;
import soot.jimple.internal.JInvokeStmt;
import soot.tagkit.LineNumberTag;
import soot.tagkit.Tag;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.graph.UnitGraphPlus;
import soot.util.Chain;

/**
 * class which implements the interface Dispathcer. Warning the
 * 
 * @author Yingqi
 *
 */
public class DispatcherFactory implements Dispatcher {
	private Map<UnitPlus, List<UnitPlus>> completeCFG;
	private Map<MethodPlus, UnitGraphPlus> methodToUnitGraph;
	private boolean isDistinguishedOverload;
	private List<SootMethod> sootMethods;

	public DispatcherFactory(Map<UnitPlus, List<UnitPlus>> completeCFG,
			StackTraceElement[] stackTrace,
			Map<MethodPlus, UnitGraphPlus> methodToUnitGraph,
			List<SootMethod> sootMethods) {
		this.completeCFG = completeCFG;
		this.methodToUnitGraph = methodToUnitGraph;
		isDistinguishedOverload = true;
		this.sootMethods = new ArrayList<>();
		this.sootMethods = sootMethods;
	}

	@Override
	public List<UnitPlus> getPredecessors(UnitPlus unitPlus) {
		List<UnitPlus> preds = null;
		Set<UnitPlus> keys = completeCFG.keySet();
		for (UnitPlus key : keys) {
			if (key.getNumber() == unitPlus.getNumber()) {
				// System.out.println(key.getNumber()+key.getAttribute()+":"+unitPlus.getNumber()+unitPlus.getAttribute());
				if (key.getAttribute().equals(unitPlus.getAttribute())) {
					// System.out.println("**"+key.getNumber()+key.getAttribute()+":"+unitPlus.getNumber()+unitPlus.getAttribute());
					preds = completeCFG.get(key);
					// System.out.println(key.getNumber()+key.getAttribute());
					// System.out.println(preds.size());
				}
			}
		}
		return preds;
	}

	@Override
	public UnitPlus getStackTraceCallSite(UnitPlus unitPlus,
			StackTraceElement[] stackTrace, int indexOfStackTrace)
			throws ClassNotFoundException, FileNotFoundException {
		UnitPlus callSite = null;
		StackTraceElement stackTraceElement = stackTrace[indexOfStackTrace];
		String methodName = stackTraceElement.getMethodName();
		List<UnitPlus> preds = this.getPredecessors(unitPlus);
		for (UnitPlus pred : preds) {
			if (isDistinguishedOverload) {
				if (pred.getMethodPlus().getMethodName().equals(methodName)) {
					callSite = pred;
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
		for (UnitPlus pred : callSites) {
			if (pred.getMethodPlus().getMethodName().equals(methodName)) {
				sTCallSites.add(pred);
			}
		}
		return sTCallSites;
	}

	@Override
	public List<UnitPlus> getAllCallSites(UnitPlus unitPlus) {
		List<UnitPlus> allCallSites = this.getPredecessors(unitPlus);
		for (UnitPlus upPred : allCallSites) {
			if (!(upPred.getUnit() instanceof JInvokeStmt)) {
				System.out.println("The Units are not call sites!");
				System.out.println(upPred);
				allCallSites = null;
			}
		}
		return allCallSites;
	}

	@Override
	public UnitPlus getExitUnitPlus(MethodPlus Method) {
		UnitPlus tailUnitPlus = null;
		Unit tailUnit = methodToUnitGraph.get(Method).getTail();
		Set<UnitPlus> keys = completeCFG.keySet();
		for (UnitPlus key : keys) {
			if (key.getUnit().equals(tailUnit))
				tailUnitPlus = key;
		}
		return tailUnitPlus;
	}

	@Override
	public UnitPlus getEntryUnitPlus(MethodPlus Method) {
		UnitPlus headUnitPlus = null;
		Unit headUnit = methodToUnitGraph.get(Method).getHead();
		Set<UnitPlus> keys = completeCFG.keySet();
		for (UnitPlus key : keys) {
			if (key.getUnit().equals(headUnit))
				headUnitPlus = key;
		}
		return headUnitPlus;
	}

	@Override
	public MethodPlus getMethod(UnitPlus unitPlus) {
		return unitPlus.getMethodPlus();
	}

	@Override
	public boolean isEntry(UnitPlus unitPlus) {
		boolean isEntry = false;
		if (unitPlus.getUnit() instanceof BeginStmt) {
			isEntry = true;
		}
		return isEntry;
	}

	@Override
	public boolean isCall(UnitPlus unitPlus) {
		return unitPlus.isCall();
	}

	@Override
	public Value valueMap(Value defValue, UnitPlus unitPlus) {
		List<ValueBox> useValueBoxs = unitPlus.getUnit().getUseBoxes();
		ValueBox useValueBox = useValueBoxs.get(useValueBoxs.size() - 1);
		return useValueBox.getValue();
	}

	@Override
	public List<UnitPlus> StackTraceElementToUnit(
			StackTraceElement[] stackTrace, int indexOfStackTrace) {
		List<UnitPlus> units = new ArrayList<>();
		StackTraceElement ste = stackTrace[indexOfStackTrace];
		String methodName = ste.getMethodName();
		int lineNumber = ste.getLineNumber();
		for (SootMethod sootMethod : sootMethods) {
			if (sootMethod.getName().equals(methodName)) {
				units.addAll(lineNumberToUnit(sootMethod, lineNumber));
			}
		}
		for (UnitPlus unitPlus : units) {
			String methodString = String.format("%-30s", unitPlus
					.getMethodPlus().toString());
			System.out.println("StackTraceElementToUnit" + '\t'
					+ unitPlus.getNumber() + '\t' + methodString
					+ unitPlus.getUnit().toString());
		}
		return units;
	}

	private List<UnitPlus> lineNumberToUnit(SootMethod sootMethod,
			int lineNumber) {
		List<UnitPlus> units = new ArrayList<>();
		Body body = sootMethod.retrieveActiveBody();
		PatchingChain<Unit> unitPatchingChain = body.getUnits();
		for (Unit unit : unitPatchingChain) {
			List<Tag> tags = unit.getTags();
			for (Tag tag : tags) {
				if (tag instanceof LineNumberTag) {
					LineNumberTag lineNumberTag = (LineNumberTag) tag;
					if (lineNumber == lineNumberTag.getLineNumber()) {
						Set<UnitPlus> unitPluses = completeCFG.keySet();
						boolean unitCounted = false;
						for (UnitPlus unitPlus : unitPluses) {
							if (!unitCounted && unit.equals(unitPlus.getUnit())) {
								unitCounted = true;
								units.add(unitPlus);
							}
						}
					}
				}
			}
		}
		return units;
	}

	@Override
	public boolean isTransform(UnitPlus unitPlus) {
		boolean isTransform = false;
		if (unitPlus.getUnit() instanceof AbstractDefinitionStmt) {
			isTransform = true;
		}
		return isTransform;
	}

	@Override
	public Map<MethodPlus, UnitGraphPlus> getMethodToUnitGraphPlus() {
		return methodToUnitGraph;
	}

	@Override
	public List<UnitPlus> getAllCallSitesOfMethod(MethodPlus methodPlus) {
		return this.getAllCallSites(this.getEntryUnitPlus(methodPlus));
	}

	@Override
	public UnitPlus getStackTraceCallSiteOfMethod(MethodPlus methodPlus,
			StackTraceElement[] stackTrace, int indexOfStackTrace)
			throws ClassNotFoundException, FileNotFoundException {
		UnitPlus callSite = null;
		StackTraceElement stackTraceElement = stackTrace[indexOfStackTrace];
		List<UnitPlus> preds = this.getPredecessors(this
				.getEntryUnitPlus(methodPlus));
		System.out.println("Entry: "+this
				.getEntryUnitPlus(methodPlus));
		for (UnitPlus pred : preds) {
			System.out.println("Pred: "+pred);
			List<Tag> tags =pred.getUnit().getTags();
			for(Tag tag:tags){
				if(tag instanceof LineNumberTag){
					LineNumberTag lineNumberTag = (LineNumberTag) tag;
					if(stackTraceElement.getLineNumber()==lineNumberTag.getLineNumber()){
						callSite = pred;
					}
				}
			}
		}
		return callSite;

	}

	@Override
	public UnitPlus getCallSitePred(UnitPlus callB) {
		UnitPlus callA =null;
		Set<UnitPlus> keys = completeCFG.keySet();
		for(UnitPlus key:keys){
			if(key.getAttribute().equals("a")&&key.getNumber()==callB.getNumber()){
				callA = key;
			}
		}
		return callA;
	}
	
//	private boolean isLineInMethod(int lineNumber, MethodPlus methodPlus){
//		Chain<Unit> units = methodPlus.getSootmethod().retrieveActiveBody().getUnits();
//		int lowBound =Integer.MAX_VALUE, highBound = 0;
//		for(Unit unit:units){
//			List<Tag> tags = unit.getTags();
//			for(Tag  tag:tags){
//				if(tag instanceof LineNumberTag){
//					LineNumberTag lineNumberTag = (LineNumberTag) tag;
//					lowBound = Math.min(lineNumberTag.getLineNumber(), lowBound);
//					highBound = Math.max(lineNumberTag.getLineNumber(), highBound);
//				}
//			}
//		}
////		System.out.println("LineNumber: "+lineNumber+" LowBound: "+lowBound+" HighBound: "+highBound);
//		return lineNumber>=lowBound&&lineNumber<=highBound;
//	}

}

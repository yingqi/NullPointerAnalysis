package dispatcher;

import internal.MethodPlus;
import internal.State;
import internal.UnitPlus;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
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
import soot.jimple.InvokeExpr;
import soot.jimple.internal.AbstractDefinitionStmt;
import soot.jimple.internal.AbstractInvokeExpr;
import soot.jimple.internal.BeginStmt;
import soot.jimple.internal.EndStmt;
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
public class Dispatcher {
	private Map<UnitPlus, List<UnitPlus>> completeCFG;
	private Map<MethodPlus, UnitGraphPlus> methodToUnitGraph;

	public Dispatcher(Map<UnitPlus, List<UnitPlus>> completeCFG,
			StackTraceElement[] stackTrace,
			Map<MethodPlus, UnitGraphPlus> methodToUnitGraph) {
		this.completeCFG = completeCFG;
		this.methodToUnitGraph = methodToUnitGraph;
	}

	public List<UnitPlus> getPredecessors(UnitPlus unitPlus) {
		List<UnitPlus> preds = null;
		Set<UnitPlus> keys = completeCFG.keySet();
		for (UnitPlus key : keys) {
			if (key.getNumber() == unitPlus.getNumber()) {
				if (key.getAttribute().equals(unitPlus.getAttribute())) {
					preds = completeCFG.get(key);
				}
			}
		}
		return preds;
	}

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

	public Set<UnitPlus> StackTraceElementToUnit(
			StackTraceElement[] stackTrace, int indexOfStackTrace) {
		Set<UnitPlus> units = new HashSet<>();
		StackTraceElement ste = stackTrace[indexOfStackTrace];
		String methodName = ste.getMethodName();
		String classname = ste.getClassName();
		int lineNumber = ste.getLineNumber();
		units.addAll(lineNumberToUnit(methodName, classname, lineNumber));
		return units;
	}

	/**
	 * Transfer line number to its units
	 * @param sootMethod
	 * @param lineNumber
	 * @return
	 */
	private List<UnitPlus> lineNumberToUnit(String methodName, String className, 
			int lineNumber) {
		List<UnitPlus> units = new ArrayList<>();
		Set<UnitPlus> unitPluses = completeCFG.keySet();
		for (UnitPlus unitPlus : unitPluses) {
			Unit unit = unitPlus.getUnit();
			List<Tag> tags = unit.getTags();
			for (Tag tag : tags) {
				if (tag instanceof LineNumberTag) {
					LineNumberTag lineNumberTag = (LineNumberTag) tag;
					if (lineNumber == lineNumberTag.getLineNumber()&&
							unitPlus.getMethodPlus().getclassName().equals(className)
							&&unitPlus.getMethodPlus().getMethodName().equals(methodName)
							//to ensure that caller a and caller b is not reviewed twice
							//to ensure that do not go into the useless method inside
							&&!unitPlus.getAttribute().equals("b")
							) {
						units.add(unitPlus);
					}
				}
			}
		}
		
		return units;
	}

	public Map<MethodPlus, UnitGraphPlus> getMethodToUnitGraphPlus() {
		return methodToUnitGraph;
	}

	public UnitPlus getStackTraceCallSiteOfMethod(MethodPlus methodPlus,
			StackTraceElement[] stackTrace, int indexOfStackTrace)
			throws ClassNotFoundException, FileNotFoundException {
		UnitPlus callSite = null;
		StackTraceElement stackTraceElement = stackTrace[indexOfStackTrace];
		// get all call sites of this method
		List<UnitPlus> preds = this.getPredecessors(this
				.getEntryUnitPlus(methodPlus));
		// check which call site fit the stack trace
		for (UnitPlus pred : preds) {
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

}

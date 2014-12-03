package dispatcher;

import internal.MethodPlus;
import internal.UnitPlus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.InvokeExpr;
import soot.jimple.internal.AbstractDefinitionStmt;
import soot.jimple.internal.JInvokeStmt;
import soot.toolkits.graph.ExceptionalUnitGraphPlus;
import soot.toolkits.graph.UnitGraphPlus;

/**
 * this class is to create a control flow graph which contains all methods and
 * the connections between methods are methods calls. Warning: Still not know
 * how to decide which is a caller.
 * 
 * @author Yingqi
 * 
 */
public class CreateAllCFG {

	private Map<UnitPlus, List<UnitPlus>> completeCFG;
	private Map<UnitPlus, List<Unit>> CFG;
	private Set<UnitPlus> UnitDirectory;
	private Map<MethodPlus, UnitGraphPlus> methodToUnitGraph;
	private List<MethodPlus> Methods;
	private List<SootClass> sootclasses;

	/**
	 * constructor which initialize all necessary fields.
	 * 
	 * @param sootclass
	 *            representation of class in soot
	 * @param classNameString
	 *            class name
	 */
	public CreateAllCFG(List<SootClass> sootclasses) {
		// this.classNameString = classNameString;
		this.sootclasses = sootclasses;
		completeCFG = new HashMap<>();
		CFG = new HashMap<>();
		UnitDirectory = new HashSet<>();
		methodToUnitGraph = new HashMap<>();
		Methods = new ArrayList<>();
	}

	/**
	 * create a complete control flow graph.
	 * 
	 * @return
	 */
	public Map<UnitPlus, List<UnitPlus>> createCFG() {
		// in the class level
		for (SootClass sootclass : sootclasses) {
			List<SootMethod> sootMethods = sootclass.getMethods();
			// in the method level
			for (SootMethod sootMethod : sootMethods) {
				Body body = sootMethod.retrieveActiveBody();
				UnitGraphPlus unitGraph = new ExceptionalUnitGraphPlus(body);
				List<Type> parameterList = sootMethod.getParameterTypes();
				MethodPlus methodPlus = new MethodPlus(sootMethod.getName(),
						sootclass.getName(), parameterList, sootMethod);
				Methods.add(methodPlus);
				methodToUnitGraph.put(methodPlus, unitGraph);
				this.createCFGsForMethod(unitGraph, methodPlus);
			}

		}
		this.createCompleteCFG();
		this.combineAllCFGs();

		return completeCFG;
	}

	/**
	 * create control flow graphs for each method
	 * @param unitGraph
	 * @param Method
	 */
	private void createCFGsForMethod(UnitGraphPlus unitGraph, MethodPlus Method) {
		Iterator<Unit> unitIterator = unitGraph.iterator();
		int index = UnitDirectory.size();
		while (unitIterator.hasNext()) {
			Unit unit = unitIterator.next();
			//if a unit is caller
			if (unit instanceof JInvokeStmt) {
				UnitPlus NodeA = new UnitPlus(index, "a", unit, Method);
				UnitPlus NodeB = new UnitPlus(index, "b", unit, Method);
				index++;
				UnitDirectory.add(NodeA);
				UnitDirectory.add(NodeB);
				List<Unit> preds = new ArrayList<>();
				preds.addAll(unitGraph.getPredsOf(unit));
				CFG.put(NodeA, preds);
				CFG.put(NodeB, new ArrayList<Unit>());
			} else 
				//if a unit is a definition statement and the right side is a caller
				if(unit instanceof AbstractDefinitionStmt){
				AbstractDefinitionStmt abstractDefinitionStmt = (AbstractDefinitionStmt) unit;
				Value rightValue = abstractDefinitionStmt.getRightOp();
				if (rightValue instanceof InvokeExpr){
					UnitPlus NodeA = new UnitPlus(index, "a", unit, Method);
					UnitPlus NodeB = new UnitPlus(index, "b", unit, Method);
					index++;
					UnitDirectory.add(NodeA);
					UnitDirectory.add(NodeB);
					List<Unit> preds = new ArrayList<>();
					preds.addAll(unitGraph.getPredsOf(unit));
					CFG.put(NodeA, preds);
					CFG.put(NodeB, new ArrayList<Unit>());
				}else 
				// if it is only a denifition statement
				{
					UnitPlus Node = new UnitPlus(index, unit, Method);
					UnitDirectory.add(Node);
					index++;
					List<Unit> preds = new ArrayList<>();
					preds.addAll(unitGraph.getPredsOf(unit));
					CFG.put(Node, preds);
				}
			}else 
			//if it is neither above
			{
				UnitPlus Node = new UnitPlus(index, unit, Method);
				UnitDirectory.add(Node);
				index++;
				List<Unit> preds = new ArrayList<>();
				preds.addAll(unitGraph.getPredsOf(unit));
				CFG.put(Node, preds);
			}
		}
	}

	/**
	 * From Map<Unitplus, List<Unit> to Map<Unitplus, List<Unitplus>>
	 */
	private void createCompleteCFG() {
		Set<UnitPlus> keys = CFG.keySet();
		for (UnitPlus key : keys) {
			List<UnitPlus> unitPlusPreds = new ArrayList<>();
			List<Unit> unitPreds = CFG.get(key);
			// units for keys
			for (Unit unitPred : unitPreds) {
				// all unitpluses
				for (UnitPlus unitPlus : UnitDirectory) {
					// find unitplus for unit
					if (unitPlus.getUnit().equals(unitPred)) {
						//caller a is not any unit's predecessors unless it is a head which will be addressed later
						if (!unitPlus.getAttribute().equals("a")) {
							unitPlusPreds.add(unitPlus);
						}
					}
				}
			}
			completeCFG.put(key, unitPlusPreds);
		}
	}
	
	/**
	 * to find whetehr a method is analyzed
	 * @param unitPlus
	 * @return
	 */
	private boolean isCalledMethodAnalyzed(UnitPlus unitPlus) {
		boolean isCalledMethodAnalyzed = false;
		if (getMethodPlus(unitPlus) != null) {
			isCalledMethodAnalyzed = true;
		}
		return isCalledMethodAnalyzed;
	}
	
	/**
	 * to get method plus in a caller unit plus
	 * @param unitPlus
	 * @return
	 */
	private MethodPlus getMethodPlus (UnitPlus unitPlus){
		if (unitPlus.getUnit() instanceof JInvokeStmt) {
			JInvokeStmt jInvokeStmt = (JInvokeStmt) unitPlus.getUnit();
			SootMethod sootMethod = jInvokeStmt.getInvokeExpr().getMethod();
			for(MethodPlus methodPlus:methodToUnitGraph.keySet()){
				if (methodPlus.getSootmethod().equals(sootMethod)) {
					return methodPlus;
				}
			}
		}else if (unitPlus.getUnit() instanceof AbstractDefinitionStmt){
			AbstractDefinitionStmt abstractDefinitionStmt = (AbstractDefinitionStmt) unitPlus.getUnit();
			Value rightValue = abstractDefinitionStmt.getRightOp();
			if (rightValue instanceof InvokeExpr){
				InvokeExpr invokeExpr = (InvokeExpr) rightValue;
				SootMethod sootMethod = invokeExpr.getMethod();
				for(MethodPlus methodPlus:methodToUnitGraph.keySet()){
					if (methodPlus.getSootmethod().equals(sootMethod)) {
						return methodPlus;
					}
				}
			}
			
		}
		
		return null;
	}

	/**
	 * combine CFGs from different methods
	 */
	private void combineAllCFGs() {
		for (UnitPlus ud : UnitDirectory) {
			// for callers
			if (ud.getAttribute().equals("a") || ud.getAttribute().equals("b")) {
				// For method not analyzed
				if (!isCalledMethodAnalyzed(ud)) {
					// for caller b
					if (ud.getAttribute().equals("b")) {
						for (UnitPlus udPred : UnitDirectory) {
							// connect caller a and caller b
							if (udPred.getNumber() == ud.getNumber()
									&& udPred.getAttribute().equals("a")) {
								List<UnitPlus> preds = completeCFG.get(ud);
								preds.add(udPred);
							}
						}
					} 
					// for caller a in not analyzed method, no use
//					else {
//						for (UnitPlus udSucc : UnitDirectory) {
//							if (udSucc.getNumber() == ud.getNumber()
//									&& udSucc.getAttribute().equals("b")) {
//								List<UnitPlus> preds = completeCFG.get(udSucc);
//								preds.add(ud);
//							}
//						}
//					}
				} else {
					// For method analyzed
					if (ud.getAttribute().equals("a")) {
						//for caller a, add caller a to method head's predecessors
						UnitGraphPlus unitGraph = methodToUnitGraph.get(getMethodPlus(ud));
						Unit head = unitGraph.getHead();
						for (UnitPlus headUnitPlus : UnitDirectory) {
							if (headUnitPlus.getUnit().equals(head)
//									&& (!headUnitPlus.getAttribute().equals("b"))
											) {
								List<UnitPlus> preds = completeCFG
										.get(headUnitPlus);
								preds.add(ud);
							}
						}
					}else if(ud.getAttribute().equals("b")){
						//for caller b, add method's tail to caller b's predecessors
						UnitGraphPlus unitGraph = methodToUnitGraph.get(getMethodPlus(ud));
						List<UnitPlus> preds = completeCFG.get(ud);
						Unit tail = unitGraph.getTail();
						for (UnitPlus tailUnitPlus : UnitDirectory) {
							if (tailUnitPlus.getUnit().equals(tail)
//									&& (!tailUnitPlus.getAttribute().equals("a"))
											) {
								preds.add(tailUnitPlus);
								ud.setCall(true);
							}
						}
					}
				}
			}
		}
	}
	
//	private void combineAllCFG() {
//		for (UnitPlus ud : UnitDirectory) {
//			if (ud.getAttribute().equals("a")) {
//				boolean isMethodAnalysed = false;
//				for (MethodPlus method : Methods) {
//					if (ud.getUnit().toString()
//							.contains(method.getMethodName())) {
//						isMethodAnalysed = true;
//						UnitGraphPlus unitGraph = methodToUnitGraph.get(method);
//						Unit head = unitGraph.getHead();
//						for (UnitPlus headUnitPlus : UnitDirectory) {
//							if (headUnitPlus.getUnit().equals(head)
//									&& (!headUnitPlus.getAttribute()
//											.equals("b"))) {
//								List<UnitPlus> preds = completeCFG
//										.get(headUnitPlus);
//								preds.add(ud);
//								// ud.setEntry(true);
//							}
//						}
//					}
//				}
//				// For methods in other class
//			} else if (ud.getAttribute().equals("b")) {
//				boolean isMethodInClass = false;
//				for (MethodPlus method : Methods) {
//					if (ud.getUnit().toString()
//							.contains(method.getMethodName())) {
//						isMethodInClass = true;
//						UnitGraphPlus unitGraph = methodToUnitGraph.get(method);
//						List<UnitPlus> preds = completeCFG.get(ud);
//						Unit tail = unitGraph.getTail();
//						for (UnitPlus tailUnitPlus : UnitDirectory) {
//							if (tailUnitPlus.getUnit().equals(tail)
//									&& (!tailUnitPlus.getAttribute()
//											.equals("a"))) {
//								preds.add(tailUnitPlus);
//								ud.setCall(true);
//							}
//						}
//					}
//				}
//				// For methods in other class
//				if (!isMethodInClass) {
//					for (UnitPlus udPred : UnitDirectory) {
//						if (udPred.getNumber() == ud.getNumber()
//								&& udPred.getAttribute().equals("a")) {
//							List<UnitPlus> preds = completeCFG.get(ud);
//							preds.add(udPred);
//						}
//					}
//				}
//			}
//		}
//
//	}

	/**
	 * get the unit directory
	 * 
	 * @return unit directory
	 */
	public Set<UnitPlus> getUnitDirectory() {
		return UnitDirectory;
	}

	/**
	 * get the map of methods to unit graph
	 * 
	 * @return map of methods to unit graph
	 */
	public Map<MethodPlus, UnitGraphPlus> getMethodToUnitGraph() {
		return methodToUnitGraph;
	}

}

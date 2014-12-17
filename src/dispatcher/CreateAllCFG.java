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
import soot.Local;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.internal.AbstractDefinitionStmt;
import soot.jimple.internal.JInvokeStmt;
import soot.toolkits.graph.ExceptionalUnitGraphPlus;
import soot.toolkits.graph.UnitGraphPlus;
import soot.util.Chain;

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
			sootclass.setApplicationClass();
//			System.out.println("Analyze class: "+sootclass.getName()+sootMethods.size());
			// in the method level
			for (SootMethod sootMethod : sootMethods) {
//				System.out.println("Analyze method: "+sootclass.getName()+"."+sootMethod.getName());
				if(sootMethod.isConcrete()&&sootMethod.getSource()!=null
						&&!sootMethod.isJavaLibraryMethod()
//						&&!sootMethod.getName().equals("doMakeObject")
//						&&(sootMethod.getName().equals("notifyVisit")
//								||(sootMethod.getName().equals("visitToken")&&sootclass.getName().equals("com.puppycrawl.tools.checkstyle.checks.coding.FallThroughCheck")))
						){
					try {
						Body body = sootMethod.retrieveActiveBody();
						UnitGraphPlus unitGraph = new ExceptionalUnitGraphPlus(body);
						List<Type> parameterList = sootMethod.getParameterTypes();
						MethodPlus methodPlus = new MethodPlus(sootMethod.getName(),
								sootclass.getName(), parameterList);
						Methods.add(methodPlus);
						methodToUnitGraph.put(methodPlus, unitGraph);
						this.createCFGsForMethod(unitGraph, methodPlus);
					} catch (Exception e) {
						System.out.println("Encounter exception: "+sootclass.getName()+"."+sootMethod.getName());
						e.printStackTrace();
					}					
				}else {
//					System.out.println("non-concrete method: "+sootclass.getName()+"."+sootMethod.getName());
				}
			}
		}
		System.out.println("Method analyzed!");
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
//			System.out.println(unit);
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
		if (getMethodPlus(unitPlus).size() != 0) {
			isCalledMethodAnalyzed = true;
		}
		return isCalledMethodAnalyzed;
	}
	
	/**
	 * to get method plus in a caller unit plus
	 * @param unitPlus
	 * @return
	 */
	private List<MethodPlus> getMethodPlus (UnitPlus unitPlus){
		List<MethodPlus> methodList = new ArrayList<>();
		if (unitPlus.getUnit() instanceof JInvokeStmt) {
			JInvokeStmt jInvokeStmt = (JInvokeStmt) unitPlus.getUnit();
			InvokeExpr invokeExpr = jInvokeStmt.getInvokeExpr();
			methodList =  SootMethodAnalyzed(invokeExpr);
		}else if (unitPlus.getUnit() instanceof AbstractDefinitionStmt){
			AbstractDefinitionStmt abstractDefinitionStmt = (AbstractDefinitionStmt) unitPlus.getUnit();
			Value rightValue = abstractDefinitionStmt.getRightOp();
			if (rightValue instanceof InvokeExpr){
				InvokeExpr invokeExpr = (InvokeExpr) rightValue;				
				methodList =  SootMethodAnalyzed(invokeExpr);
			}	
		}
		return methodList;
	}
	
	private List<MethodPlus> SootMethodAnalyzed(InvokeExpr invokeExpr) {
		List<MethodPlus> methodList = new ArrayList<>();
		SootMethod sootMethod = invokeExpr.getMethod();
//		if (!sootMethod.isPhantom()) {
			for (MethodPlus methodPlusTemp : methodToUnitGraph.keySet()) {
				if (methodPlusTemp.equalTo(sootMethod)
						) {
					methodList.add(methodPlusTemp);
				}
			}
//		}
		return methodList;
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
				} else {
					// For method analyzed
					if (ud.getAttribute().equals("a")) {
						//for caller a, add caller a to method head's predecessors
						for(MethodPlus methodPlus:getMethodPlus(ud)){
							UnitGraphPlus unitGraph = methodToUnitGraph.get(methodPlus);
							Unit head = unitGraph.getHead();
							for (UnitPlus headUnitPlus : UnitDirectory) {
								if (headUnitPlus.getUnit().equals(head)
												) {
									List<UnitPlus> preds = completeCFG
											.get(headUnitPlus);
									preds.add(ud);
								}
							}
						}
					}else if(ud.getAttribute().equals("b")){
						//for caller b, add method's tail to caller b's predecessors
						for(MethodPlus methodPlus:getMethodPlus(ud)){
							UnitGraphPlus unitGraph = methodToUnitGraph.get(methodPlus);
							List<UnitPlus> preds = completeCFG.get(ud);
							Unit tail = unitGraph.getTail();
							for (UnitPlus tailUnitPlus : UnitDirectory) {
								if (tailUnitPlus.getUnit().equals(tail)
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
	}


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

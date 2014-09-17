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
import soot.jimple.internal.JInvokeStmt;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.ExceptionalUnitGraphPlus;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.graph.UnitGraphPlus;

/**
 * this class is to create a control flow graph which contains all methods and
 * the connections between methods are methods calls. Warning: Still not know
 * how to decide which is a caller.
 * 
 * @author Yingqi
 * 
 */
public class CreateCompleteCFG {

	private Map<UnitPlus, List<UnitPlus>> completeCFG;
	private Map<UnitPlus, List<Unit>> CFG;
	private Set<UnitPlus> UnitDirectory;
	private Map<MethodPlus, UnitGraphPlus> methodToUnitGraph;
	private List<MethodPlus> Methods;
	// private Method[] methods;
	private String classNameString;
	private SootClass sootclass;

	/**
	 * constructor which initialize all necessary fields.
	 * 
	 * @param sootclass
	 *            representation of class in soot
	 * @param classNameString
	 *            class name
	 */
	public CreateCompleteCFG(SootClass sootclass, String classNameString) {
		this.classNameString = classNameString;
		this.sootclass = sootclass;
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
		// try {
		// Class<?> classType = Class.forName(classNameString);
		// methods = classType.getDeclaredMethods();
		// for (Method method : methods) {
		// Class<?>[] parameterTypes = method.getParameterTypes();
		// List<Class<?>> parameterList = new ArrayList<>();
		// for(Class<?> parameterType:parameterTypes){
		// parameterList.add(parameterType);
		// System.out.println(parameterType);
		// }
		// SootMethod sootmethod = sootclass.getMethod(method.getName(),
		// parameterList);
		// Body body = sootmethod.retrieveActiveBody();
		// UnitGraph unitGraph = new ExceptionalUnitGraph(body);
		// methodToUnitGraph.put(methodName, unitGraph);

		List<SootMethod> sootMethods = sootclass.getMethods();
		for (SootMethod sootMethod : sootMethods) {
			Body body = sootMethod.retrieveActiveBody();
			UnitGraphPlus unitGraph = new ExceptionalUnitGraphPlus(body);
			List<Type> parameterList = sootMethod.getParameterTypes();
			MethodPlus methodPlus = new MethodPlus(sootMethod.getName(),
					classNameString, parameterList, sootMethod);
			Methods.add(methodPlus);
			methodToUnitGraph.put(methodPlus, unitGraph);
			this.createCFGsForMethod(unitGraph, methodPlus);
		}

		// }
		// } catch (ClassNotFoundException e) {
		// e.printStackTrace();
		// }
		this.createCompleteCFG();
		this.combineAllCFGs();

		return completeCFG;
	}

	private void createCFGsForMethod(UnitGraphPlus unitGraph, MethodPlus Method) {
		Iterator<Unit> unitIterator = unitGraph.iterator();
		int index = UnitDirectory.size();
		while (unitIterator.hasNext()) {
			Unit unit = unitIterator.next();
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
			} else {
				UnitPlus Node = new UnitPlus(index, unit, Method);
				UnitDirectory.add(Node);
				index++;
				List<Unit> preds = new ArrayList<>();
				preds.addAll(unitGraph.getPredsOf(unit));
				CFG.put(Node, preds);
			}

		}
	}

	private void createCompleteCFG() {
		Set<UnitPlus> keys = CFG.keySet();
		for (UnitPlus key : keys) {
			List<Unit> unitPreds = new ArrayList<>();
			List<UnitPlus> unitPlusPreds = new ArrayList<>();
			unitPreds = CFG.get(key);
			for (Unit unitPred : unitPreds) {
				for (UnitPlus unitPlus : UnitDirectory) {
					if (unitPlus.getUnit().equals(unitPred)) {
						if (!unitPlus.getAttribute().equals("a")) {
							unitPlusPreds.add(unitPlus);
						}
					}
				}
			}
			completeCFG.put(key, unitPlusPreds);
		}
	}

	private void combineAllCFGs() {
		for (UnitPlus ud : UnitDirectory) {
			if (ud.getAttribute().equals("a")) {
				boolean isMethodInClass = false;
				for (MethodPlus method : Methods) {
					if (ud.getUnit().toString()
							.contains(method.getMethodName())) {
						isMethodInClass = true;
						UnitGraphPlus unitGraph = methodToUnitGraph.get(method);
						Unit head = unitGraph.getHead();
						for (UnitPlus headUnitPlus : UnitDirectory) {
							if (headUnitPlus.getUnit().equals(head)
									&& (!headUnitPlus.getAttribute()
											.equals("b"))) {
								List<UnitPlus> preds = completeCFG
										.get(headUnitPlus);
								preds.add(ud);
								ud.setEntry(true);
							}
						}
					}
				}
				//For methods in other class
			} else if (ud.getAttribute().equals("b")) {
				boolean isMethodInClass = false;
				for (MethodPlus method : Methods) {
					if (ud.getUnit().toString()
							.contains(method.getMethodName())) {
						isMethodInClass = true;
						UnitGraphPlus unitGraph = methodToUnitGraph.get(method);
						List<UnitPlus> preds = completeCFG.get(ud);
						Unit tail = unitGraph.getTail();
						for (UnitPlus tailUnitPlus : UnitDirectory) {
							if (tailUnitPlus.getUnit().equals(tail)
									&& (!tailUnitPlus.getAttribute()
											.equals("a"))) {
								preds.add(tailUnitPlus);
								ud.setCall(true);
							}
						}
					}
				}
				//For methods in other class
				if(!isMethodInClass){
					for (UnitPlus udPred : UnitDirectory){
						if(udPred.getNumber()==ud.getNumber()&&udPred.getAttribute().equals("a")){
							List<UnitPlus> preds = completeCFG.get(ud);
							preds.add(udPred);
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


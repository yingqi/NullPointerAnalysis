package dispatcher;

import internal.MethodPlus;
import internal.State;
import internal.UnitPlus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.Immediate;
import soot.Local;
import soot.RefLikeType;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.Expr;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.NewExpr;
import soot.jimple.Ref;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.internal.AbstractDefinitionStmt;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JimpleLocal;
import soot.toolkits.graph.ExceptionalUnitGraphPlus;
import soot.toolkits.graph.UnitGraphPlus;
import soot.util.Chain;

/**
 * this class is to create a control flow graph which contains all methods and the connections between methods are
 * methods calls. Warning: Still not know how to decide which is a caller.
 * 
 * @author Yingqi
 * 
 */
public class CreateAllCFG {

	private Map<UnitPlus, List<UnitPlus>> completeCFG;
	private Map<UnitPlus, List<Unit>> CFG;
	private List<UnitPlus> UnitDirectory;
	private Map<MethodPlus, UnitGraphPlus> methodToUnitGraph;
	private Set<MethodPlus> Methods;
	private List<SootClass> sootclasses;
	private Map<Unit, UnitPlus> unitMapUnitPlus;
	private List<UnitPlus> callerAs;
	private List<UnitPlus> callerBs;
	long time;

	/**
	 * constructor which initialize all necessary fields.
	 * 
	 * @param sootclass
	 *            representation of class in soot
	 * @param classNameString
	 *            class name
	 */
	public CreateAllCFG(List<SootClass> sootclasses, long time) {
		callerAs = new ArrayList<>();
		callerBs = new ArrayList<>();
		unitMapUnitPlus = new HashMap<>();
		this.time = time;
		this.sootclasses = sootclasses;
		completeCFG = new HashMap<>();
		CFG = new HashMap<>();
		UnitDirectory = new ArrayList<>();
		methodToUnitGraph = new HashMap<>();
		Methods = new HashSet<>();
	}

	/**
	 * create a complete control flow graph.
	 * 
	 * @return
	 */
	public Map<UnitPlus, List<UnitPlus>> createCFG() {
		// in the class level
		System.out.println("Method " + (System.currentTimeMillis() - time));
		for (SootClass sootclass : sootclasses) {
			List<SootMethod> sootMethods = sootclass.getMethods();
			sootclass.setApplicationClass();
			// in the method level
			for (SootMethod sootMethod : sootMethods) {
//				 System.out.println("Analyze method: "+sootclass.getName()+"."+sootMethod.getName());
				if (sootMethod.isConcrete() && sootMethod.getSource() != null && !sootMethod.isJavaLibraryMethod()
						&& !sootMethod.getName().equals("doMakeObject")
						&&!(sootclass.getName().equals("org.apache.bcel.verifier.statics.Pass2Verifier$CPESSC_Visitor")&&sootMethod.getName().equals("visitCode"))
						) {
					try {
						Body body = sootMethod.retrieveActiveBody();
						UnitGraphPlus unitGraph = new ExceptionalUnitGraphPlus(body);
						List<Type> parameterList = sootMethod.getParameterTypes();
						MethodPlus methodPlus = new MethodPlus(sootMethod.getName(), sootclass.getName(),
								parameterList, sootclass, body);
						Methods.add(methodPlus);
						methodToUnitGraph.put(methodPlus, unitGraph);
						this.createCFGsForMethod(unitGraph, methodPlus);
					} catch (Exception e) {
						System.out.println("Encounter exception: " + sootclass.getName() + "." + sootMethod.getName());
						e.printStackTrace();
					}
				} else {
					// System.out.println("non-concrete method: "+sootclass.getName()+"."+sootMethod.getName());
				}
			}
		}
		System.out.println("Method analyzed!" + (System.currentTimeMillis() - time));

		this.createCompleteCFG();
		System.out.println("create completed cfg!" + (System.currentTimeMillis() - time));
		this.combineAllCFGs();
		return completeCFG;
	}

	/**
	 * create control flow graphs for each method
	 * 
	 * @param unitGraph
	 * @param Method
	 */
	private void createCFGsForMethod(UnitGraphPlus unitGraph, MethodPlus Method) {
		Iterator<Unit> unitIterator = unitGraph.iterator();
		int index = UnitDirectory.size();
		while (unitIterator.hasNext()) {
			Unit unit = unitIterator.next();
			// System.out.println(unit);
			// if a unit is caller
			if (unit instanceof JInvokeStmt) {
				UnitPlus NodeA = new UnitPlus(index, "a", unit, Method);
				UnitPlus NodeB = new UnitPlus(index, "b", unit, Method);
				callerAs.add(NodeA);
				callerBs.add(NodeB);
				// callerpairs.put(NodeA, NodeB);
				unitMapUnitPlus.put(unit, NodeB);
				index++;
				UnitDirectory.add(NodeA);
				UnitDirectory.add(NodeB);
				List<Unit> preds = new ArrayList<>();
				preds.addAll(unitGraph.getPredsOf(unit));
				CFG.put(NodeA, preds);
				CFG.put(NodeB, new ArrayList<Unit>());
			} else
			// if a unit is a definition statement and the right side is a caller
			if (unit instanceof AbstractDefinitionStmt) {
				AbstractDefinitionStmt abstractDefinitionStmt = (AbstractDefinitionStmt) unit;
				Value rightValue = abstractDefinitionStmt.getRightOp();
				if (rightValue instanceof InvokeExpr) {
					UnitPlus NodeA = new UnitPlus(index, "a", unit, Method);
					UnitPlus NodeB = new UnitPlus(index, "b", unit, Method);
					callerAs.add(NodeA);
					callerBs.add(NodeB);
					unitMapUnitPlus.put(unit, NodeB);
					index++;
					UnitDirectory.add(NodeA);
					UnitDirectory.add(NodeB);
					List<Unit> preds = new ArrayList<>();
					preds.addAll(unitGraph.getPredsOf(unit));
					CFG.put(NodeA, preds);
					CFG.put(NodeB, new ArrayList<Unit>());
				} else
				// if it is only a denifition statement
				{
					UnitPlus Node = new UnitPlus(index, unit, Method);
					unitMapUnitPlus.put(unit, Node);
					UnitDirectory.add(Node);
					index++;
					List<Unit> preds = new ArrayList<>();
					preds.addAll(unitGraph.getPredsOf(unit));
					CFG.put(Node, preds);
					if (unit.equals(unitGraph.getHead())) {
						Method.setHeadPlus(Node);
					} else if (unit.equals(unitGraph.getTail())) {
						Method.setTailPlus(Node);
					}
				}
			} 
			else
			// if it is neither above
			{
				UnitPlus Node = new UnitPlus(index, unit, Method);
				UnitDirectory.add(Node);
				unitMapUnitPlus.put(unit, Node);
				index++;
				List<Unit> preds = new ArrayList<>();
				preds.addAll(unitGraph.getPredsOf(unit));
				CFG.put(Node, preds);
				if (unit.equals(unitGraph.getHead())) {
					Method.setHeadPlus(Node);
				} else if (unit.equals(unitGraph.getTail())) {
					Method.setTailPlus(Node);
				}
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
				unitPlusPreds.add(unitMapUnitPlus.get(unitPred));
			}
			completeCFG.put(key, unitPlusPreds);
		}
	}

	/**
	 * to get method plus in a caller unit plus
	 * 
	 * @param unitPlus
	 * @return
	 */
	private Set<MethodPlus> getMethodPlus(UnitPlus unitPlus) {
		Set<MethodPlus> methodSet = new HashSet<>();
		if (unitPlus.getUnit() instanceof JInvokeStmt) {
			JInvokeStmt jInvokeStmt = (JInvokeStmt) unitPlus.getUnit();
			InvokeExpr invokeExpr = jInvokeStmt.getInvokeExpr();
			methodSet = SootMethodAnalyzed(invokeExpr);
		} else if (unitPlus.getUnit() instanceof AbstractDefinitionStmt) {
			AbstractDefinitionStmt abstractDefinitionStmt = (AbstractDefinitionStmt) unitPlus.getUnit();
			Value rightValue = abstractDefinitionStmt.getRightOp();
			if (rightValue instanceof InvokeExpr) {
				InvokeExpr invokeExpr = (InvokeExpr) rightValue;
				methodSet = SootMethodAnalyzed(invokeExpr);
			}
		}
		return methodSet;
	}

	private Set<MethodPlus> SootMethodAnalyzed(InvokeExpr invokeExpr) {
		Set<MethodPlus> calledMethodSet = new HashSet<>();
		SootMethod sootMethod = invokeExpr.getMethod();
		SootClass invokeSootClass = sootMethod.getDeclaringClass();
//		if(invokeExpr instanceof InstanceInvokeExpr){
//			InstanceInvokeExpr instanceInvokeExpr = (InstanceInvokeExpr) invokeExpr;
//			if(instanceInvokeExpr.toString().contains("interfaceinvoke $r1.<internal.Int: int getInt()>()")){
//				System.out.println(instanceInvokeExpr.getBase()+"\t"+instanceInvokeExpr.getBase().getType());
//				Value base = instanceInvokeExpr.getBase();
//				if(base instanceof JimpleLocal){
//					JimpleLocal jimpleLocal = (JimpleLocal) base;
//					System.out.println(jimpleLocal.getClass());
//				}
//			}
//		}
		if(!sootMethod.isJavaLibraryMethod()){
			if(invokeSootClass.isInterface()){
				for (MethodPlus methodPlusTemp : methodToUnitGraph.keySet()) {
					if (methodPlusTemp.equalTo(sootMethod)) {
						SootClass methodSootClass = methodPlusTemp.getSootClass();
						if(findMethodForInterfaces(methodSootClass, invokeSootClass)){
							calledMethodSet.add(methodPlusTemp);
						}
					}
				}
			}else if(invokeSootClass.isConcrete()){
				boolean findSameClass = false;
				for (MethodPlus methodPlusTemp : methodToUnitGraph.keySet()) {
					if (methodPlusTemp.equalTo(sootMethod)) {
						SootClass methodSootClass = methodPlusTemp.getSootClass();
						if (methodSootClass.equals(invokeSootClass)) {
							calledMethodSet.add(methodPlusTemp);
							findSameClass = true;
						}
					}
				}
				if(!findSameClass){
					SootClass tempClass = null;
					for (MethodPlus methodPlusTemp : methodToUnitGraph.keySet()) {
						if (methodPlusTemp.equalTo(sootMethod)) {
							SootClass methodSootClass = methodPlusTemp.getSootClass();
							if (findMethodForChildren(invokeSootClass, methodSootClass)) {
								if(tempClass==null||isBFatherOfA(methodSootClass, tempClass))
								tempClass = methodSootClass;
							}
						}
					}
				}
			}else if(invokeSootClass.isAbstract()){
				boolean isMethodInAbstractClass = false;
//				for (MethodPlus methodPlusTemp : methodToUnitGraph.keySet()) {
//					if (methodPlusTemp.equalTo(sootMethod)) {
//						SootClass methodSootClass = methodPlusTemp.getSootClass();
//						if (methodSootClass.equals(invokeSootClass)
//								&&sootMethod.getActiveBody().getUnits().size()!=0) {
//							isMethodInAbstractClass = true;
//							calledMethodSet.add(methodPlusTemp);
//						}
//					}
//				}
				if(!isMethodInAbstractClass){
					for (MethodPlus methodPlusTemp : methodToUnitGraph.keySet()) {
						if (methodPlusTemp.equalTo(sootMethod)) {
							SootClass methodSootClass = methodPlusTemp.getSootClass();
							if(findMethodForAbstractClass(methodSootClass,invokeSootClass)
									||findMethodForAbstractClass(invokeSootClass, methodSootClass)){
								calledMethodSet.add(methodPlusTemp);
							}
						}
					}
				}
			}else {
				System.out.println("Error class: "+invokeSootClass);
			}
		}
		return calledMethodSet;
	}
	
	private boolean findMethodForAbstractClass(SootClass childClass, SootClass fatherClass) {
		boolean findMethod = false;
		SootClass tempSootClass = childClass;
		while (tempSootClass.hasSuperclass()) {
			if (tempSootClass.equals(fatherClass) || tempSootClass.getInterfaces().contains(fatherClass)) {
				findMethod = true;
				break;
			}
			tempSootClass = tempSootClass.getSuperclass();
		}
		return findMethod;
	}
	
	private boolean isBFatherOfA(SootClass childClass, SootClass fatherClass) {
		boolean isFatehrOf = false;
		if(childClass!=null){
			SootClass tempSootClass = childClass;
			while(tempSootClass.hasSuperclass()){
				if(tempSootClass.getSuperclass().equals(fatherClass)){
					isFatehrOf = true;
					break;
				}
				tempSootClass = tempSootClass.getSuperclass();
			}
		}
		return isFatehrOf;
	}
	
	private boolean findMethodForInterfaces(SootClass concreteClass, SootClass interfaceClass) {
		boolean findMethod = false;
		SootClass tempSootClass = concreteClass;
		while (tempSootClass.hasSuperclass()) {
			if (tempSootClass.getInterfaces().contains(interfaceClass)) {
				findMethod = true;
				break;
			}
			tempSootClass = tempSootClass.getSuperclass();
		}
		return findMethod;
	} 
	
	//father class cannot use method defined in children so that father could only be method class
	private boolean findMethodForChildren(SootClass childClass, SootClass fatherClass) {
		boolean findMethod = false;
		SootClass tempSootClass = childClass;
		while (tempSootClass.hasSuperclass()) {
			if (tempSootClass.equals(fatherClass)) {
				findMethod = true;
				break;
			}
			tempSootClass = tempSootClass.getSuperclass();
		}
		return findMethod;
	}
	/**
	 * combine CFGs from different methods
	 */

	private void combineAllCFGs() {
		for (int i = 0; i < callerAs.size(); i++) {
			Set<MethodPlus> calledMethodPlus = getMethodPlus(callerAs.get(i));
			if (calledMethodPlus.size() == 0) {
				List<UnitPlus> preds = completeCFG.get(callerBs.get(i));
				preds.add(callerAs.get(i));
			} else {
				callerBs.get(i).setCall(true);
				for (MethodPlus methodPlus : calledMethodPlus) {
					completeCFG.get(methodPlus.getHeadPlus()).add(callerAs.get(i));
				}
				for (MethodPlus methodPlus : calledMethodPlus) {
					completeCFG.get(callerBs.get(i)).add(methodPlus.getTailPlus());
				}

			}
		}
	}

	/**
	 * get the unit directory
	 * 
	 * @return unit directory
	 */
	public List<UnitPlus> getUnitDirectory() {
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

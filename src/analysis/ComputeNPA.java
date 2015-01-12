package analysis;

import internal.Element;
import internal.MethodPlus;
import internal.State;
import internal.Summary;
import internal.UnitPlus;
import internal.VisitRecord;

import java.io.FileNotFoundException;
import java.util.*;

import javax.xml.stream.events.EndDocument;

import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.NullConstant;
import soot.jimple.ParameterRef;
import soot.jimple.ReturnStmt;
import soot.jimple.ThisRef;
import soot.jimple.internal.AbstractDefinitionStmt;
import soot.jimple.internal.JInvokeStmt;
import dispatcher.Dispatcher;
import dispatcher.LightDispatcher;

/**
 * This program is to analyze MethodPlus for NPA
 */
/**
 * @version 2014-08-06
 * @author Lind, Yingqi
 * 
 */
public class ComputeNPA {

	private Set<UnitPlus> NPA;
	private Set<UnitPlus> PossibleNPAs;
	private Stack<MethodPlus> CS;
	private Dispatcher dispatcher;
	private Summary summary;
//	private Set<VisitRecord> visitRecords;
	private Map<UnitPlus, Set<State>> visits;
	private int indexOfStackTrace;
	private StackTraceElement[] stackTrace;
	

	/**
	 * constructor
	 * 
	 * @param dispatcher
	 * @param stackTrace
	 */
	public ComputeNPA(Dispatcher dispatcher, StackTraceElement[] stackTrace) {
		PossibleNPAs = new HashSet<>();
		NPA = new HashSet<>();
		this.dispatcher = dispatcher;
		CS = new Stack<>();
		summary = new Summary();
//		visitRecords = new HashSet<>();
		visits = new HashMap<UnitPlus, Set<State>>();
		indexOfStackTrace = 0;
		this.stackTrace = stackTrace;
	}

	 /**
	 * reset the index of stack trace
	 */
	 public void resetIndexOfStarckTrace() {
	 indexOfStackTrace = 0;
//	 visitRecords.clear();
	 visits.clear();
	 }

	/**
	 * analyze the method
	 * 
	 * @param unitPlus
	 * @param states
	 * @throws ClassNotFoundException
	 * @throws FileNotFoundException
	 */
	public Set<State> analyzeMethod(Element initializeElement) throws ClassNotFoundException, FileNotFoundException {
		// Queue or Stack, not considered clearly!
//		Stack<Element> worklist = new Stack<Element>();
		Queue<Element> worklist = new LinkedList<>();
		if (initializeElement.getStates().size() != 0) {
			worklist.offer(initializeElement);
			visits.put(initializeElement.getUnitPlus(), initializeElement.getStates());
////			worklist.push(initializeElement);
//			visitRecords.add(new VisitRecord(initializeElement.getUnitPlus().getNumber(), initializeElement.getUnitPlus().getAttribute(), initializeElement.getStates()));
		}
		Set<State> tempStates = initializeElement.getStates();
		boolean isFirstTimeVisitedEntryInUpperClass = true;
		while (!worklist.isEmpty()) {
//			Element presentElement = worklist.pop();
			Element presentElement = worklist.poll();
//			visitRecords.add(new VisitRecord(presentElement.getUnitPlus().getNumber(), presentElement.getUnitPlus().getAttribute(), presentElement.getStates()));
//			System.out.println("Present: "+presentElement);
			List<UnitPlus> preds = dispatcher.getPredecessors(presentElement.getUnitPlus());
//			tempStates = presentElement.getStates();
			for (UnitPlus upPred : preds) {
				boolean isElementVisited = false;
//				for (VisitRecord visitRecord : visitRecords) {
//					if (visitRecord.visited(upPred, presentElement.getStates())) {
//						isElementVisited = true;
//						break;
//					}
//				}
				if (!isElementVisited) {
//					Element originalElement = new Element(upPred, presentElement.getStates());
					Element predElement = new Element(upPred, presentElement.getStates());
//					if(upPred.toString().contains("r2 := @parameter1: freemarker.template.ObjectWrapper")
//							&&upPred.getMethodPlus().getclassName().contains("freemarker.ext.beans.CollectionModel$1")
//							&&upPred.getMethodPlus().getMethodName().contains("create")){
//						System.out.println("PredElement: "+predElement);
//					}
					if (LightDispatcher.isTransform(upPred)) {
						predElement.transform(NPA, PossibleNPAs);
						worklistPush(predElement, worklist, visits);
					} else if (upPred.isCall()) {
//						if(dispatcher.getPredecessors(upPred).size()>1){
//							System.out.println("Multiple callers: "+upPred);
//							for (UnitPlus upPredPred : dispatcher.getPredecessors(upPred)) {
//								System.out.println(upPredPred.getMethodPlus().getclassName()+"."+upPredPred.getMethodPlus().getMethodName());
//							}
//						}
						boolean isFirstTimeGoDeeper = true;
						for (UnitPlus upPredPred : dispatcher.getPredecessors(upPred)) {
							transferReturnState(predElement, upPredPred.getMethodPlus());
							Set<State> outgoingStates = summary.getInformation(predElement.getStates(),
									upPredPred.getMethodPlus());
//							if(upPredPred.getMethodPlus().getMethodName().equals("setFirstChild")){
//								System.out.println(upPredPred);
//								System.out.println(outgoingStates);
//							}
							if (outgoingStates == null) {
								MethodPlus calledMethodPlust = null;
								calledMethodPlust = upPredPred.getMethodPlus();
								CS.push(calledMethodPlust);
								UnitPlus exitnode = dispatcher.getExitUnitPlus(calledMethodPlust);
//								UnitPlus entrynode = dispatcher.getEntryUnitPlus(calledMethodPlust);
								outgoingStates = mapAtCall(predElement.getStates(), upPred, exitnode);
								// states before going into a deeper method
//								transitStates = outgoingStates;
								Element callElement = new Element(exitnode, outgoingStates);
								outgoingStates = analyzeMethod(callElement);
								calledMethodPlust = CS.pop();
								outgoingStates = mapAtEntryOfMethod(outgoingStates, calledMethodPlust, upPred);
								// predStates = transitStates;
//								outgoingStates = mapAtEntryOfMethod(transitStates, calledMethodPlust, upPred);
								// after analyze the call method, the predElement changes to the caller a
//								predElement.setUnitPlus(dispatcher.getCallSitePred(upPred));
								summary.setInformation(calledMethodPlust, predElement.getStates(), outgoingStates);
							} else {
								// System.out.println("Analyzed: Method: " + outgoingStates);
//								predElement.setUnitPlus(dispatcher.getCallSitePred(upPred));
							}
							// if method has not been analyzed, then the outgoing states are the new analyzed states
							// if the method has been analyzed, then the outgoing states are the states in the summary
//							predElement.setStates(outgoingStates);
//							worklistPush(originalElement, predElement, worklist, visitRecords, false);
							if(isFirstTimeGoDeeper){
								predElement.setStates(outgoingStates);
								isFirstTimeGoDeeper = false;
							}else {
								LightDispatcher.addAll(predElement.getStates(), outgoingStates);
							}
						}
//						System.out.println("Call End: "+predElement.getStates());
						predElement.setUnitPlus(dispatcher.getCallSitePred(upPred));
						worklistPush(predElement, worklist, visits);
					}
					// if upPred is a normal statement push it and go on
					else if (!LightDispatcher.isEntry(upPred)) {
						Set<State> removeStates = new HashSet<>();
						Set<State> addStates = new HashSet<>();
						if (upPred.getUnit() instanceof ReturnStmt) {
							ReturnStmt returnStmt = (ReturnStmt) upPred.getUnit();
							Value returnValue = returnStmt.getOp();
							for (State state : predElement.getStates()) {
								if (state.isReturnValue()
										&& state.getReturnInMethodPlus().equals(upPred.getMethodPlus())) {
									// System.out.println("return value");
									if (returnValue instanceof NullConstant) {
										LightDispatcher.AddNPA(state, upPred, NPA, predElement.getStates(), removeStates);
										removeStates.add(state);
									}
									// it not null, add them to states
									else {
										LightDispatcher.stateReplace(state, returnValue, upPred, predElement.getStates(), removeStates, addStates);
										state.desetReturnValue();
									}
								}
							}
						}
						for (State removeState : removeStates) {
							predElement.getStates().remove(removeState);
						}
						for (State addState : addStates) {
							predElement.getStates().add(addState);
						}
						worklistPush(predElement, worklist, visits);
					}
					else if (LightDispatcher.isEntry(upPred)) {
//						System.out.println(isFirstTimeVisitedEntryInUpperClass);
//						System.out.println(tempStates);
						if (isFirstTimeVisitedEntryInUpperClass) {
							tempStates = LightDispatcher.copyStates(predElement.getStates());
							isFirstTimeVisitedEntryInUpperClass = false;
						} else {
							LightDispatcher.addAll(tempStates, predElement.getStates());
						}
//						System.out.println("Entry: "+predElement.getUnitPlus().getMethodPlus()+"\t"+tempStates);
					}else {
						System.out.println("Error: "+predElement);
					}
//					visitRecords.add(new VisitRecord(originalElement.getUnitPlus().getNumber(), originalElement.getUnitPlus().getAttribute(), originalElement.getStates()));
				}
			}
		}

		// if work list and CS are all empty, we need to find the caller site of this method with the help of stack
		// trace
		if (CS.size() == 0) {
			if (indexOfStackTrace < stackTrace.length - 1) {
				indexOfStackTrace++;
				UnitPlus callSite = dispatcher.getStackTraceCallSiteOfMethod(initializeElement.getUnitPlus()
						.getMethodPlus(), stackTrace, indexOfStackTrace);
				System.out.println("NumberOfCallSite: " + indexOfStackTrace + "    Called Method: "
						+ initializeElement.getUnitPlus().getMethodPlus() + "    CallSite:  " + callSite);
				System.out.println("Before Map: "+tempStates);
				Set<State> outgoingStates = mapAtEntryOfMethod(tempStates, initializeElement.getUnitPlus()
						.getMethodPlus(), callSite);
				System.out.println("After Map: "+outgoingStates);
				Element callSiteElement = new Element(callSite, outgoingStates);
//				if(indexOfStackTrace ==3){
//					System.out.println("PossibleNPA States:  " + tempStates);
//				}else {
					analyzeMethod(callSiteElement);
//				}
			} else {
				System.out.println("PossibleNPA States:  " + tempStates);
			}
		}
//		System.out.println("Return: "+tempStates);
		return tempStates;
	}

	private void transferReturnState(Element element, MethodPlus calledMethodPlus) {
		if (element.getUnitPlus().getUnit() instanceof AbstractDefinitionStmt) {
			AbstractDefinitionStmt abstractDefinitionStmt = (AbstractDefinitionStmt) element.getUnitPlus().getUnit();
			Value rightValue = abstractDefinitionStmt.getRightOp();
			Value leftValue = abstractDefinitionStmt.getLeftOp();
			if (rightValue instanceof InvokeExpr) {
				for (State state : element.getStates()) {
					// if states and left values are the same
					if (state.equalValue(leftValue, element.getUnitPlus().getMethodPlus())) {
						state.setReturnValue(calledMethodPlus);
					}
				}
			}
		}
	}

	/**
	 * map the states at the entry of a method
	 * 
	 * @param states
	 * @param entrynode
	 * @param upPred
	 * @return
	 */
	private Set<State> mapAtEntryOfMethod(Set<State> states, MethodPlus calledMethodPlus, UnitPlus upPred) {
		// map all the local values in states to the used values in the unit the invoke expression case does not need to
		// worry as when mapped back, the return value is the real value of the left value in the upper method
		Set<State> removeStates = new HashSet<>();
		Set<State> addStates = new HashSet<>();
		for (State state : states) {
			if (state.getVariable() instanceof ParameterRef) {
				// System.out.println("ParameterRef: "+state.getValue());
				ParameterRef parameterRef = (ParameterRef) state.getVariable();
				int index = parameterRef.getIndex();
				if ((upPred.getUnit() instanceof JInvokeStmt) && state.getmethod().equals(calledMethodPlus)) {
					JInvokeStmt jInvokeStmt = (JInvokeStmt) upPred.getUnit();
					InvokeExpr invokeExpr = jInvokeStmt.getInvokeExpr();
					List<Value> args = invokeExpr.getArgs();
					if (args.get(index) instanceof NullConstant) {
						LightDispatcher.AddNPA(state, upPred, NPA, states, removeStates);
						removeStates.add(state);
					} else {
//						state.replaceValue(args.get(index), upPred);
						LightDispatcher.stateReplace(state, args.get(index), upPred, states, removeStates, addStates);
					}
				} else if ((upPred.getUnit() instanceof AbstractDefinitionStmt)
						&& state.getmethod().equals(calledMethodPlus)) {
					AbstractDefinitionStmt abstractDefinitionStmt = (AbstractDefinitionStmt) upPred.getUnit();
					Value rightValue = abstractDefinitionStmt.getRightOp();
					if (rightValue instanceof InvokeExpr) {
						InvokeExpr invokeExpr = (InvokeExpr) rightValue;
						List<Value> args = invokeExpr.getArgs();
						if (args.get(index) instanceof NullConstant) {
							LightDispatcher.AddNPA(state, upPred, NPA, states, removeStates);
						} else {
//							state.replaceValue(args.get(index), upPred);
							LightDispatcher.stateReplace(state, args.get(index), upPred, states, removeStates, addStates);
						}
					}
				}
			} else if (state.getVariable() instanceof ThisRef) {
				// System.out.println("ThisRef: "+state.getValue());
				if (upPred.getUnit() instanceof JInvokeStmt) {
					JInvokeStmt jInvokeStmt = (JInvokeStmt) upPred.getUnit();
					InvokeExpr invokeExpr = jInvokeStmt.getInvokeExpr();
					if (invokeExpr instanceof InstanceInvokeExpr) {
						InstanceInvokeExpr instanceInvokeExpr = (InstanceInvokeExpr) invokeExpr;
//						state.replaceValue(instanceInvokeExpr.getBase(), upPred);
						LightDispatcher.stateReplace(state, instanceInvokeExpr.getBase(), upPred, states, removeStates, addStates);
					}
				} else if (upPred.getUnit() instanceof AbstractDefinitionStmt) {
					AbstractDefinitionStmt abstractDefinitionStmt = (AbstractDefinitionStmt) upPred.getUnit();
					Value rightValue = abstractDefinitionStmt.getRightOp();
					if (rightValue instanceof InvokeExpr) {
						InvokeExpr invokeExpr = (InvokeExpr) rightValue;
						if (invokeExpr instanceof InstanceInvokeExpr) {
							InstanceInvokeExpr instanceInvokeExpr = (InstanceInvokeExpr) invokeExpr;
//							state.replaceValue(instanceInvokeExpr.getBase(), upPred);
							LightDispatcher.stateReplace(state, instanceInvokeExpr.getBase(), upPred, states, removeStates, addStates);
						}
					}
				}
			}
		}
		for (State state : removeStates) {
			states.remove(state);
		}
		for (State state : addStates) {
			states.add(state);
		}
		return states;
	}

	private Set<State> mapAtCall(Set<State> states, UnitPlus upPred, UnitPlus exitnode) {
		// if the unit is a JInvokeStmt
		Set<State> removeStates = new HashSet<>();
		Set<State> addStates = new HashSet<>();
		if (upPred.getUnit() instanceof JInvokeStmt) {
			MethodPlus calledMethodPlus = exitnode.getMethodPlus();
			JInvokeStmt jInvokeStmt = (JInvokeStmt) upPred.getUnit();
			List<Value> args = jInvokeStmt.getInvokeExpr().getArgs();
			for (int index = 0; index < args.size(); index++) {
				for (State state : states) {
					if (state.equalValue(args.get(index), upPred.getMethodPlus())) {
//						state.replaceValue(calledMethodPlus.getBody().getParameterLocal(index), exitnode);
						LightDispatcher.stateReplace(state, calledMethodPlus.getBody().getParameterLocal(index), exitnode, states, removeStates, addStates);
					}
				}
			}
		}
		// if the unit is definition statement and right value is invoke expression
		else if (upPred.getUnit() instanceof AbstractDefinitionStmt) {
			AbstractDefinitionStmt abstractDefinitionStmt = (AbstractDefinitionStmt) upPred.getUnit();
			Value rightValue = abstractDefinitionStmt.getRightOp();
			// Value leftValue = abstractDefinitionStmt.getLeftOp();
			if (rightValue instanceof InvokeExpr) {
				// check if used values in the invoke experssion are states
				MethodPlus calledMethodPlus = exitnode.getMethodPlus();
				InvokeExpr invokeExpr = (InvokeExpr) rightValue;
				List<Value> args = invokeExpr.getArgs();
				for (int index = 0; index < args.size(); index++) {
					for (State state : states) {
						if (state.equalValue(args.get(index), upPred.getMethodPlus())) {
							LightDispatcher.stateReplace(state, calledMethodPlus.getBody().getParameterLocal(index), exitnode, states, removeStates, addStates);
						}
					}
				}
			}
		}
		for (State state : removeStates) {
			states.remove(state);
		}
		for (State state : addStates) {
			states.add(state);
		}
		return states;
	}

	public Set<UnitPlus> getNPA() {
		return NPA;
	}


//	private void worklistPush(Element originalElement, Element predElement, Stack<Element> worklist,
//			Set<VisitRecord> visitRecords, boolean noNeedToCheck) {
//		if (noNeedToCheck) {
//			worklist.push(predElement);
//		} else {
//			boolean isElementAnalyzed = false;
//			for (VisitRecord visitRecord : visitRecords) {
//				if (visitRecord.elementVisited(originalElement)) {
//					isElementAnalyzed = true;
//					break;
//				}
//			}
//			if (!isElementAnalyzed) {
//				worklistPush(predElement, worklist, visitRecords);
//			}
//		}
//	}

	private void worklistPush(Element predElement, Queue<Element> worklist,
			Map<UnitPlus, Set<State>> visits) {
		Set<State> removeStates = new HashSet<>();
		if (predElement.getStates().size() > 0) {
			// to check whether the element has been analyzed
			boolean isElementAnalyzed = false;
			if(LightDispatcher.unitPlusSetContains(visits.keySet(), predElement.getUnitPlus())){
				Set<State> visitStates = visits.get(predElement.getUnitPlus());
				for(State state:predElement.getStates()){
					if(LightDispatcher.stateSetContains(visitStates, state)){
						removeStates.add(state);
					}
				}
				if(predElement.getStates().size()==removeStates.size()){
					isElementAnalyzed = true;
				}else {
					LightDispatcher.addAll(visits.get(predElement.getUnitPlus()), predElement.getStates());
				}
			}else {
				visits.put(predElement.getUnitPlus(), predElement.getStates());
			}
			// to check whether the element is already in worklist
			boolean isElementInWorklist = false;
			for (Element wlElement : worklist) {
				if (wlElement.equalTo(predElement)) {
					isElementInWorklist = true;
					break;
				}
			}
			if ((!isElementAnalyzed) && (!isElementInWorklist)) {
				//to check whether the unitplus is in the worklist
				boolean isUnitPlusInWorkList = false;
				for (Element wlElement : worklist) {
					if (wlElement.getUnitPlus().equalTo(predElement.getUnitPlus())) {
						isUnitPlusInWorkList = true;
						LightDispatcher.addAll(wlElement.getStates(), predElement.getStates());
						break;
					}
				}
				if(!isUnitPlusInWorkList){
//					boolean isRemove = false;
//					for (State state : removeStates) {
//						isRemove =true;
//						predElement.getStates().remove(state);
//					}
//					if(isRemove){
//						System.out.println(visits.get(predElement.getUnitPlus()));
//						System.out.println(worklist);
//						System.out.println(predElement);
//					}
					worklist.offer(predElement);
				}
			}
		}
	}
	
	public Set<UnitPlus> getPossibleNPAs() {
		return PossibleNPAs;
	}

}

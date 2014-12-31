package analysis;

import internal.Element;
import internal.MethodPlus;
import internal.State;
import internal.Summary;
import internal.UnitPlus;
import internal.VisitRecord;

import java.io.FileNotFoundException;
import java.util.*;

import java_cup.internal_error;
import soot.Value;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.NullConstant;
import soot.jimple.ParameterRef;
import soot.jimple.ReturnStmt;
import soot.jimple.ThisRef;
import soot.jimple.internal.AbstractDefinitionStmt;
import soot.jimple.internal.BeginStmt;
import soot.jimple.internal.JInvokeStmt;
import dispatcher.Dispatcher;

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
	private Set<VisitRecord> visitRecords;
	private int indexOfStackTrace;
	private StackTraceElement[] stackTrace;
	private Set<State> transitStates;

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
		visitRecords = new HashSet<>();
		indexOfStackTrace = 0;
		this.stackTrace = stackTrace;
	}

	/**
	 * reset the index of stack trace
	 */
	public void resetIndexOfStarckTrace() {
		indexOfStackTrace = 0;
		visitRecords.clear();
	}

	/**
	 * analyze the method
	 * 
	 * @param unitPlus
	 * @param states
	 * @throws ClassNotFoundException
	 * @throws FileNotFoundException
	 */
	public void analyzeMethod(Element initializeElement) throws ClassNotFoundException, FileNotFoundException {
		// the list to work on
		Stack<Element> worklist = new Stack<Element>();
		// first we have to have states
		if (initializeElement.getStates().size() != 0) {
			worklist.push(initializeElement);
		}
		// if work list is empty, get the present states
		Set<State> tempStates = initializeElement.getStates();
		boolean isFirstTimeVisitedEntry = true;
		boolean isFirstTimeVisitedEntryInUpperClass = true;
		Element presentElement = initializeElement;
//		if(!worklist.isEmpty()){
//			presentElement = worklist.peek();
//		}
//		System.out.println(presentElement);
		while (!worklist.isEmpty()) {
			presentElement = worklist.pop();
			visitRecords.add(new VisitRecord(presentElement.getUnitPlus().getNumber(), 
					presentElement.getUnitPlus().getAttribute(), dispatcher.copyStates(presentElement.getStates())));
//			tempStates = presentElement.getStates();
//			System.out.println("Present: "+presentElement);
			UnitPlus presentUnitPlus = presentElement.getUnitPlus();
			Set<UnitPlus> preds = dispatcher.getPredecessors(presentUnitPlus);
			Set<State> presentStates = presentElement.getStates();
			presentElement = null;
			for (UnitPlus upPred : preds) {
				Element predElement = new Element(upPred, presentStates);
				boolean isElementAnalyzed = false;
//				for (VisitRecord visitRecord: visitRecords) {
//					if (visitRecord.elementVisited(predElement)) {
//						isElementAnalyzed = true;
//					}
//				}
				if (!isElementAnalyzed) {
					// if the element is definition statement
//					System.out.println("Pred: "+predElement);
//					Set<State> recordStates = dispatcher.copyStates(predElement.getStates());
					if (dispatcher.isTransform(upPred)) {
						predElement.transform(NPA, PossibleNPAs);
						//to ensure that there are states
						worklistPush(predElement,worklist,visitRecords,false);	
					} else if (dispatcher.isCall(upPred)) {
						for (UnitPlus upPredPred : dispatcher.getPredecessors(upPred)) {
							transferReturnState(predElement, upPredPred.getMethodPlus());
							Set<State> outgoingStates = summary.getInformation(predElement.getStates(),upPredPred.getMethodPlus());
							if (outgoingStates == null) {
//								Set<State> predStates = predElement.getStates();
								MethodPlus calledMethodPlust = null;
								// upPred is a caller b so it has only one predecessor
								calledMethodPlust = upPredPred.getMethodPlus();
								CS.push(calledMethodPlust);
								UnitPlus exitnode = dispatcher.getExitUnitPlus(calledMethodPlust);
								UnitPlus entrynode = dispatcher.getEntryUnitPlus(calledMethodPlust);
								outgoingStates = mapAtCall(predElement.getStates(), upPred, exitnode);
								// states before going into a deeper method
								transitStates = outgoingStates;
//								predElement.setUnitPlus(exitnode);
//								predElement.setStates(outgoingStates);
								Element callElement = new Element(exitnode, outgoingStates);
								analyzeMethod(callElement);
								calledMethodPlust = CS.pop();
//								predStates = transitStates;
								outgoingStates = mapAtEntryOfMethod(transitStates, calledMethodPlust, upPred);
								// after analyze the call method, the predElement changes to the caller a
								predElement.setUnitPlus(dispatcher.getCallSitePred(upPred));
								summary.setInformation(calledMethodPlust, predElement.getStates(), outgoingStates);
							} else {
//								System.out.println("Analyzed: "+outgoingStates);
								predElement.setUnitPlus(dispatcher.getCallSitePred(upPred)); 
							}
							// if method has not been analyzed, then the outgoing states are the new analyzed states
							// if the method has been analyzed, then the outgoing states are the states in the summary
							predElement.setStates(outgoingStates);
							worklistPush(predElement, worklist, visitRecords, true);
						}
					}
					// if upPred is a normal statement push it and go on
					else if (!dispatcher.isEntry(upPred)) {
						Set<State> removeStates = new HashSet<>();
						if (upPred.getUnit() instanceof ReturnStmt) {
							ReturnStmt returnStmt = (ReturnStmt) upPred.getUnit();
							Value returnValue = returnStmt.getOp();
							for (State state : predElement.getStates()) {
								if (state.getAttribute().equals("return value")
										&&state.getReturnInMethodPlus().equals(upPred.getMethodPlus())) {
									// System.out.println("return value");
									if (returnValue instanceof NullConstant) {
										increaseNPA(upPred);
										removeStates.add(state);
									}
									// it not null, add them to states
									else {
										state.replaceValue(returnValue, upPred);
										state.setAttribute("");
										state.setReturnInMethodPlus(null);
									}
								}
							}
						}
						for (State removeState : removeStates) {
							predElement.getStates().remove(removeState);
						}
						worklistPush(predElement,worklist,visitRecords,false);
					}
					// if it is the entry of method and CS is not empty, it means that it needs to go to upper method
					// and the states need to be preserved and that is where we need the transitStates
					else if (dispatcher.isEntry(upPred) && !CS.isEmpty()) {
						if (isFirstTimeVisitedEntry) {
							transitStates = dispatcher.copyStates(predElement.getStates());
							isFirstTimeVisitedEntry = false;
						} else {
							addAll(transitStates,predElement.getStates());
						}
					}else if(dispatcher.isEntry(upPred) && CS.isEmpty()){
						if (isFirstTimeVisitedEntryInUpperClass) {
							tempStates = dispatcher.copyStates(predElement.getStates());
							isFirstTimeVisitedEntryInUpperClass = false;
						} else {
							addAll(tempStates,predElement.getStates());
						}
					}
//					visitRecords.add(new VisitRecord(predElement.getUnitPlus().getNumber(), predElement.getUnitPlus().getAttribute(), recordStates));
				}
			}
		}
		// if work list and CS are all empty, we need to find the caller site of this method with the help of stack
		// trace
		if (CS.size() == 0 ) {
			if(indexOfStackTrace < stackTrace.length - 1){
				indexOfStackTrace++;
				UnitPlus callSite = dispatcher.getStackTraceCallSiteOfMethod(initializeElement.getUnitPlus().getMethodPlus(),
						stackTrace, indexOfStackTrace);
				System.out.println("NumberOfCallSite: " + indexOfStackTrace + "    Called Method: "
						+ initializeElement.getUnitPlus().getMethodPlus() + "    CallSite:  " + callSite);
				Set<State> outgoingStates = mapAtEntryOfMethod(tempStates, initializeElement.getUnitPlus().getMethodPlus(), callSite);
				Element callSiteElement = new Element(callSite, outgoingStates);
				analyzeMethod(callSiteElement);
			}else {
//				if(!PossibleNPAs.contains(dispatcher.getEntryUnitPlus(presentElement.getUnitPlus().getMethodPlus()))){
//					PossibleNPAs.add(dispatcher.getEntryUnitPlus(presentElement.getUnitPlus().getMethodPlus()));
//					System.out.println("PossibleNPA:  " + dispatcher.getEntryUnitPlus(presentElement.getUnitPlus().getMethodPlus()));
					System.out.println("PossibleNPA States:  " +tempStates);
//				}
			}
		}
	}
	
	private void addAll(Set<State> statesToBeAdded, Set<State> statesToAdd){
		for(State state:statesToAdd){
			boolean contains = false;
			for(State state2:statesToBeAdded){
				if(state.equalTo(state2)){
					contains = true;
				}
			}
			if(!contains){
				statesToBeAdded.add(state);
			}
		}
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
						state.setAttribute("return value");
						state.setReturnInMethodPlus(calledMethodPlus);
//						System.out.println("State: "+state);
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
		for (State state : states) {
			if (state.getValue() instanceof ParameterRef) {
//				System.out.println("ParameterRef: "+state.getValue());
				ParameterRef parameterRef = (ParameterRef) state.getValue();
				int index = parameterRef.getIndex();
				if ((upPred.getUnit() instanceof JInvokeStmt)&&state.getmethod().equals(calledMethodPlus)) {
					JInvokeStmt jInvokeStmt = (JInvokeStmt) upPred.getUnit();
					InvokeExpr invokeExpr = jInvokeStmt.getInvokeExpr();
					List<Value> args = invokeExpr.getArgs();
					if (args.get(index) instanceof NullConstant) {
						increaseNPA(upPred);
						removeStates.add(state);
					} else {
						state.replaceValue(args.get(index), upPred);
					}
				} else if ((upPred.getUnit() instanceof AbstractDefinitionStmt)&&state.getmethod().equals(calledMethodPlus)) {
					AbstractDefinitionStmt abstractDefinitionStmt = (AbstractDefinitionStmt) upPred.getUnit();
					Value rightValue = abstractDefinitionStmt.getRightOp();
					if (rightValue instanceof InvokeExpr) {
						InvokeExpr invokeExpr = (InvokeExpr) rightValue;
						List<Value> args = invokeExpr.getArgs();
						if (args.get(index) instanceof NullConstant) {
							increaseNPA(upPred);
						} else {
							state.replaceValue(args.get(index), upPred);
						}
					}
				}
			} 
			else if (state.getValue() instanceof ThisRef) {
//				 System.out.println("ThisRef: "+state.getValue());
				if (upPred.getUnit() instanceof JInvokeStmt) {
					JInvokeStmt jInvokeStmt = (JInvokeStmt) upPred.getUnit();
					InvokeExpr invokeExpr = jInvokeStmt.getInvokeExpr();
					if (invokeExpr instanceof InstanceInvokeExpr) {
						InstanceInvokeExpr instanceInvokeExpr = (InstanceInvokeExpr) invokeExpr;
						state.replaceValue(instanceInvokeExpr.getBase(), upPred);
					}
				} else if (upPred.getUnit() instanceof AbstractDefinitionStmt) {
					AbstractDefinitionStmt abstractDefinitionStmt = (AbstractDefinitionStmt) upPred.getUnit();
					Value rightValue = abstractDefinitionStmt.getRightOp();
					if (rightValue instanceof InvokeExpr) {
						InvokeExpr invokeExpr = (InvokeExpr) rightValue;
						if (invokeExpr instanceof InstanceInvokeExpr) {
							InstanceInvokeExpr instanceInvokeExpr = (InstanceInvokeExpr) invokeExpr;
							state.replaceValue(instanceInvokeExpr.getBase(), upPred);
						}
					}
				}
			}
		}
		for (State state : removeStates) {
			states.remove(state);
		}
		return states;
	}

	private Set<State> mapAtCall(Set<State> states, UnitPlus upPred, UnitPlus exitnode) {
		// if the unit is a JInvokeStmt
//		Set<State> removeStates = new HashSet<>();
		if (upPred.getUnit() instanceof JInvokeStmt) {
			MethodPlus calledMethodPlus = exitnode.getMethodPlus();
			JInvokeStmt jInvokeStmt = (JInvokeStmt) upPred.getUnit();
			List<Value> args = jInvokeStmt.getInvokeExpr().getArgs();			
			for(int index = 0;index<args.size();index++){
				for(State state:states){
					if(state.equalValue(args.get(index), upPred.getMethodPlus())){
						state.replaceValue(calledMethodPlus.getBody().getParameterLocal(index), exitnode);
					}
				}
			}
		}
		// if the unit is definition statement and right value is invoke expression
		else if (upPred.getUnit() instanceof AbstractDefinitionStmt) {
			AbstractDefinitionStmt abstractDefinitionStmt = (AbstractDefinitionStmt) upPred.getUnit();
			Value rightValue = abstractDefinitionStmt.getRightOp();
//			Value leftValue = abstractDefinitionStmt.getLeftOp();
			if (rightValue instanceof InvokeExpr) {
				// check if used values in the invoke experssion are states
				MethodPlus calledMethodPlus = exitnode.getMethodPlus();
				InvokeExpr invokeExpr = (InvokeExpr) rightValue;
				List<Value> args = invokeExpr.getArgs();
				for(int index = 0;index<args.size();index++){
					for(State state:states){
						if(state.equalValue(args.get(index), upPred.getMethodPlus())){
							state.replaceValue(calledMethodPlus.getBody().getParameterLocal(index), exitnode);
						}
					}
				}
			}
		}
		return states;
	}

	public Set<UnitPlus> getNPA() {
		return NPA;
	}

	private void worklistPush(Element element, Stack<Element> worklist, Set<VisitRecord> visitRecords, boolean noNeedToCheck){
		if(element.getStates().size()>0){
			if(noNeedToCheck){
				worklist.push(element);
			}else{
				boolean isElementAnalyzed = false;
				for (VisitRecord visitRecord: visitRecords) {
					if (visitRecord.elementVisited(element)) {
						isElementAnalyzed = true;
						break;
					}
				}
				if(!isElementAnalyzed){
					worklist.push(element);
				}
			}
		}
	}
	private void increaseNPA(UnitPlus unitPlus) {
		if(!NPA.contains(unitPlus)){
			NPA.add(unitPlus);
			System.out.println("NPA:\t"+unitPlus);
		}
	}
	
	public Set<UnitPlus> getPossibleNPAs() {
		return PossibleNPAs;
	}

}

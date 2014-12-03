package analysis;

import internal.Element;
import internal.MethodPlus;
import internal.State;
import internal.UnitPlus;

import java.io.FileNotFoundException;
import java.util.*;

import java_cup.lalr_item;
import soot.Body;
import soot.Local;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.coffi.parameter_annotation;
import soot.jimple.InvokeExpr;
import soot.jimple.ReturnStmt;
import soot.jimple.internal.AbstractDefinitionStmt;
import soot.jimple.internal.AbstractInvokeExpr;
import soot.jimple.internal.JInvokeStmt;
import soot.toolkits.graph.UnitGraphPlus;
import test.Analysis;
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

	// private Map<MethodPlus, UnitGraphPlus> methodToUnitGraphPlusMap;
	private ArrayList<UnitPlus> NPA;
	private Stack<MethodPlus> CS;
	private Dispatcher dispatcher;
	private Summary summary;
	private Set<Element> elementSet;
	private int indexOfStackTrace;
	private StackTraceElement[] stackTrace;
	private List<State> transitStates;

	/**
	 * constructor
	 * @param dispatcher
	 * @param stackTrace
	 */
	public ComputeNPA(Dispatcher dispatcher, StackTraceElement[] stackTrace) {
		NPA = new ArrayList<>();
		this.dispatcher = dispatcher;
		// methodToUnitGraphPlusMap = dispatcher.getMethodToUnitGraphPlus();
		CS = new Stack<>();
		summary = new Summary();
		elementSet = new HashSet<>();
		indexOfStackTrace = 0;
		this.stackTrace = stackTrace;
	}

	/**
	 * reset the index of stack trace
	 */
	public void resetIndexOfStarckTrace() {
		indexOfStackTrace = 0;
	}

	/**
	 * analyze the method
	 * @param unitPlus
	 * @param states
	 * @throws ClassNotFoundException
	 * @throws FileNotFoundException
	 */
	public void analyzeMethod(Element initializeElement)
			throws ClassNotFoundException, FileNotFoundException {
		// the list to work on
		Stack<Element> worklist = new Stack<Element>();
		elementSet.add(initializeElement);
		worklist.push(initializeElement);
		// if work list is empty, get the present states
		List<State> tempStates = initializeElement.getStates();
		boolean isFirstTimeVisitedEntry = true;
		while (!worklist.isEmpty()) {
			Element presentElement = worklist.pop();
			tempStates = presentElement.getStates();
			System.out.println("presentElement: " + presentElement);
			presentElement.setVisited();
			List<UnitPlus> preds = dispatcher.getPredecessors(presentElement
					.getUnitPlus());
			for (UnitPlus upPred : preds) {
				List<State> presentStates = presentElement.getStates();
				// to avoid one element visited twice which is caused by create same elements twice
				boolean isElementCreated = false;
				for (Element element : elementSet) {
					if (element.getUnitPlus().equals(upPred)
							&& element.getStates().equals(presentStates)) {
						isElementCreated = true;
					}
				}
				
				if (!isElementCreated) {
					Element predElement = new Element(upPred, presentStates);
					elementSet.add(predElement);
					// if the element is definition statement
					if (dispatcher.isTransform(upPred)) {
						Element newElement = predElement;
						if (predElement.isPredicate()) {
							NPA.add(upPred);
							System.out.println("NPA:  " + upPred);
						} 
						// if it is only a transform element
						else {
							predElement.transform();
							if (!predElement.isVisited()) {
								worklist.push(predElement);
							}
						}
					} else if (dispatcher.isCall(upPred)) {
						List<State> outgoingStates = summary
								.getInformation(predElement);
						if (outgoingStates == null) {
							MethodPlus methodPlus = null;
							for (UnitPlus upPredPred : dispatcher
									.getPredecessors(upPred)) {
								// upPred is a caller b so it has only one predecessor
								methodPlus = upPredPred.getMethodPlus();
								CS.push(methodPlus);
								System.out.println("Pushed Methods: " + methodPlus);
								UnitPlus exitnode = dispatcher
										.getExitUnitPlus(methodPlus);
								UnitPlus entrynode = dispatcher
										.getEntryUnitPlus(methodPlus);
								outgoingStates = mapAtCall(presentStates, upPred,
										exitnode);
								// states before going into a deeper method
								transitStates = outgoingStates;
								Element exitElement = new Element(exitnode, outgoingStates);
								analyzeMethod(exitElement);
								methodPlus = CS.pop();
								System.out.println("Poped Methods: " + methodPlus);
								// System.out.println(upPred);
//								System.out.println(transitStates.get(0));
								presentStates = transitStates;
								outgoingStates = mapAtEntryOfMethod(presentStates,
										entrynode, upPred);
								//after analyze the call method, the predElement changes to the caller a 
								predElement.setUnitPlus(dispatcher.getCallSitePred(upPred));
								summary.setInformation(methodPlus, presentStates,
										outgoingStates);
							}
						}
						// set the predElement 
						// if method has not been analyzed, then the outgoing states are the new analyzed states
						// if the method has been analyzed, then the outgoing states are the states in the summary
						if (!predElement.isVisited()) {
							predElement.setStates(outgoingStates);
							worklist.push(predElement);
						}
					} 				
					//if upPred is a normal statement push it and go on
					else if (!dispatcher.isEntry(upPred)) {
						if (upPred.getUnit() instanceof ReturnStmt){
							ReturnStmt returnStmt = (ReturnStmt) upPred.getUnit();
							Value returnValue = returnStmt.getOp();
							for(State state:presentStates){
								if(state.getAttribute().equals("return value")){
									if (returnValue.toString().equals("null")) {
										System.out.println("NPA:  " + upPred);
										NPA.add(upPred);
									}
									// it not null, add them to states
									else {
										state.replaceValue(returnValue);
										if (!predElement.isVisited()){
											worklist.push(predElement);
										}
									}
									
								}
							}
						}else{
							if (!predElement.isVisited()){
								worklist.push(predElement);
							}
						}
					}
					// if it is the entry of method and CS is not empty,
					// it means that it needs to go to upper method and 
					// the states need to be preserved and that is where
					// we need the transitStates
					else if (dispatcher.isEntry(upPred) && !CS.isEmpty()) {
						if(isFirstTimeVisitedEntry){
							transitStates = presentStates;
							isFirstTimeVisitedEntry =false;
							System.out.println("Transit: " + transitStates.size());
						}else {
							transitStates.addAll(presentStates);
							System.out.println("NotFirstTime Transit: " + transitStates.size());
						}
					}
				}

			}
		}
		System.out.println("worklist is empty");
		// if work list and CS are all empty, we need to find the caller site of this method
		// with the help of stack trace
		if (CS.size() == 0 && indexOfStackTrace < stackTrace.length - 1
//				&& initializeElement.getUnitPlus() != null
				) {
			indexOfStackTrace++;
			UnitPlus callSite = dispatcher.getStackTraceCallSiteOfMethod(
					initializeElement.getUnitPlus().getMethodPlus(), stackTrace, indexOfStackTrace);
			System.out.println("Number: " + indexOfStackTrace
					+ "    Unitplus: " + initializeElement.getUnitPlus() + "    CallSite:  "
					+ callSite);
			List<State> outgoingStates = mapAtEntryOfMethod(tempStates,
					initializeElement.getUnitPlus(), callSite);
			Element callElement = new Element(callSite, outgoingStates);
			analyzeMethod(callElement);
		}
	}

	/**
	 * map the states at the entry of a method
	 * @param states
	 * @param entrynode
	 * @param upPred
	 * @return
	 */
	private List<State> mapAtEntryOfMethod(List<State> states,
			UnitPlus entrynode, UnitPlus upPred) {
		// map all the local values in states to the used values in the unit
		// the invoke expression case does not need to worry as when mapped back,
		// the return value is the real value of the left value in the upper method
		int totalOflocals = entrynode.getMethodPlus().getSootmethod()
				.getParameterCount();
		Map<Integer, Integer> localsToStates = new HashMap<>();
		boolean callInvolvesState = false;
		for (int numberOflocals = 0; numberOflocals < totalOflocals; numberOflocals++) {
			Value localValue = (Value) entrynode.getMethodPlus()
					.getSootmethod().retrieveActiveBody()
					.getParameterLocal(numberOflocals);
			for (int numberOfStates = 0; numberOfStates < states.size(); numberOfStates++) {
				if (states.get(numberOfStates).getValue().toString().equals(localValue.toString())) {
//					System.out.println("Value Equals!!!111");
					localsToStates.put(new Integer(numberOflocals),
							new Integer(numberOfStates));
					callInvolvesState = true;
				}
			}
		}
		if (callInvolvesState) {
			JInvokeStmt jInvokeStmt = (JInvokeStmt) upPred.getUnit();
			// How the useBoxes Lies
			List<ValueBox> useValueBoxs = jInvokeStmt.getUseBoxes();
			Set<Integer> keys = localsToStates.keySet();
			for (Integer localNumber : keys) {
				Value value = useValueBoxs.get(localNumber.intValue())
						.getValue();
				states.get(localsToStates.get(localNumber)).replaceValue(value);
			}
		}
		return states;
	}

	private List<State> mapAtCall(List<State> states, UnitPlus upPred,
			UnitPlus exitnode) {
		// if the unit is a JInvokeStmt
		if (upPred.getUnit() instanceof JInvokeStmt) {
			MethodPlus calledMethodPlus = exitnode.getMethodPlus();
			JInvokeStmt jInvokeStmt = (JInvokeStmt) upPred.getUnit();
			List<ValueBox> useValueBoxs = jInvokeStmt.getUseBoxes();
			boolean callInvolvesState = false;
			Map<Integer, Integer> parametersToStates = new HashMap<>();
			int totalOfUsedValues = useValueBoxs.size();
			for (int numberOfUsedValues = 0; numberOfUsedValues < totalOfUsedValues; numberOfUsedValues++) {
				Value parameterValue = useValueBoxs.get(numberOfUsedValues)
						.getValue();
				for (int numberOfStates = 0; numberOfStates < states.size(); numberOfStates++) {
					if (states.get(numberOfStates).getValue().toString()
							.equals(parameterValue.toString())) {
						System.out.println("Value Equals!!!222");
						parametersToStates.put(new Integer(numberOfUsedValues),
								new Integer(numberOfStates));
						callInvolvesState = true;
					}
				}
			}

			if (callInvolvesState) {
				System.out.println("callInvolvesState:     " + jInvokeStmt);
				Body body = calledMethodPlus.getSootmethod()
						.retrieveActiveBody();
				Set<Integer> parameterKeys = parametersToStates.keySet();
				for (Integer parameterNumber : parameterKeys) {
					Value parameterValue = (Value) body
							.getParameterLocal(parameterNumber);
					states.get(parametersToStates.get(parameterNumber))
							.replaceValue(parameterValue);
				}

			}
		} 
		// if the unit is definition statement and right value is invoke expression
		else if (upPred.getUnit() instanceof AbstractDefinitionStmt) {
			AbstractDefinitionStmt abstractDefinitionStmt = (AbstractDefinitionStmt) upPred
					.getUnit();
			Value rightValue = abstractDefinitionStmt.getRightOp();
			Value leftValue = abstractDefinitionStmt.getLeftOp();
			if (rightValue instanceof InvokeExpr) {
				
				// check if used values in the invoke experssion are states
				MethodPlus calledMethodPlus = exitnode.getMethodPlus();
				InvokeExpr invokeExpr = (InvokeExpr) rightValue;
				List<ValueBox> useValueBoxs = invokeExpr.getUseBoxes();
				boolean callInvolvesState = false;
				Map<Integer, Integer> parametersToStates = new HashMap<>();
				int totalOfUsedValues = useValueBoxs.size();
				for (int numberOfUsedValues = 0; numberOfUsedValues < totalOfUsedValues; numberOfUsedValues++) {
					Value parameterValue = useValueBoxs.get(numberOfUsedValues)
							.getValue();
					for (int numberOfStates = 0; numberOfStates < states.size(); numberOfStates++) {
						if (states.get(numberOfStates).getValue().toString()
								.equals(parameterValue.toString())) {
							parametersToStates.put(new Integer(numberOfUsedValues),
									new Integer(numberOfStates));
							callInvolvesState = true;
						}
					}
				}

				if (callInvolvesState) {
					System.out.println("callInvolvesState:     " + invokeExpr);
					Body body = calledMethodPlus.getSootmethod()
							.retrieveActiveBody();
					Set<Integer> parameterKeys = parametersToStates.keySet();
					for (Integer parameterNumber : parameterKeys) {
						Value parameterValue = (Value) body
								.getParameterLocal(parameterNumber);
						states.get(parametersToStates.get(parameterNumber))
								.replaceValue(parameterValue);
					}

				}
				
				
				// check if the return values which are left values are states				
				List<UnitPlus> returnUnitPlusList = dispatcher
						.getPredecessors(exitnode);
				for (UnitPlus returnUnitPlus : returnUnitPlusList) {
					if (returnUnitPlus.getUnit() instanceof ReturnStmt) {
						ReturnStmt returnStmt = (ReturnStmt) returnUnitPlus
								.getUnit();
						Value returnValue = returnStmt.getOp();
						for (int numberOfStates = 0; numberOfStates < states
								.size(); numberOfStates++) {
							//if states and left values are the same
							if (states.get(numberOfStates).getValue().toString()
									.equals(leftValue.toString())) {
								//Q2.5: compared to Q2, why here it is okay to get compare value without toString?
								System.out.println("Value Equals!!!333"+states.get(numberOfStates));
								// set the states as return value when hit the return statement, we can fix that
								states.get(numberOfStates).setAttribute("return value");;
								
							}
						}
					}
				}
			}
		}

		return states;
	}

	public ArrayList<UnitPlus> getNPA() {
		return NPA;
	}
}

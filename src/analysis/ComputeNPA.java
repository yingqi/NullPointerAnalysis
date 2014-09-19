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
import soot.jimple.internal.AbstractDefinitionStmt;
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

	public ComputeNPA(Analysis analysis) {
		NPA = new ArrayList<>();
		dispatcher = analysis.getDispatcher();
		// methodToUnitGraphPlusMap = dispatcher.getMethodToUnitGraphPlus();
		CS = new Stack<>();
		summary = new Summary();
		elementSet = new HashSet<>();
		indexOfStackTrace = 0;
		stackTrace = analysis.getStackTraceElements();
	}

	public void analyzeMethod(UnitPlus unitPlus, List<State> states)
			throws ClassNotFoundException, FileNotFoundException {
		Stack<Element> worklist = new Stack<Element>();
		Element initializeElement = new Element(unitPlus, states);
		elementSet.add(initializeElement);
		worklist.push(initializeElement);
		while (!worklist.isEmpty() && (unitPlus != null)) {
			Element presentElement = worklist.pop();
			System.out.println("presentElement: " + presentElement);
			presentElement.setVisited();
			List<UnitPlus> preds = dispatcher.getPredecessors(presentElement
					.getUnitPlus());
			for (UnitPlus upPred : preds) {
				List<State> presentStates = presentElement.getStates();
				boolean isElementCreated = false;
				for (Element element : elementSet) {
					if (element.getUnitPlus().equals(upPred)
							&& element.getStates().equals(presentStates)) {
						isElementCreated = true;
					}
				}
				// System.out.println("isElementCreated: "+isElementCreated );
				if (!isElementCreated) {
					Element predElement = new Element(upPred, presentStates);
					elementSet.add(predElement);
					if (dispatcher.isTransform(upPred)) {
						Element newElement = predElement;
						if (predElement.isPredicate()) {
							NPA.add(upPred);
							System.out.println("NPA:  " + upPred);
						} else {
							newElement = predElement.transform();
						}
						if (!predElement.isVisited()) {

							worklist.push(newElement);
						}
					} else if (dispatcher.isCall(upPred)) {
						List<State> outgoingStates = summary
								.getInformation(predElement);
						if (outgoingStates == null) {
							MethodPlus methodPlus = null;
							for (UnitPlus upPredPred : dispatcher
									.getPredecessors(upPred)) {
								methodPlus = upPredPred.getMethodPlus();
							}
							CS.push(methodPlus);
							System.out.println("Pushed Methods: " + methodPlus);
							UnitPlus exitnode = dispatcher
									.getExitUnitPlus(methodPlus);
							UnitPlus entrynode = dispatcher
									.getEntryUnitPlus(methodPlus);
							outgoingStates = mapAtCall(presentStates, upPred,
									exitnode);
							transitStates = outgoingStates;
							analyzeMethod(exitnode, outgoingStates);
							methodPlus = CS.pop();
							System.out.println("Poped Methods: " + methodPlus);
							// System.out.println(upPred);
							// System.out.println(transitStates.get(0));
							presentStates = transitStates;
							outgoingStates = mapAtEntryOfMethod(presentStates,
									entrynode, upPred);
							predElement.setUnitPlus(dispatcher
									.getCallSitePred(upPred));
							System.out.println(outgoingStates.get(0));
							summary.setInformation(methodPlus, presentStates,
									outgoingStates);
						}
						// System.out.println(predElement.isVisited());
						// System.out.println(worklist.isEmpty());
						if (!predElement.isVisited()){
							predElement.setStates(outgoingStates);
							worklist.push(predElement);						
						}

						// System.out.println(worklist.isEmpty());
					} else if (!dispatcher.isEntry(upPred)) {
						if (!predElement.isVisited())
							worklist.push(predElement);
					} else if (dispatcher.isEntry(upPred) && !CS.isEmpty()) {
						transitStates = presentStates;
						System.out.println("Transit: " + transitStates.get(0));
					}
				}

			}
		}
		if (CS.size() == 0 && indexOfStackTrace < stackTrace.length - 1
				&& unitPlus != null) {
			indexOfStackTrace++;
			UnitPlus callSite = dispatcher.getStackTraceCallSiteOfMethod(
					unitPlus.getMethodPlus(), stackTrace, indexOfStackTrace);
			System.out.println("Number: " + indexOfStackTrace
					+ "    Unitplus: " + unitPlus + "    CallSite:  "
					+ callSite);
			List<State> outgoingStates = mapAtEntryOfMethod(states, unitPlus,
					callSite);
			analyzeMethod(callSite, outgoingStates);
		}
	}

	private List<State> mapAtEntryOfMethod(List<State> states,
			UnitPlus entrynode, UnitPlus upPred) {
		int totalOflocals = entrynode.getMethodPlus().getSootmethod()
				.getParameterCount();
		Map<Integer, Integer> localsToStates = new HashMap<>();
		boolean callInvolvesState = false;
		for (int numberOflocals = 0; numberOflocals < totalOflocals; numberOflocals++) {
			Value localValue = (Value) entrynode.getMethodPlus()
					.getSootmethod().retrieveActiveBody()
					.getParameterLocal(numberOflocals);
			for (int numberOfStates = 0; numberOfStates < states.size(); numberOfStates++) {
				if (states.get(numberOfStates).equals(localValue)) {
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
				if (states.get(numberOfStates).equals(parameterValue)) {
					parametersToStates.put(new Integer(numberOfUsedValues),
							new Integer(numberOfStates));
					callInvolvesState = true;
				}
			}
		}

		if (callInvolvesState) {
			System.out.println("callInvolvesState:     " + jInvokeStmt);
			Body body = calledMethodPlus.getSootmethod().retrieveActiveBody();
			Set<Integer> parameterKeys = parametersToStates.keySet();
			for (Integer parameterNumber : parameterKeys) {
				Value parameterValue = (Value) body
						.getParameterLocal(parameterNumber);
				states.get(parametersToStates.get(parameterNumber))
						.replaceValue(parameterValue);
			}

		}
		return states;
	}

	public ArrayList<UnitPlus> getNPA() {
		return NPA;
	}
}

package analysis;

import internal.Element;
import internal.MethodPlus;
import internal.State;
import internal.UnitPlus;

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

	public ComputeNPA(Analysis analysis) {
		NPA = new ArrayList<>();
		dispatcher = analysis.getDispatcher();
		// methodToUnitGraphPlusMap = dispatcher.getMethodToUnitGraphPlus();
		CS = new Stack<>();
		summary = new Summary();
		elementSet = new HashSet<>();
	}

	public void analyzeMethod(UnitPlus unitPlus, State state) {
		Stack<Element> worklist = new Stack<Element>();
		Element initializeElement = new Element(unitPlus, state);
		elementSet.add(initializeElement);
		worklist.push(initializeElement);
		while (!worklist.isEmpty()) {
			Element presentElement = worklist.pop();
			presentElement.setVisited();
			List<UnitPlus> preds = dispatcher.getPredecessors(presentElement
					.getUnitPlus());
			for (UnitPlus upPred : preds) {
				State presentState = presentElement.getState();
				boolean isElementCreated = false;
				for (Element element : elementSet) {
					if (element.getUnitPlus().equals(upPred)
							&& element.getState().equals(presentState)) {
						isElementCreated = true;
					}
				}
				if (!isElementCreated) {
					Element predElement = new Element(upPred, presentState);
					elementSet.add(predElement);
					if (dispatcher.isTransform(upPred)) {
						
						if (predElement.isPredicate()) {
							NPA.add(upPred);
						} else {
							predElement.transform();
						}
						if (!predElement.isVisited()) {
							
							worklist.push(predElement);
						}
					} else if (dispatcher.isCall(upPred)) {
						State outgoingState = summary
								.getInformation(predElement);
						if (outgoingState == null) {
							MethodPlus methodPlus = null;
							for (UnitPlus upPredPred : dispatcher
									.getPredecessors(upPred)) {
								methodPlus = upPredPred.getMethodPlus();
							}
							CS.push(methodPlus);
							UnitPlus exitnode = dispatcher
									.getExitUnitPlus(methodPlus);
							UnitPlus entrynode = dispatcher
									.getEntryUnitPlus(methodPlus);
							outgoingState = mapAtCall(presentState, upPred,
									exitnode);
							analyzeMethod(exitnode, outgoingState);
							CS.pop();
							outgoingState = mapAtEntryOfMethod(presentState,
									entrynode, upPred);
							summary.setInformation(methodPlus, presentState,
									outgoingState);
						}
						if (!predElement.isVisited())
							worklist.push(predElement);
					} else if (dispatcher.isEntry(upPred)) {
						List<UnitPlus> callSites = dispatcher
								.getAllCallSites(upPred);
						for (UnitPlus callSite : callSites) {
							State outgoingState = mapAtEntryOfMethod(state,
									unitPlus, callSite);
							analyzeMethod(callSite, outgoingState);
						}
						if (!predElement.isVisited())
							worklist.push(predElement);
					}else{
						if (!predElement.isVisited())
							worklist.push(predElement);
					}
				}

			}
		}
		// What does this part do?
		// if (CS.size() == 0) {
		// List<UnitPlus> callSites = dispatcher.getAllCallSites(unitPlus);
		// for (UnitPlus callSite : callSites) {
		// State outgoingState = mapAtEntryOfMethod(state, unitPlus,
		// callSite);
		// analyzeMethod(callSite, outgoingState);
		// }
		// }
		// What does this part do?
	}

	private State mapAtEntryOfMethod(State state, UnitPlus entrynode,
			UnitPlus upPred) {
		int numberOfParameters = entrynode.getMethodPlus().getSootmethod()
				.getParameterCount();
		int i = 0;
		boolean callInvolvesState = false;
		for (; i < numberOfParameters; i++) {
			Value localValue = (Value) entrynode.getMethodPlus()
					.getSootmethod().retrieveActiveBody().getParameterLocal(i);
			if (state.getValue().equals(localValue)) {
				callInvolvesState = true;
				break;
			}
		}
		if (callInvolvesState) {
			JInvokeStmt jInvokeStmt = (JInvokeStmt) upPred.getUnit();
			List<ValueBox> useValueBoxs = jInvokeStmt.getUseBoxes();
			Value value = useValueBoxs.get(i).getValue();
			state.replaceValue(value);
		}
		return state;
	}

	private State mapAtCall(State state, UnitPlus upPred, UnitPlus exitnode) {
		MethodPlus calledMethodPlus = exitnode.getMethodPlus();
		JInvokeStmt jInvokeStmt = (JInvokeStmt) upPred.getUnit();
		List<ValueBox> useValueBoxs = jInvokeStmt.getUseBoxes();
		boolean callInvolvesState = false;
		int count = 0;
		for (ValueBox valueBox : useValueBoxs) {
			if (valueBox.getValue().equals(state.getValue())) {
				callInvolvesState = true;
				break;
			}
			count++;
		}
		if (callInvolvesState) {
			System.out.println("callInvolvesState:     " + jInvokeStmt);
			Body body = calledMethodPlus.getSootmethod().retrieveActiveBody();
			Value parameterValue = (Value) body.getParameterLocal(count);
			state.replaceValue(parameterValue);
		}
		return state;
	}

	public ArrayList<UnitPlus> getNPA() {
		return NPA;
	}
}

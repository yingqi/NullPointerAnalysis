package analysis;

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
import bean.Element;
import bean.MethodPlus;
import bean.State;
import bean.UnitPlus;
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

	
	private Map<MethodPlus, UnitGraphPlus> methodToUnitGraphPlusMap;
	private ArrayList<UnitPlus> NPA;
	private Stack<MethodPlus> CS;
	private Dispatcher dispatcher;
	private Summary summary;
	
	public ComputeNPA() {
		NPA = new ArrayList<>();
		Analysis rteAnalysis = new Analysis();
		dispatcher = rteAnalysis.getDispatcher();
		methodToUnitGraphPlusMap = dispatcher.getMethodToUnitGraphPlus();
	}


	public void analyzeMethod(UnitPlus unitPlus, State state) {
		Stack<Element> worklist = new Stack<Element>();
		Element initializeElement = new Element(unitPlus, state);
		worklist.push(initializeElement);
		while (worklist.isEmpty() != false) {
			Element presentElement = worklist.pop();// traverse the present
													// statement
			presentElement.setVisited();// mark the statement
			List<UnitPlus> preds = dispatcher.getPredecessors(presentElement
					.getUnitPlus());
			for (UnitPlus upPred : preds) {
				State presentState = presentElement.getState();
				Element predElement = new Element(upPred, presentState);
				if (dispatcher.isTransform(upPred)) {
					predElement.transform();
					if (predElement.isPredicate()) {
						NPA.add(upPred);
					}
				} else if (dispatcher.isCall(upPred)) {
					State outgoingState = summary.getInformation(predElement);
					if (outgoingState == null) {
						MethodPlus methodPlus = upPred.getMethodPlus();
						CS.push(methodPlus);
						UnitPlus exitnode = dispatcher
								.getExitUnitPlus(methodPlus);
						UnitPlus entrynode = dispatcher
								.getEntryUnitPlus(methodPlus);
						outgoingState = mapAtCall(presentState, upPred, exitnode);
						analyzeMethod(exitnode, outgoingState);
						CS.pop();
						outgoingState = mapAtEntryOfMethod(presentState, entrynode,upPred);
						summary.setInformation(methodPlus, presentState,
								outgoingState);
					}
				}
				if (!predElement.isVisited())
					worklist.push(predElement);
			}
		}
		if (CS.size() == 0) {
			List<UnitPlus> callSites = dispatcher.getAllCallSites(unitPlus);
			for (UnitPlus callSite : callSites) {
				State outgoingState = mapAtEntryOfMethod(state, unitPlus,callSite);
				analyzeMethod(callSite, outgoingState);

			}
		}
	}

	private State mapAtEntryOfMethod(State state, UnitPlus entrynode,
			UnitPlus upPred) {
		int numberOfParameters = entrynode.getMethodPlus().getSootmethod().getParameterCount();
		int i=0;
		for(;i<numberOfParameters;i++){
			Value localValue = (Value) entrynode.getMethodPlus().getSootmethod().retrieveActiveBody().getParameterLocal(i);
			if(state.getValue().equals(localValue)){
				break;
			}
		}
		JInvokeStmt jInvokeStmt = (JInvokeStmt) upPred.getUnit();
		List<ValueBox> useValueBoxs = jInvokeStmt.getUseBoxes();
		Value value = useValueBoxs.get(i).getValue();
		state.replaceValue(value);
		return state;
	}

	private State mapAtCall(State state,UnitPlus upPred, UnitPlus exitnode)
	{
		MethodPlus calledMethodPlus = exitnode.getMethodPlus();
		JInvokeStmt jInvokeStmt = (JInvokeStmt) upPred.getUnit();
		List<ValueBox> useValueBoxs = jInvokeStmt.getUseBoxes();
		int count = 0;
		for(ValueBox valueBox:useValueBoxs){
			if(valueBox.getValue().equals(state.getValue())){
				break;
			}
			count++;
		}
		UnitGraphPlus unitGraphPlus = methodToUnitGraphPlusMap.get(calledMethodPlus);
		Body body = calledMethodPlus.getSootmethod().retrieveActiveBody();
		Value parameterValue  =(Value) body.getParameterLocal(count);
		state.replaceValue(parameterValue);;
		return state;
	}

	public ArrayList<UnitPlus> getNPA(){
		return NPA;
	}
}

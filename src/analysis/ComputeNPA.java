package analysis;

import java.util.*;

import java_cup.lalr_item;
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
					// what is summary?
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
		String param = "";
		UnitGraphPlus unitGraphPlus = methodToUnitGraphPlusMap.get(entrynode.getMethodPlus());
		Unit head = unitGraphPlus.getHead();
		while(!unitGraphPlus.getSuccsOf(head).equals(unitGraphPlus.getTail())){
			List<Unit> units = unitGraphPlus.getSuccsOf(head);
			for(Unit unit:units){
				if(unit instanceof AbstractDefinitionStmt){
					AbstractDefinitionStmt abstractDefinitionStmt = (AbstractDefinitionStmt) unit;
					Value leftValue = abstractDefinitionStmt.getLeftOp();
					if(leftValue.equals(state.getValue())){
						param = abstractDefinitionStmt.getRightOp().toString();
					}
				}
			}
		}
		int count = Integer.parseInt(param.substring(9, 9));
		JInvokeStmt jInvokeStmt = (JInvokeStmt) upPred.getUnit();
		List<ValueBox> useValueBoxs = jInvokeStmt.getUseBoxes();
		Value value = useValueBoxs.get(count).getValue();
		state.replaceValue(value);
		return state;
	}

	private State mapAtCall(State state,UnitPlus upPred, UnitPlus exitnode)
	{
//		MethodPlus startMP = startUP.getMethodPlus();
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
		Unit head = unitGraphPlus.getHead();
		while(!unitGraphPlus.getSuccsOf(head).equals(unitGraphPlus.getTail())){
			List<Unit> units = unitGraphPlus.getSuccsOf(head);
			for(Unit unit:units){
				if(unit instanceof AbstractDefinitionStmt){
					AbstractDefinitionStmt abstractDefinitionStmt = (AbstractDefinitionStmt) unit;
					Value rightValue = abstractDefinitionStmt.getRightOp();
					if(rightValue.toString().contains("@parameter"+count)){
						state.replaceValue(abstractDefinitionStmt.getLeftOp());
					}
				}
			}
		}
		return state;
	}
	
	private Map<MethodPlus, UnitGraphPlus> methodToUnitGraphPlusMap;
	private ArrayList<UnitPlus> NPA;
	private Stack<MethodPlus> CS;
	private Dispatcher dispatcher;
	private Summary summary;
}

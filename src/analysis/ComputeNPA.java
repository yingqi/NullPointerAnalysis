package analysis;

import java.util.*;

<<<<<<< HEAD
=======
import soot.Unit;
>>>>>>> origin/master
import test.RTEAnalysis;
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
 * @author Lind
 *
 */
public class ComputeNPA {
	
	public ComputeNPA()
	{
		NPA=new ArrayList<>();
		RTEAnalysis rteAnalysis = new RTEAnalysis();
		dispatcher = rteAnalysis.getDispatcher();
//		i=0;
		
	}
//	public static void main(String[] args)
//	{
//		Statement statement;
//		
//	}
	
<<<<<<< HEAD
	public void  analyzeMethod(UnitPlus unitPlus,State state)
=======
	public void analyzeMethod(UnitPlus unitPlus,State state)
>>>>>>> origin/master
	{
		Stack<Element> worklist=new Stack<Element>();
		Element initializeElement=new Element(unitPlus,state);
		worklist.push(initializeElement);
		while(worklist.isEmpty()!=false)
		{
			Element presentElement=worklist.pop();//traverse the present statement
			presentElement.setVisited();//mark the statement
			List<UnitPlus> preds=dispatcher.getPredecessors(presentElement.getUnitPlus());
			for(UnitPlus up:preds)
			{
				State presentState=presentElement.getState();
				Element predElement=new Element(up,presentState);
				if(dispatcher.isCall(up)&&dispatcher.isEntry(up))
				{
//					if(up.isTransform())
//					{
						predElement.transform();//how to transform???????
						if(predElement.isPredicate())
						{
							NPA.add(up);
						}
//					}
				}
				else if(dispatcher.isCall(up))
				{
					State outgoingState=summary.getInformation(predElement);
					if(presentState==null)
					{
						MethodPlus methodPlus=up.getMethodPlus();
						CS.push(methodPlus);
						for(UnitPlus exitnode:dispatcher.getExitUnitPlus(methodPlus)){
							outgoingState=map(presentState,exitnode);//map transform the incoming state to ougoing state
							analyzeMethod(exitnode,outgoingState);
						}
						//the outgoing state from the first node of the MethodPlus was returned by analyzeMethodPlus
						CS.pop();
<<<<<<< HEAD
						outgoingState = map(presentState, unitPlus);
=======
>>>>>>> origin/master
						summary.setInformation(methodPlus,presentState,outgoingState);
					}
				}
				if(!predElement.isVisited())	worklist.push(predElement);
//				if(up.isEntry())	return presentState;//whether using return would result in a bug?????
			}
		}
		if(CS.size()==0)
		{
			List<UnitPlus> callSites=dispatcher.getAllCallSites(unitPlus);
			for(UnitPlus callSite:callSites)
			{
				State outgoingState=map(state, callSite);
				analyzeMethod(callSite,outgoingState);
				
			}
		}
	}
	
	private State map(State incomingState,UnitPlus unitPlus)//how to map??????
	{
		//TO DO
		return incomingState;
	}
	private ArrayList<UnitPlus> NPA;
//	private final int DEFAULT_CAPACITY=5;
	private Stack<MethodPlus> CS;
	private Dispatcher dispatcher;
	private Summary summary;
}







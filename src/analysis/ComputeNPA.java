package analysis;

import java.util.*;

import soot.Unit;
import bean.Element;
import bean.MethodPlus;
import bean.State;
import bean.UnitPlus;
import dispatcher.Dispatcher;
import dispatcher.DispatcherFactory;

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
		dispatcher = RTE
//		i=0;
		
	}
//	public static void main(String[] args)
//	{
//		Statement statement;
//		
//	}
	
	public State analyzeMethod(UnitPlus unitPlus,State state)
	{
		Stack<Element> worklist=new Stack<Element>();
		Element initializeElement=new Element(unitPlus,state);
		worklist.push(initializeElement);
		while(worklist.isEmpty()!=false)
		{
			Element presentElement=worklist.pop();//traverse the present statement
			presentElement.setVisited();//mark the statement
			ArrayList<UnitPlus> preds=(ArrayList<UnitPlus>) Dispatcher.getPredecessors(presentElement.getUnitPlus());
			for(UnitPlus up:preds)
			{
				State presentState=presentElement.getState();
				Element predElement=new Element(up,presentState);
				if(!up.isCall()&&!sp.isEntry())
				{
					if(sp.isTransform())
					{
						predElement.transform();//how to transform???????
						if(predElement.isPredicate())
						{
							NPA[i++]=sp;
						}
					}
				}
				else if(sp.isCall())
				{
					State outgoingState=summary.getInformation(predElement);
					if(presentState=State.EMPTY)
					{
						MethodPlus MethodPlus=sp.getMethodPlus();
						CS.push(MethodPlus);
						outgoingState=map(presentState,MethodPlus.getExitNode());//map transform the incoming state to ougoing state
						outgoingState=analyzeMethodPlus(MethodPlus.getExitNode(),outgoingState);
						//the outgoing state from the first node of the MethodPlus was returned by analyzeMethodPlus
						CS.pop();
					}
					summary.setInformation(MethodPlus,presentState,outgoingState);
				}
				if(!predElement.isVisited())	worklist.push(predElement);
				if(sp.isEntry())	return presentState;//whether using return would result in a bug?????
			}
		}
		if(CS.size()==0)
		{
			Statement[] cs=D.getCallSites();
			for(Statement sc:cs)
			{
				State outgoingState=map(sc,presentState);
				analyzeMethodPlus(sc,outgoingState);
				
			}
		}
	}
	
	private State map(State incomingState,Statement statement)//how to map??????
	{
		return incomingState;
	}
	private ArrayList<UnitPlus> NPA;
//	private final int DEFAULT_CAPACITY=5;
	private Stack<MethodPlus> CS;
	private Dispatcher dispatcher;
	private Summary summary;
}







package analysis;

import java.util.ArrayList;

import bean.Element;
import bean.MethodPlus;
import bean.Record;
import bean.State;
import bean.UnitPlus;

/**
 * help to map the incoming state to outgoing state
 */
/**
 * @version 2014-08-06
 * @author Lind
 *
 */
public class Summary {
	public Summary(){
		information = new ArrayList<>();
	}
	public State getInformation(Element element)
	{
		State incomingState=element.getState();
		MethodPlus methodPlus = element.getUnitPlus().getMethod();
		for(Record record:information)
		{
			if(record.compareMethod(methodPlus)&&record.compareIncomingState(incomingState))
			{
				return record.getOutgoingState();
			}
		}
		return null;
	}
	public void setInformation(MethodPlus methodPlus,State incomingState,State outgoingState)
	{	
		information.add(new Record(methodPlus,incomingState,outgoingState));
	}
	
	private ArrayList<Record> information;
}

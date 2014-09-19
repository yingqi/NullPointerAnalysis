package analysis;

import internal.Element;
import internal.MethodPlus;
import internal.Record;
import internal.State;

import java.util.ArrayList;
import java.util.List;

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
	public List<State> getInformation(Element element)
	{
		List<State> incomingStates=element.getStates();	
		MethodPlus methodPlus = element.getUnitPlus().getMethodPlus();
		for(Record record:information)
		{
			if(record.compareMethod(methodPlus)&&record.compareIncomingStates(incomingStates))
			{
				return record.getOutgoingStates();
			}
		}
		return null;
	}
	public void setInformation(MethodPlus methodPlus,List<State> incomingStates,List<State> outgoingStates)
	{	
		information.add(new Record(methodPlus,incomingStates,outgoingStates));
	}
	
	private List<Record> information;
}

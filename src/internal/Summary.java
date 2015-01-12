package internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dispatcher.LightDispatcher;

/**
 * help to map the incoming state to outgoing state
 */
/**
 * @version 2014-08-06
 * @author Lind
 *
 */
public class Summary {
	
//	private Map<MethodPlus, List<StateRecord>> informationMap;
	
	private List<Record> information;
	/**
	 * constructor
	 */
	public Summary(){
		information = new ArrayList<>();
	}
	
	/**
	 * get information
	 * @param element
	 * @return
	 */
	public Set<State> getInformation(Set<State> incomingStates, MethodPlus calledMethodPlus)
	{
		for(Record record:information)
		{
			if(record.compareMethod(calledMethodPlus)&&record.compareIncomingStates(incomingStates))
			{
				return record.getOutgoingStates();
			}
		}
		return null;
	}
	
	/**
	 * set information
	 * @param methodPlus
	 * @param incomingStates
	 * @param outgoingStates
	 */
	public void setInformation(MethodPlus methodPlus,Set<State> incomingStates,Set<State> outgoingStates)
	{	
		information.add(new Record(methodPlus,incomingStates,outgoingStates));
	}
}

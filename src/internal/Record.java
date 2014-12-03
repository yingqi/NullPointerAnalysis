package internal;

import java.util.List;

/**
 * Record is an internal class for whether the method is visited record.
 * @author leo
 *
 */
public class Record {
	
	/**
	 * constructor
	 * @param methodPlus
	 * @param incomingStates
	 * @param outgoingStates
	 */
	public Record(MethodPlus methodPlus,List<State> incomingStates,List<State> outgoingStates){
		this.methodPlus = methodPlus;
		this.incomingStates = incomingStates;
		this.outgoingStates = outgoingStates;
	}
	
	/**
	 * compare method whether they are the same
	 * @param methodPlus
	 * @return
	 */
	public boolean compareMethod(MethodPlus methodPlus) {
		//Q3: Why toString?
		//Assume: connected with sootmethod
		return methodPlus.toString().equals(methodPlus.toString());
	}

	/**
	 * compare states 
	 * @param states
	 * @return
	 */
	public boolean compareIncomingStates(List<State> states) {
		boolean statesEquals =  incomingStates.size()==states.size();
		if(statesEquals){
			for(int i=0;i<states.size();i++){
				if(!states.get(i).equals(incomingStates.get(i))){
					statesEquals =false;
				}
			}
		}
		return statesEquals;
	}

	private MethodPlus methodPlus;
	private List<State> incomingStates;
	private List<State> outgoingStates;

	public List<State> getOutgoingStates() {
		return outgoingStates;
	}
	
	@Override
	public String toString(){
		String toString = "Method: "+methodPlus+"\tIncommingStates: ";
		for(State state:incomingStates){
			toString+=state;
		}
		toString+="\tOutgoingStates: ";
		for(State state:outgoingStates){
			toString+=state;
		}
		return toString;
	}
	
	@Override
	public boolean equals(Object object){
		if(! (object instanceof Record)){
			return false;
		}else {
			Record record = (Record) object;
			boolean equals =  this.compareIncomingStates(record.getIncomingStates())&&this.getMethodPlus().equals(record.getMethodPlus());
			equals =  outgoingStates.size()==record.getOutgoingStates().size();
			if(equals){
				for(int i=0;i<outgoingStates.size();i++){
					if(!outgoingStates.get(i).equals(incomingStates.get(i))){
						equals =false;
					}
				}
			}
			return equals;
		}
	}
	
	public MethodPlus getMethodPlus() {
		return methodPlus;
	}
	public void setMethodPlus(MethodPlus methodPlus) {
		this.methodPlus = methodPlus;
	}
	public List<State>  getIncomingStates() {
		return incomingStates;
	}
	public void setIncomingStates(List<State> incomingStates) {
		this.incomingStates = incomingStates;
	}
	public void setOutgoingStates(List<State>  outgoingStates) {
		this.outgoingStates = outgoingStates;
	}
}

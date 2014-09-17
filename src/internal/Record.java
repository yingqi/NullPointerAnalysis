package internal;

public class Record {
	public Record(MethodPlus methodPlus, State incomingState, State outgoingState){
		this.methodPlus = methodPlus;
		this.incomingState = incomingState;
		this.outgoingState = outgoingState;
	}
	public boolean compareMethod(MethodPlus methodPlus) {
		return methodPlus.toString().equals(methodPlus.toString());
	}

	public boolean compareIncomingState(State state) {
		return incomingState.equals(state);
	}

	private MethodPlus methodPlus;
	private State incomingState;
	private State outgoingState;

	public State getOutgoingState() {
		return outgoingState;
	}
	
	@Override
	public String toString(){
		return "Method: "+methodPlus+"\tIncommingState: "+incomingState+"\tOutGoingState: "+outgoingState;
	}
	
	@Override
	public boolean equals(Object object){
		if(! (object instanceof Record)){
			return false;
		}else {
			Record record = (Record) object;
			return this.getIncomingState().equals(record.getIncomingState())&&this.getOutgoingState().equals(record.getOutgoingState())&&this.getMethodPlus().equals(record.getMethodPlus());
		}
	}
	public MethodPlus getMethodPlus() {
		return methodPlus;
	}
	public void setMethodPlus(MethodPlus methodPlus) {
		this.methodPlus = methodPlus;
	}
	public State getIncomingState() {
		return incomingState;
	}
	public void setIncomingState(State incomingState) {
		this.incomingState = incomingState;
	}
	public void setOutgoingState(State outgoingState) {
		this.outgoingState = outgoingState;
	}
}

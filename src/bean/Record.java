package bean;

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
		// TODO Auto-generated method stub
		return outgoingState;
	}
}

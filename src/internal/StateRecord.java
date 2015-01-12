package internal;

public class StateRecord {
	private State inState;
	private State outState;
	
	public StateRecord(State inState, State outState){
		this.inState = inState;
		this.outState = outState;
	}
}

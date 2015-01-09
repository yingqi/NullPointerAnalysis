package internal;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dispatcher.LightDispatcher;

/**
 * Record is an internal class for whether the method is visited record.
 * 
 * @author leo
 * 
 */
public class Record {

	/**
	 * constructor
	 * 
	 * @param methodPlus
	 * @param incomingStates
	 * @param outgoingStates
	 */
	public Record(MethodPlus methodPlus, Set<State> incomingStates, Set<State> outgoingStates) {
		this.methodPlus = methodPlus;
		this.incomingStates = new HashSet<>();
		this.outgoingStates = new HashSet<>();
		for (State state : incomingStates) {
			this.incomingStates.add(new State(state));
		}
		for (State state : outgoingStates) {
			this.outgoingStates.add(new State(state));
		}
	}

	/**
	 * compare method whether they are the same
	 * 
	 * @param methodPlus
	 * @return
	 */
	public boolean compareMethod(MethodPlus methodPlus) {
		return this.methodPlus.equals(methodPlus);
	}

	/**
	 * compare states
	 * 
	 * @param states
	 * @return
	 */
	public boolean compareIncomingStates(Set<State> states) {
		boolean statesEquals = incomingStates.size() == states.size();
		if (statesEquals) {
			for (State state : incomingStates) {
				boolean stateInStates = false;
				for (State state2 : states) {
					if (state2.equalTo(state)) {
						stateInStates = true;
						break;
					}
				}
				if (!stateInStates) {
					statesEquals = false;
					break;
				}
			}
			if (statesEquals) {
				for (State state : states) {
					boolean stateInIncomingStates = false;
					for (State state2 : incomingStates) {
						if (state2.equalTo(state)) {
							stateInIncomingStates = true;
							break;
						}
					}
					if (!stateInIncomingStates) {
						statesEquals = false;
						break;
					}
				}
			}
		}
		return statesEquals;
	}

	private MethodPlus methodPlus;
	private Set<State> incomingStates;
	private Set<State> outgoingStates;

	public Set<State> getOutgoingStates() {
		return outgoingStates;
	}

	@Override
	public String toString() {
		String toString = "Method: " + methodPlus + "\tIncommingStates: ";
		for (State state : incomingStates) {
			toString += state;
		}
		toString += "\tOutgoingStates: ";
		for (State state : outgoingStates) {
			toString += state;
		}
		return toString;
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof Record)) {
			return false;
		} else {
			Record record = (Record) object;
			boolean equals = this.compareIncomingStates(record.getIncomingStates())
					&& this.getMethodPlus().equals(record.getMethodPlus());
			equals = outgoingStates.size() == record.getOutgoingStates().size();
			if (equals) {
				for (State state : outgoingStates) {
					boolean isStateInRecord = false;
					for (State stateInRecord : record.getOutgoingStates()) {
						if (state.equalTo(stateInRecord)) {
							isStateInRecord = true;
							break;
						}
					}
					if (!isStateInRecord) {
						equals = false;
					}
				}
				if (equals) {
					for (State state : record.getOutgoingStates()) {
						boolean isStateInOutStates = false;
						for (State stateInOutStates : outgoingStates) {
							if (state.equalTo(stateInOutStates)) {
								isStateInOutStates = true;
								break;
							}
						}
						if (!isStateInOutStates) {
							equals = false;
						}
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

	public Set<State> getIncomingStates() {
		return incomingStates;
	}

	public void setIncomingStates(Set<State> incomingStates) {
		this.incomingStates = incomingStates;
	}

	public void setOutgoingStates(Set<State> outgoingStates) {
		this.outgoingStates = outgoingStates;
	}
}

package internal;

import java.util.List;
import java.util.Set;

public class VisitRecord {
	private int unitNumber;
	private String unitAttribute;
	private Set<State> values;

	public int getUnitNumber() {
		return unitNumber;
	}

	public void setUnitNumber(int unitNumber) {
		this.unitNumber = unitNumber;
	}

	public String getUnitAttribute() {
		return unitAttribute;
	}

	public void setUnitAttribute(String unitAttribute) {
		this.unitAttribute = unitAttribute;
	}

	public Set<State> getValues() {
		return values;
	}

	public void setValues(Set<State> values) {
		this.values = values;
	}

	public VisitRecord(int unitNumber, String unitAttribute, Set<State> values) {
		super();
		this.unitNumber = unitNumber;
		this.unitAttribute = unitAttribute;
		this.values = values;
	}

	public boolean elementVisited(Element element) {
		boolean elementVisited = visited(element.getUnitPlus(), element.getStates());
		return elementVisited;
	}

	public boolean visited(UnitPlus unitPlus, Set<State> states) {
		boolean visited = unitPlus.getNumber() == unitNumber && unitPlus.getAttribute().equals(unitAttribute)
				&& values.size() == states.size();
		if (visited) {
			//all the states are in the values
			for (State state : states) {
				boolean isStateInValue = false;
				for (State stateInValue : values) {
					if (state.equalTo(stateInValue)) {
						isStateInValue = true;
						break;
					}
				}
				if (!isStateInValue) {
					visited = false;
					break;
				}
			}
		}
		return visited;
	}

	@Override
	public boolean equals(Object object) {
		boolean equals = false;
		if (!(object instanceof VisitRecord)) {

		} else {
			VisitRecord visitRecord = (VisitRecord) object;
			equals = visitRecord.getUnitNumber() == unitNumber && visitRecord.getUnitAttribute().equals(unitAttribute)
					&& values.size() == visitRecord.getValues().size();
			if (equals) {
				for (State state : values) {
					boolean isStateInElement = false;
					for (State stateInElement : visitRecord.getValues()) {
						if (state.equalTo(stateInElement)) {
							isStateInElement = true;
							break;
						}
					}
					if (!isStateInElement) {
						equals = false;
						break;
					}
				}
				if (equals) {
					for (State state : visitRecord.getValues()) {
						boolean isStateInValue = false;
						for (State stateInValue : values) {
							if (state.equalTo(stateInValue)) {
								isStateInValue = true;
								break;
							}
						}
						if (!isStateInValue) {
							equals = false;
							break;
						}
					}
				}
			}
		}
		return equals;
	}

}

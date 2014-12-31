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
	
	public boolean elementVisited(Element element){
		boolean elementVisited = element.getUnitPlus().getNumber()==unitNumber
				&&element.getUnitPlus().getAttribute().equals(unitAttribute);
		if(elementVisited){
			for(State state:values){
				boolean isStateInElement = false;
				for(State stateInElement:element.getStates()){
					if(state.equalTo(stateInElement)){
						isStateInElement = true;
						break;
					}
				}
				if(!isStateInElement){
					elementVisited = false;
					break;
				}
			}
			if(elementVisited){
				for(State state:element.getStates()){
					boolean isStateInValue = false;
					for(State stateInValue:values){
						if(state.equalTo(stateInValue)){
							isStateInValue = true;
							break;
						}
					}
					if(!isStateInValue){
						elementVisited = false;
						break;
					}
				}
			}
		}
		return elementVisited;
	}

}

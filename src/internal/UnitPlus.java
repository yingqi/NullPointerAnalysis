package internal;

import soot.Unit;


public class UnitPlus {
	private Integer numberInteger;
	private String attributeString;
	private Unit unit;
	private MethodPlus Method;
//	private State state;
	private boolean isVisited;
	private boolean isCall;
	private boolean isEntry;
	
	/**
	 * constructor for units which is not a caller.
	 * @param number
	 * @param unit
	 * @param Method
	 */
	public UnitPlus(int number, Unit unit,MethodPlus Method){
		numberInteger = new Integer(number);
		this.unit = unit;
		attributeString="null";
		this.Method = Method;
		this.isVisited = false;
		isCall = false;
		isEntry = false;
	}
	
	/**
	 * constructor for units which is a caller, so that we have attribute to decide which is caller a and caller b.
	 * @param number
	 * @param attributeString
	 * @param unit
	 * @param Method
	 */
	public UnitPlus(int number, String attributeString, Unit unit,MethodPlus Method){
		numberInteger = new Integer(number);
		this.attributeString = attributeString;
		this.unit = unit;
		this.Method = Method;
		this.isVisited = false;
		isCall = false;
		isEntry = false;
	}

	/**
	 * set the number
	 * @param number
	 */
	public void setNumber(int number){
		numberInteger = new Integer(number);
	}
	
	/**
	 * set the attribute
	 * @param attributeString
	 */
	public void setAttribute(String attributeString){
		this.attributeString = attributeString;
	}
	
	/**
	 * set the unit
	 * @param unit
	 */
	public void setUnit(Unit unit){
		this.unit = unit;
	}
	
	/**
	 * set the method
	 * @param Method
	 */
	public void setMethod(MethodPlus Method){
		this.Method = Method;
	}
	
	/**
	 * get the number
	 * @return number
	 */
	public int getNumber(){
		return numberInteger.intValue();
	}
	
	/**
	 * get the attribute
	 * @return attribute
	 */
	public String getAttribute(){
		return attributeString;
	}
	
	/**
	 * get the unit
	 * @return unit
	 */
	public Unit getUnit(){
		return unit;
	}
	
	/**
	 * get the method
	 * @return method
	 */
	public MethodPlus getMethodPlus(){
		return Method;
	}
	
	public boolean isVisited() {
		return isVisited;
	}

	public void setVisited(boolean isVisited) {
		this.isVisited = isVisited;
	}

	public boolean isCall() {
		return isCall;
	}

	public void setCall(boolean isCall) {
		this.isCall = isCall;
	}

	public boolean isEntry() {
		return isEntry;
	}

	public void setEntry(boolean isEntry) {
		this.isEntry = isEntry;
	}
}

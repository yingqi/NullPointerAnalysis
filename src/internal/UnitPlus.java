package internal;



import soot.Unit;

/**
 * comprehensive version of unit
 * @author leo
 *
 */
public class UnitPlus {
	private Integer numberInteger;
	private String attributeString;
	private Unit unit;
	private MethodPlus Method;
//	private State state;
	private boolean isCall;
	
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
		isCall = false;
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
		isCall = false;
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
	

	public boolean isCall() {
		return isCall;
	}

	public void setCall(boolean isCall) {
		this.isCall = isCall;
	}
	
	@Override
	public String toString(){
		String toString ="";
		if (attributeString.equals("null")) {
			//Show the units
			String methodString = String.format("%-30s", Method.toString());
			toString ="unit" + '\t' + numberInteger + '\t'
					+ methodString 
					+unit.toString();
		}else{
			String methodString = String.format("%-30s",Method.toString());
			toString = "unit" + '\t' + numberInteger + attributeString
					+ '\t' + methodString 
					+ unit.toString();
		}
		return toString;
	}
	
	@Override public boolean equals(Object object){
		if(! (object instanceof UnitPlus)){
			return false;
		}else {
			UnitPlus unitPlus = (UnitPlus) object;
			return numberInteger==unitPlus.getNumber()&&attributeString.equals(unitPlus.getAttribute());
		}
	}
}

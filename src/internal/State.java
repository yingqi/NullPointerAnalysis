package internal;

import soot.Immediate;
import soot.RefType;
import soot.SootClass;
import soot.SootField;
import soot.SootFieldRef;
import soot.Value;
import soot.JastAddJ.ThisAccess;
import soot.jimple.Expr;
import soot.jimple.InstanceFieldRef;
import soot.jimple.Ref;


/**
 * This class is the state of a node
 */
/**
 * @version 2014-08-06
 * @author Yingqi
 *
 */
public class State
{
	private Value variable;
	private String attribute;
	private MethodPlus methodPlus;
	private MethodPlus returnInMethodPlus;
	private Value baseValue;
	private SootClass baseSootClass;
	private SootField field;
	
	/**
	 * constructor
	 * @param initValue
	 */
	public State(Value initValue, MethodPlus methodPlus, String attribute, MethodPlus returnInMethodPlus){
		variable = initValue;
		this.attribute = attribute;
		this.methodPlus = methodPlus;
		this.returnInMethodPlus = returnInMethodPlus;
		if (variable instanceof Ref) {
			Ref ref = (Ref) variable;
			if (ref instanceof InstanceFieldRef) {
				InstanceFieldRef instanceFieldRef = (InstanceFieldRef) ref;
				this.field = instanceFieldRef.getField();
				this.baseValue = instanceFieldRef.getBase();
				if(baseValue.getType() instanceof RefType){
					RefType refType = (RefType) baseValue.getType();
					this.baseSootClass = refType.getSootClass();
					
				}
			}
		}
	}
	
	public MethodPlus getReturnInMethodPlus() {
		return returnInMethodPlus;
	}

	public void setReturnInMethodPlus(MethodPlus returnInMethodPlus) {
		this.returnInMethodPlus = returnInMethodPlus;
	}

	/**
	 * replace the value in a state
	 * Remember to check NPA before we encounter replaceValue
	 * @param value
	 */
//	public boolean testReplaceValue(Value value, MethodPlus methodPlus){
//		boolean replace = false;
//		if(value instanceof Expr){
//		System.out.println("Error value cannot be replaced with Expr: "+value);	
//		}else {
//			replace = true;
////			variable = value;
////			this.methodPlus = methodPlus;
////			state = this;
//		}
//		return replace;
//	}
	
	public void replaceValue(Value value, UnitPlus unitPlus){
		if(value instanceof Expr){
			System.out.println("Error value cannot be replaced with Expr: "+value);	
			}else {
				System.out.println("Value Replace In: "+unitPlus);
				System.out.println("Value Replace: "+this.methodPlus+" "+variable+"\tto "+unitPlus.getMethodPlus()+" "+value);
				variable = value;
				this.methodPlus = unitPlus.getMethodPlus();	
			}
	}
	/**
	 * get the value
	 * @return
	 */
	public Value getValue(){
		return variable;
	}
	
	/**
	 * get the method
	 * @return
	 */
	public MethodPlus getmethod(){
		return methodPlus;
	}
	
	@Override
	public String toString(){
		return methodPlus.toString()+"\tValue: "+variable.toString()+" "+attribute;
	}
	
	@Override
	public boolean equals(Object object){
		return equalTo(object);
	}
	
//	public boolean equalValue(Value value, MethodPlus methodPlus){
//		boolean equalValue = false;
//		if(value instanceof Expr){
//			equalValue = value.toString().equals(variable.toString());
////			System.out.println("Expr: "+value);
//		}else {
//			equalValue = value.toString().equals(variable.toString());
//			if(equalValue){
//				System.out.println(value+"\t"+variable);
//				System.out.println(value.equals(variable));
//				System.out.println(value.equivTo(variable));
//				if (value instanceof Ref) {
////					System.out.println("Ref: "+value);
//				}else if(value instanceof Immediate){
////					System.out.println("Immediate: "+value);
//					equalValue = this.methodPlus.equals(methodPlus);
//				}
//			}
//			else {
//				if (value instanceof Ref) {
//					Ref ref = (Ref) value;
//					if (ref instanceof InstanceFieldRef) {
//						InstanceFieldRef instanceFieldRef = (InstanceFieldRef) ref;
//						instanceFieldRef.getField();
//						SootField valueField = instanceFieldRef.getField();
//						if(valueField.equals(field)){
//							Value base = instanceFieldRef.getBase();
//							if(base.getType() instanceof RefType){
//								RefType refType = (RefType) base.getType();
//								SootClass valueBaseSootClass = refType.getSootClass();
//
//								if((!valueBaseSootClass.getFields().contains(field))||(!baseSootClass.getFields().contains(valueField))){
//									equalValue=isParent(valueBaseSootClass, baseSootClass)||isParent(baseSootClass, valueBaseSootClass);
//								}
//							}
//						}
//						
//					}
//				}else if(value instanceof Immediate){
////					System.out.println("Immediate: "+value);
//				}
//			}
//		}
//		return equalValue;
//	}
	
	public boolean equalValue(Value value, MethodPlus methodPlus){
	boolean equalValue = false;
	if(value instanceof Expr){
//		equalValue = value.toString().equals(variable.toString());
		if(value.toString().equals(variable.toString())){
			System.out.println("Error Expr: "+value);
		}
		equalValue = false;
	}else if(value instanceof Immediate){
		equalValue = value.equals(variable)&&this.methodPlus.equals(methodPlus);;
	}else if(value instanceof Ref){
		equalValue = value.toString().equals(variable.toString());
		if(!equalValue){
			Ref ref = (Ref) value;
			if (ref instanceof InstanceFieldRef) {
				InstanceFieldRef instanceFieldRef = (InstanceFieldRef) ref;
				instanceFieldRef.getField();
				SootField valueField = instanceFieldRef.getField();
				if(valueField.equals(field)){
					Value base = instanceFieldRef.getBase();
					if(base.getType() instanceof RefType){
						RefType refType = (RefType) base.getType();
						SootClass valueBaseSootClass = refType.getSootClass();
						if((!valueBaseSootClass.getFields().contains(field))||(!baseSootClass.getFields().contains(valueField))){
							equalValue=isParent(valueBaseSootClass, baseSootClass)||isParent(baseSootClass, valueBaseSootClass);
						}
					}
				}
			}
		}
	}
	return equalValue;
	}
	
	
	private boolean isParent(SootClass childClass, SootClass fatherClass) {
		boolean isParent = false;
		SootClass tempSootClass = childClass;
		while (tempSootClass.hasSuperclass()) {
			if (tempSootClass.equals(fatherClass)) {
				isParent = true;
				break;
			}
			tempSootClass = tempSootClass.getSuperclass();
		}
		return isParent;
	}
	
	public boolean equalTo(Object object){
		if(! (object instanceof State)){
			return false;
		}else {
			State state = (State) object;
			boolean equals = equalValue(state.getValue(), state.getmethod());
			return equals;
		}
	}

	public String getAttribute() {
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
	
}
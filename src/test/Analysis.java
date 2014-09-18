package test;

import internal.MethodPlus;
import internal.State;
import internal.UnitPlus;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dispatcher.CreateCompleteCFG;
import dispatcher.Dispatcher;
import dispatcher.DispatcherFactory;
import analysis.ComputeNPA;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.internal.AbstractDefinitionStmt;
import soot.options.Options;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.graph.UnitGraphPlus;

public class Analysis {
	private String classNameString;
	private String sootClassPath;
	private String phaseOption1;
	private String phaseOption2;
	private Map<UnitPlus, List<UnitPlus>> completeCFG;
	private StackTraceElement[] stackTrace;
	private Map<MethodPlus, UnitGraphPlus> methodToUnitGraph;
	private Dispatcher dispatcher;
	private List<SootMethod> sootMethods;

	public Analysis(StackTraceElement[] stackTrace){
		StackTraceElement ste = stackTrace[0];
		classNameString = ste.getClassName();
		sootClassPath=System.getProperty("user.dir")+"\\bin";;
		phaseOption1 = "jpt";
		phaseOption2 = "use-original-names:true";
		this.sootMethods = new ArrayList<>();
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_app(true);
		Options.v().set_whole_program(true);
		Options.v().set_keep_line_number(true);
		Options.v().set_soot_classpath(sootClassPath);
		Options.v().setPhaseOption(phaseOption1, phaseOption2);

		SootClass sootclass = Scene.v().loadClassAndSupport(classNameString);
		Scene.v().loadNecessaryClasses();

		CreateCompleteCFG completeCFG = new CreateCompleteCFG(sootclass, classNameString);
		this.sootMethods = sootclass.getMethods();
		this.stackTrace = stackTrace;
		this.completeCFG = completeCFG.createCFG();
		this.methodToUnitGraph = completeCFG.getMethodToUnitGraph();
	}
	
	public void showCFG(){
		Set<UnitPlus> keySet = completeCFG.keySet();
		for (UnitPlus node : keySet) {
			if (node.getAttribute().equals("null")) {
				//Show the units
				String methodString = String.format("%-30s", node.getMethodPlus().toString());
				System.out.println("unit" + '\t' + node.getNumber() + '\t'
						+ methodString 
						+ node.getUnit().toString());
//				List<ValueBox> vl= node.getUnit().getUseBoxes();
//				for(ValueBox vb:vl){
//					System.out.println(vb.getValue().toString());
//				}
				
				//show the preds
				List<UnitPlus> preds = completeCFG.get(node);
				for(UnitPlus pred:preds){
					if(pred.getAttribute().equals("null")){
						String methodPredString = String.format("%-30s", pred.getMethodPlus().toString());
						System.out.println("pred" + '\t' + pred.getNumber() + '\t'
								+ methodPredString 
								+ pred.getUnit().toString());
					}else{
						String methodPredString = String.format("%-30s", pred.getMethodPlus().toString());
						System.out.println("pred" + '\t' + pred.getNumber()+ pred.getAttribute() + '\t'
								+ methodPredString 
								+ pred.getUnit().toString());
					}

				}
				
			} else {
				//show the units
				String methodString = String.format("%-30s", node.getMethodPlus().toString());
				System.out.println("unit" + '\t' + node.getNumber() + node.getAttribute()
						+ '\t' + methodString 
						+ node.getUnit().toString());
				
				//show the preds
				List<UnitPlus> preds = completeCFG.get(node);
				for(UnitPlus pred:preds){
					if(pred.getAttribute().equals("null")){
						String methodPredString = String.format("%-30s", pred.getMethodPlus().toString());
						System.out.println("pred" + '\t' + pred.getNumber() + '\t'
								+ methodPredString 
								+ pred.getUnit().toString());
					}else{
						String methodPredString = String.format("%-30s", pred.getMethodPlus().toString());
						System.out.println("pred" + '\t' + pred.getNumber()+ pred.getAttribute() + '\t'
								+ methodPredString 
								+ pred.getUnit().toString());
					}

				}
			}

		}
		// Show all Nodes
	}
	public void doAnalysis() throws ClassNotFoundException, FileNotFoundException{
		//Need check!!!
		dispatcher = new DispatcherFactory(this.completeCFG, this.stackTrace, methodToUnitGraph, sootMethods);
		ComputeNPA computeNPA = new ComputeNPA(this);
		List<UnitPlus> errorUnits = dispatcher.StackTraceElementToUnit(stackTrace, 0);
		for(UnitPlus errorUnit:errorUnits){
			if(errorUnit.getUnit() instanceof AbstractDefinitionStmt){
				AbstractDefinitionStmt ads = (AbstractDefinitionStmt) errorUnit.getUnit();
				Value possibleErrorValue = ads.getRightOp();
					computeNPA.analyzeMethod(errorUnit, new State(possibleErrorValue));
			}
		}
		ArrayList<UnitPlus> NPA = computeNPA.getNPA();
		for(UnitPlus unitPlus:NPA){
			String methodString = String.format("%-30s", unitPlus.getMethodPlus().toString());
			System.out.println("NPA" + '\t' + unitPlus.getNumber() + '\t'
					+ methodString 
					+ unitPlus.getUnit().toString());
		}
	}
	
	public Dispatcher getDispatcher(){
		return dispatcher;
	} 
	public  Map<UnitPlus, List<UnitPlus>> getCFG(){
		return completeCFG;
	}
	
	public StackTraceElement[] getStackTraceElements() {
		return stackTrace;
	}

}

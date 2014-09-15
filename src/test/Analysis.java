package test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dispatcher.CreateCompleteCFG;
import dispatcher.Dispatcher;
import dispatcher.DispatcherFactory;
import analysis.ComputeNPA;
import bean.MethodPlus;
import bean.State;
import bean.UnitPlus;
import soot.Scene;
import soot.SootClass;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
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

	public Analysis(){
		classNameString = "analysis.RTEExample";
		sootClassPath = null;
		phaseOption1 = "jpt";
		phaseOption2 = "use-original-names:true";
	}	
	public void createDispatcher(StackTraceElement[] stackTrace) {
		sootClassPath=System.getProperty("user.dir")+"\\bin";
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_app(true);
		Options.v().set_whole_program(true);
		Options.v().set_keep_line_number(true);
		Options.v().set_soot_classpath(sootClassPath);
		Options.v().setPhaseOption(phaseOption1, phaseOption2);

		SootClass sootclass = Scene.v().loadClassAndSupport(classNameString);
		Scene.v().loadNecessaryClasses();

		CreateCompleteCFG completeCFG = new CreateCompleteCFG(sootclass, classNameString);
		this.stackTrace = stackTrace;
		this.completeCFG = completeCFG.createCFG();
		this.methodToUnitGraph = completeCFG.getMethodToUnitGraph();
		
	}
	
	public void doAnalysis(){
		dispatcher = new DispatcherFactory(this.completeCFG, this.stackTrace, methodToUnitGraph);
		ComputeNPA computeNPA = new ComputeNPA();
		List<UnitPlus> errorUnits = dispatcher.StackTraceElementToUnit(stackTrace, 0);
		for(UnitPlus errorUnit:errorUnits){
			List<ValueBox> possibleErrorBoxs = errorUnit.getUnit().getUseBoxes();
			for(ValueBox possibleErrorBox:possibleErrorBoxs){
				Value possibleErrorValue = possibleErrorBox.getValue();
				computeNPA.analyzeMethod(errorUnit, new State(possibleErrorValue));
			}
		}
		ArrayList<UnitPlus> NPA = computeNPA.getNPA();
		for(UnitPlus unitPlus:NPA){
			System.out.println(unitPlus.getUnit());
		}
	}
	
	public Dispatcher getDispatcher(){
		return dispatcher;
	}

}

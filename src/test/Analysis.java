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
import soot.SootMethod;
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
//		Scene.v().addBasicClass("example.ExampleWithException");
		Scene.v().loadNecessaryClasses();

		CreateCompleteCFG completeCFG = new CreateCompleteCFG(sootclass, classNameString);
		this.sootMethods = sootclass.getMethods();
		this.stackTrace = stackTrace;
		this.completeCFG = completeCFG.createCFG();
		this.methodToUnitGraph = completeCFG.getMethodToUnitGraph();
	}
	
	public void doAnalysis(){
		dispatcher = new DispatcherFactory(this.completeCFG, this.stackTrace, methodToUnitGraph, sootMethods);
		ComputeNPA computeNPA = new ComputeNPA(this);
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

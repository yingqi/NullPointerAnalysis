package test;

import java.util.List;
import java.util.Map;

import dispatcher.CreateCompleteCFG;
import dispatcher.Dispatcher;
import dispatcher.DispatcherFactory;
import bean.MethodPlus;
import bean.UnitPlus;
import soot.Scene;
import soot.SootClass;
import soot.options.Options;
import soot.toolkits.graph.UnitGraph;

public class RTEAnalysis {
	private String classNameString = "analysis.RTEExample";
	private String sootClassPath = null;
	private String phaseOption1 = "jpt";
	private String phaseOption2 = "use-original-names:true";
	private Map<UnitPlus, List<UnitPlus>> completeCFG;
	private StackTraceElement[] stackTrace;
	private Map<MethodPlus, UnitGraph> methodToUnitGraph;
	private Dispatcher dispatcher;

	public RTEAnalysis(StackTraceElement[] stackTrace) {
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
		dispatcher = new DispatcherFactory(this.completeCFG, this.stackTrace, methodToUnitGraph);
	}
	
	public Dispatcher getDispatcher(){
		return dispatcher;
	}
	
	
	public void startAnalysis(){
		int lineNumber = stackTrace[0].getLineNumber();
		
	}
}

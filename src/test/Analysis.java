package test;

import internal.Element;
import internal.MethodPlus;
import internal.State;
import internal.UnitPlus;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dispatcher.CreateAllCFG;
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
import soot.tagkit.LineNumberTag;
import soot.tagkit.Tag;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.graph.UnitGraphPlus;

/**
 * initialize the analysis procedure and also 
 * @author leo
 *
 */
public class Analysis {
//	private String classNameString;
//	private String sootClassPath;
	private String phaseOption1;
	private String phaseOption2;
	private ArrayList<String> classNames;
	private Map<UnitPlus, List<UnitPlus>> completeCFG;
//	private StackTraceElement[] stackTrace;
	private Map<MethodPlus, UnitGraphPlus> methodToUnitGraph;
	private Dispatcher dispatcher;
	private List<SootMethod> sootMethods;

	
	public Analysis(StackTraceElement[] stackTrace, String sootClassPath){
		AnalysisMultipleClasses(stackTrace, sootClassPath);
	}
	
	/**
	 * Initialization
	 * @param stackTrace
	 * @param sootClassPath
	 */
	private void  AnalysisMultipleClasses(StackTraceElement[] stackTrace, String sootClassPath){
		//sootClassPath should be specified
//		this.sootClassPath=sootClassPath;
		phaseOption1 = "jpt";
		phaseOption2 = "use-original-names:true";
		this.sootMethods = new ArrayList<>();
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_app(true);
		Options.v().set_whole_program(true);
		Options.v().set_keep_line_number(true);
		Options.v().set_soot_classpath(sootClassPath);
		Options.v().setPhaseOption(phaseOption1, phaseOption2);

		//use ste to find soot class
//		List<SootClass> sootClasses = new ArrayList<SootClass>();
//		classNames = new ArrayList<>();
//		for(StackTraceElement ste:stackTrace){
//			classNameString = ste.getClassName();
//			boolean isClassAnalysed = false;
//			for(String className:classNames){
//				if(className.equals(classNameString)){
//					isClassAnalysed = true;
//				}
//			}
//			if(!isClassAnalysed){
//				classNames.add(classNameString);
//				SootClass sootclass = Scene.v().loadClassAndSupport(classNameString);
//				sootClasses.add(sootclass);
//			}
//		}
		
		//use location to find soot class
		List<SootClass> sootClasses = new ArrayList<SootClass>();
		classNames = new ArrayList<>();
		ArrayList<String> classNames = getclassNames(sootClassPath);
		for(String classNameString: classNames){
				System.out.println(classNameString);
				SootClass sootclass = Scene.v().loadClassAndSupport(classNameString);
				sootClasses.add(sootclass);
		}
		//Be careful if we have to laod necessay classes.
//		Scene.v().loadNecessaryClasses();		
//		classNameString = stackTrace[0].getClassName();
//		SootClass sootclass = Scene.v().loadClassAndSupport(classNameString);
//		Scene.v().addBasicClass(classNameString,sootclass.SIGNATURES);

		CreateAllCFG completeCFG = new CreateAllCFG(sootClasses);
		this.sootMethods = new ArrayList<>();
		for(SootClass sootClass:sootClasses){
			sootMethods.addAll(sootClass.getMethods());
		}
		this.completeCFG = completeCFG.createCFG();
		this.methodToUnitGraph = completeCFG.getMethodToUnitGraph();
	}
	
	/**
	 * helper method to get class names in the assigned path
	 * @param path
	 * @return
	 */
	private ArrayList<String> getclassNames(String path){
		ArrayList<String> classNames = new ArrayList<>();
		File file = new File(path);
		if(file.isDirectory()){
			File[] dirFiles = file.listFiles();
			for(File deeperFile:dirFiles){
				//package name
				if(deeperFile.isDirectory()){
					classNames.addAll(getclassNames(deeperFile.getAbsolutePath()));
					for(int i=0;i<classNames.size();i++){
						String classname = classNames.get(i);
						classname = deeperFile.getName()+"."+classname;
						classNames.set(i, classname);
					}
				}else{
					//class name
					if(deeperFile.getName().endsWith(".class")){
						classNames.add(deeperFile.getName().substring(0, deeperFile.getName().length()-6));
					}
				}
			}
		}
	
		return classNames;
	}
	
	/**
	 * show the comprehensive control flow graph
	 */
	public void showCFG(){
		Set<UnitPlus> keySet = completeCFG.keySet();
		for (UnitPlus node : keySet) {
			//show the normal units
			if (node.getAttribute().equals("null")) {
				//Show the units
				String methodString = String.format("%-30s", node.getMethodPlus().toString());
				System.out.println("unit" + '\t' + node.getNumber() + '\t'
						+ methodString 
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
			//show the units connected to procedure calling
			else {
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
	}
	
	/**
	 * do really the analysis
	 * @throws ClassNotFoundException
	 * @throws FileNotFoundException
	 */
	public void doAnalysis(StackTraceElement[] stackTrace) throws ClassNotFoundException, FileNotFoundException{
		dispatcher = new DispatcherFactory(completeCFG, stackTrace, methodToUnitGraph, sootMethods);
		ComputeNPA computeNPA = new ComputeNPA(dispatcher, stackTrace);
		List<UnitPlus> errorUnits = dispatcher.StackTraceElementToUnit(stackTrace, 0);
		for(UnitPlus errorUnit:errorUnits){
			if(errorUnit.getUnit() instanceof AbstractDefinitionStmt){
				// Q1: whether there are other statments which can cause NPE?
				AbstractDefinitionStmt ads = (AbstractDefinitionStmt) errorUnit.getUnit();
				Value possibleErrorValue = ads.getRightOp();
				List<State> errorStates = new ArrayList<>();
				errorStates.add(new State(possibleErrorValue));
				System.out.println("ErrorUnit: "+errorUnit);
				computeNPA.resetIndexOfStarckTrace();
				Element errorElement = new Element(errorUnit, errorStates);
				computeNPA.analyzeMethod(errorElement);
			}
		}
		
		//show NPA founded
		ArrayList<UnitPlus> NPA = computeNPA.getNPA();
		for(UnitPlus unitPlus:NPA){
			String methodString = String.format("%-30s", unitPlus.getMethodPlus().toString());
			System.out.println("NPA" + '\t' + unitPlus.getNumber() + '\t'
					+ methodString 
					+ unitPlus.getUnit().toString());
			List<Tag> tags = unitPlus.getUnit().getTags();
			for(Tag tag:tags){
				if(tag instanceof LineNumberTag){
					LineNumberTag lineNumberTag = (LineNumberTag) tag;
					System.out.println("File: "+unitPlus.getMethodPlus().getclassName()+".java\t"+"Line Number: "+lineNumberTag.getLineNumber());
				}
			}
		}
	}
	
	/**
	 * get dispatcher
	 * @return
	 */
	public Dispatcher getDispatcher(){
		return dispatcher;
	} 
	
	/**
	 * get control flow graph
	 * @return
	 */
	public  Map<UnitPlus, List<UnitPlus>> getCFG(){
		return completeCFG;
	}


}

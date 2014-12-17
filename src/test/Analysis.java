package test;

import internal.Element;
import internal.MethodPlus;
import internal.State;
import internal.UnitPlus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.Box;

import java_cup.internal_error;
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
import soot.util.Chain;

/**
 * initialize the analysis procedure and also
 * 
 * @author leo
 * 
 */
public class Analysis {
	private String phaseOption1;
	private String phaseOption2;
	private Map<UnitPlus, List<UnitPlus>> completeCFG;
	private Map<MethodPlus, UnitGraphPlus> methodToUnitGraph;
	private Dispatcher dispatcher;

	public Analysis(StackTraceElement[] stackTrace, List<String> sootClassPaths,
			int filetype) {
		// use location to find soot class
		// actually we can only have one sootpath
		List<SootClass> sootClasses = new ArrayList<SootClass>();
		for(String sootClassPath:sootClassPaths){
			phaseOption1 = "jpt";
			phaseOption2 = "use-original-names:true";
			Options.v().set_allow_phantom_refs(true);
			Options.v().set_app(true);
			Options.v().set_whole_program(true);
			Options.v().set_keep_line_number(true);
			Options.v().set_soot_classpath(sootClassPath);
			Options.v().setPhaseOption(phaseOption1, phaseOption2);

			ArrayList<String> classNames = new ArrayList<>();
			if (filetype == 1) {
				classNames = openClassFiles(sootClassPath, sootClassPath);
			} else if (filetype == 0) {
				// analyze the jar file
			}
			for (String classNameString : classNames) {
				SootClass sootclass = Scene.v().loadClassAndSupport(classNameString);
				sootClasses.add(sootclass);
				Scene.v().addBasicClass(classNameString, sootclass.SIGNATURES);
			}
			// Be careful if we have to load necessary classes.
			Scene.v().loadNecessaryClasses();
					
		}
		CreateAllCFG completeCFG = new CreateAllCFG(sootClasses);
		this.completeCFG = completeCFG.createCFG();
		this.methodToUnitGraph = completeCFG.getMethodToUnitGraph();	
	}

	/**
	 * helper method to get class names in the assigned path
	 * 
	 * @param path
	 * @return
	 */
	private ArrayList<String> openClassFiles(String path, String absoluString) {
		ArrayList<String> classNames = new ArrayList<>();
		File file = new File(path);
		// package name
		if (file.isDirectory()) {
			File[] deeperFiles = file.listFiles();
			for(File deeperFile:deeperFiles){
				classNames .addAll(openClassFiles(deeperFile.getAbsolutePath(), absoluString));
			}
			if(!file.getAbsolutePath().equals(absoluString)){
				for (int i = 0; i < classNames.size(); i++) {
					String classname = classNames.get(i);
					classname = file.getName() + "." + classname;
					classNames.set(i, classname);
				}
			}
		} else if(file.isFile()){
			// class name
			if (file.getName().endsWith(".class")) {
				classNames.add(file.getName().substring(0,
						file.getName().length() - 6));
			}
		}
		return classNames;
	}

	/**
	 * do really the analysis
	 * 
	 * @throws ClassNotFoundException
	 * @throws FileNotFoundException
	 */
	public void doAnalysis(StackTraceElement[] stackTrace)
			throws ClassNotFoundException, FileNotFoundException {
		dispatcher = new DispatcherFactory(completeCFG, stackTrace,
				methodToUnitGraph);
		ComputeNPA computeNPA = new ComputeNPA(dispatcher, stackTrace);
		List<UnitPlus> errorUnits = dispatcher.StackTraceElementToUnit(
				stackTrace, 0);
//		System.out.println("Error Units Number: "+errorUnits.size());
		for (UnitPlus errorUnit : errorUnits) {
			List<Value> possibleErrorValues = new ArrayList<>();
			List<ValueBox> useBoxs = errorUnit.getUnit().getUseBoxes();
			for(ValueBox useBox:useBoxs){
				possibleErrorValues.add(useBox.getValue());
			}
			for(Value value:possibleErrorValues){
				List<State> errorStates = new ArrayList<>();
				errorStates.add(new State(value, errorUnit.getMethodPlus()));
//				System.out.println("ErrorUnit: " + errorUnit+"\tError States: "+errorStates.get(0).getValue());
				computeNPA.resetIndexOfStarckTrace();
				Element errorElement = new Element(errorUnit, errorStates);
				computeNPA.analyzeMethod(errorElement);

			}
		}

		// show NPA founded
		ArrayList<UnitPlus> NPA = computeNPA.getNPA();
//		System.out.println(NPA.size());
		for (UnitPlus unitPlus : NPA) {
			String methodString = String.format("%-30s", unitPlus
					.getMethodPlus().toString());
			System.out.println("NPA" + '\t' + unitPlus.getNumber() + '\t'
					+ methodString + unitPlus.getUnit().toString());
			List<Tag> tags = unitPlus.getUnit().getTags();
			boolean isPrinted = false;
			if(!isPrinted){
				for (Tag tag : tags) {
					if (tag instanceof LineNumberTag) {
						isPrinted = true;
						LineNumberTag lineNumberTag = (LineNumberTag) tag;
						System.out.println("File: "
								+ unitPlus.getMethodPlus().getclassName()
								+ ".java\t" + "Line Number: "
								+ lineNumberTag.getLineNumber());
					}
				}
			}
		}
	}


	/**
	 * show the comprehensive control flow graph
	 */
	public void showCFG() {
		System.out.println("ShowCFG: ");
		Set<UnitPlus> keySet = completeCFG.keySet();
		for (UnitPlus node : keySet) {
			// show the normal units
			if (node.getAttribute().equals("null")) {
				// Show the units
				String methodString = String.format("%-30s", node
						.getMethodPlus().toString());
				System.out.println("unit" + '\t' + node.getNumber() + '\t'
						+ methodString + node.getUnit().toString());

				// show the preds
				List<UnitPlus> preds = completeCFG.get(node);
				for (UnitPlus pred : preds) {
					if (pred.getAttribute().equals("null")) {
						String methodPredString = String.format("%-30s", pred
								.getMethodPlus().toString());
						System.out.println("pred" + '\t' + pred.getNumber()
								+ '\t' + methodPredString
								+ pred.getUnit().toString());
					} else {
						String methodPredString = String.format("%-30s", pred
								.getMethodPlus().toString());
						System.out.println("pred" + '\t' + pred.getNumber()
								+ pred.getAttribute() + '\t' + methodPredString
								+ pred.getUnit().toString());
					}

				}

			}
			// show the units connected to procedure calling
			else {
				// show the units
				String methodString = String.format("%-30s", node
						.getMethodPlus().toString());
				System.out.println("unit" + '\t' + node.getNumber()
						+ node.getAttribute() + '\t' + methodString
						+ node.getUnit().toString());

				// show the preds
				List<UnitPlus> preds = completeCFG.get(node);
				for (UnitPlus pred : preds) {
					if (pred.getAttribute().equals("null")) {
						String methodPredString = String.format("%-30s", pred
								.getMethodPlus().toString());
						System.out.println("pred" + '\t' + pred.getNumber()
								+ '\t' + methodPredString
								+ pred.getUnit().toString());
					} else {
						String methodPredString = String.format("%-30s", pred
								.getMethodPlus().toString());
						System.out.println("pred" + '\t' + pred.getNumber()
								+ pred.getAttribute() + '\t' + methodPredString
								+ pred.getUnit().toString());
					}

				}
			}

		}
	}
	
	/**
	 * get dispatcher
	 * 
	 * @return
	 */
	public Dispatcher getDispatcher() {
		return dispatcher;
	}

	/**
	 * get control flow graph
	 * 
	 * @return
	 */
	public Map<UnitPlus, List<UnitPlus>> getCFG() {
		return completeCFG;
	}

}

package analysis;

import internal.Element;
import internal.MethodPlus;
import internal.State;
import internal.UnitPlus;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dispatcher.CreateAllCFG;
import dispatcher.Dispatcher;
import dispatcher.DispatcherFactory;
import analysis.ComputeNPA;
import soot.Immediate;
import soot.Scene;
import soot.SootClass;
import soot.Value;
import soot.ValueBox;
import soot.jimple.Expr;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.Ref;
import soot.jimple.internal.AbstractDefinitionStmt;
import soot.options.Options;
import soot.tagkit.LineNumberTag;
import soot.tagkit.Tag;
import soot.toolkits.graph.UnitGraphPlus;

/**
 * initialize the analysis procedure and also
 * 
 * @author leo
 * 
 */
public class Analysis {
	private String phaseOption1;
	private String phaseOption2;
	private Map<UnitPlus, Set<UnitPlus>> completeCFG;
	private Map<MethodPlus, UnitGraphPlus> methodToUnitGraph;
	private Dispatcher dispatcher;

	public Analysis(StackTraceElement[] stackTrace, String sootClassPath, int filetype, long time) {
		// use location to find soot class
		// actually we can only have one sootpath
		Set<SootClass> sootClasses = new HashSet<SootClass>();
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
		} else if (filetype == 2) {
			for (StackTraceElement ste : stackTrace) {
				classNames.add(ste.getClassName());
			}
		}
		for (String classNameString : classNames) {
			SootClass sootclass = Scene.v().loadClassAndSupport(classNameString);
			sootClasses.add(sootclass);
			Scene.v().addBasicClass(classNameString, sootclass.SIGNATURES);
		}
		// Be careful if we have to load necessary classes.
		Scene.v().loadNecessaryClasses();

		CreateAllCFG completeCFG = new CreateAllCFG(sootClasses, time);
		this.completeCFG = completeCFG.createCFG();
		// System.out.println("CFG created!");
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
			for (File deeperFile : deeperFiles) {
				classNames.addAll(openClassFiles(deeperFile.getAbsolutePath(), absoluString));
			}
			if (!file.getAbsolutePath().equals(absoluString)) {
				for (int i = 0; i < classNames.size(); i++) {
					String classname = classNames.get(i);
					classname = file.getName() + "." + classname;
					classNames.set(i, classname);
				}
			}
		} else if (file.isFile()) {
			// class name
			if (file.getName().endsWith(".class")) {
				classNames.add(file.getName().substring(0, file.getName().length() - 6));
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
	public void doAnalysis(StackTraceElement[] stackTrace, long time) throws ClassNotFoundException,
			FileNotFoundException {
		dispatcher = new DispatcherFactory(completeCFG, stackTrace, methodToUnitGraph);
		ComputeNPA computeNPA = new ComputeNPA(dispatcher, stackTrace);
		Set<UnitPlus> errorUnits = dispatcher.StackTraceElementToUnit(stackTrace, 0);
		System.out.println("Error Units Number: " + errorUnits.size());
//		Set<State> errorStates = new HashSet<>();
		for (UnitPlus errorUnit : errorUnits) {
			Set<State> errorStates = new HashSet<>();
			System.out.println("Error Unit: "+errorUnit);
			List<ValueBox> useBoxs = errorUnit.getUnit().getUseBoxes();
			boolean isUnitError = false;
			for (ValueBox useBox : useBoxs) {
				if (useBox.getValue() instanceof InstanceInvokeExpr) {
					InstanceInvokeExpr instanceInvokeExpr = (InstanceInvokeExpr) useBox.getValue();					
					if ((instanceInvokeExpr.getBase() instanceof Ref)
							|| (instanceInvokeExpr.getBase() instanceof Immediate)) {
						isUnitError = true;
						errorStates.add(new State(instanceInvokeExpr.getBase(), errorUnit.getMethodPlus()));
					} else {
						System.out.println("Invalid Base! " + instanceInvokeExpr.getBase());
					}
				} else if (useBox.getValue() instanceof InstanceFieldRef) {
					InstanceFieldRef instanceFieldRef = (InstanceFieldRef) useBox.getValue();					
					if (((instanceFieldRef.getBase() instanceof Ref)
							|| (instanceFieldRef.getBase() instanceof Immediate))
							&&!instanceFieldRef.getBase().toString().equals("r0")//r0 stands for this object and it could not be null
							) {
						isUnitError = true;
						errorStates.add(new State(instanceFieldRef.getBase(), errorUnit.getMethodPlus()));
					} else {
						System.out.println("Invalid Base! " + instanceFieldRef.getBase());
					}
				}
			}
			if(isUnitError){
				System.out.println(System.currentTimeMillis() - time);
				System.out.println("Error Unit : " + errorUnit+"\nStates : "+errorStates);
				computeNPA.resetIndexOfStarckTrace();
				Element errorElement = new Element(errorUnit, errorStates);
				computeNPA.analyzeMethod(errorElement);
			}
		}
//		Iterator<UnitPlus> errorUnitIterator = errorUnits.iterator();
//		UnitPlus analyzeUnitPlus = errorUnitIterator.next();
//		while (errorUnitIterator.hasNext()) {
//			UnitPlus tempUnitPlus = errorUnitIterator.next();
//			List<ValueBox> useBoxs = tempUnitPlus.getUnit().getUseBoxes();
//			boolean isUnitError = false;
//			for (ValueBox useBox : useBoxs) {
//				if ((useBox.getValue() instanceof InstanceInvokeExpr)||(useBox.getValue() instanceof InstanceFieldRef)) {
//				isUnitError = true;
//				}
//			}
//			if(isUnitError){
//				if (analyzeUnitPlus.getNumber() < tempUnitPlus.getNumber()) {
//					analyzeUnitPlus = tempUnitPlus;
//				}
//			}
//		}
//		System.out.println(System.currentTimeMillis() - time);
//		System.out.println("Error Unit : " + analyzeUnitPlus+"\nStates : "+errorStates);
//		computeNPA.resetIndexOfStarckTrace();
//		Element errorElement = new Element(analyzeUnitPlus, errorStates);
//		computeNPA.analyzeMethod(errorElement);

		Set<UnitPlus> NPA = computeNPA.getNPA();
		// show NPA founded
		for (UnitPlus unitPlus : NPA) {
			System.out.println("NPA" + '\t' + unitPlus.getNumber() + unitPlus.getAttribute() + '\t'
					+ unitPlus.getUnit().toString());
			List<Tag> tags = unitPlus.getUnit().getTags();
			boolean isPrinted = false;
			for (Tag tag : tags) {
				if (!isPrinted) {
					if (tag instanceof LineNumberTag) {
						isPrinted = true;
						LineNumberTag lineNumberTag = (LineNumberTag) tag;
						System.out.println("File: " + unitPlus.getMethodPlus().getclassName() + ".java\t"
								+ "Line Number: " + lineNumberTag.getLineNumber());
					}
				}
			}
		}

		Set<UnitPlus> PossibleNPAs = computeNPA.getPossibleNPAs();
		for (UnitPlus unitPlus : PossibleNPAs) {
			System.out.println("PossibleNPA" + '\t' + unitPlus.getNumber() + unitPlus.getAttribute() + '\t'
					+ unitPlus.getUnit().toString());
			List<Tag> tags = unitPlus.getUnit().getTags();
			boolean isPrinted = false;
			for (Tag tag : tags) {
				if (!isPrinted) {
					if (tag instanceof LineNumberTag) {
						isPrinted = true;
						LineNumberTag lineNumberTag = (LineNumberTag) tag;
						System.out.println("File: " + unitPlus.getMethodPlus().getclassName() + ".java\t"
								+ "Line Number: " + lineNumberTag.getLineNumber());
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
			if (node.getAttribute().equals("")) {
				// Show the units
				String methodString = String.format("%-30s", node.getMethodPlus().toString());
				System.out.println("unit" + '\t' + node.getNumber() + '\t' + methodString + node.getUnit().toString());
				// show the preds
				Set<UnitPlus> preds = completeCFG.get(node);
				for (UnitPlus pred : preds) {
					String methodPredString = String.format("%-30s", pred.getMethodPlus().toString());
					System.out.println("pred" + '\t' + pred.getNumber() + pred.getAttribute() + '\t' + methodPredString
							+ pred.getUnit().toString());
				}

			}
			// show the units connected to procedure calling
			else {
				// show the units
				String methodString = String.format("%-30s", node.getMethodPlus().toString());
				System.out.println("unit" + '\t' + node.getNumber() + node.getAttribute() + '\t' + methodString
						+ node.getUnit().toString());

				// show the preds
				Set<UnitPlus> preds = completeCFG.get(node);
				for (UnitPlus pred : preds) {
					String methodPredString = String.format("%-30s", pred.getMethodPlus().toString());
					System.out.println("pred" + '\t' + pred.getNumber() + pred.getAttribute() + '\t' + methodPredString
							+ pred.getUnit().toString());

				}
			}

		}
	}

	public void showCFG(String[] names) {
		System.out.println("ShowCFG: ");
		Set<UnitPlus> keySet = completeCFG.keySet();
		for (UnitPlus node : keySet) {
			if (node.getMethodPlus().getclassName().equals(names[0])
					&& node.getMethodPlus().getMethodName().equals(names[1])) {
				// show the normal units
				if (node.getAttribute().equals("")) {
					// Show the units
					String methodString = String.format("%-30s", node.getMethodPlus().toString());
					System.out.println("unit" + '\t' + node.getNumber() + '\t' + methodString
							+ node.getUnit().toString());
					// show the preds
					Set<UnitPlus> preds = completeCFG.get(node);
					for (UnitPlus pred : preds) {
						String methodPredString = String.format("%-30s", pred.getMethodPlus().toString());
						System.out.println("pred" + '\t' + pred.getNumber() + pred.getAttribute() + '\t'
								+ methodPredString + pred.getUnit().toString());
					}

				}
				// show the units connected to procedure calling
				else {
					// show the units
					String methodString = String.format("%-30s", node.getMethodPlus().toString());
					System.out.println("unit" + '\t' + node.getNumber() + node.getAttribute() + '\t' + methodString
							+ node.getUnit().toString());

					// show the preds
					Set<UnitPlus> preds = completeCFG.get(node);
					for (UnitPlus pred : preds) {
						String methodPredString = String.format("%-30s", pred.getMethodPlus().toString());
						System.out.println("pred" + '\t' + pred.getNumber() + pred.getAttribute() + '\t'
								+ methodPredString + pred.getUnit().toString());

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
	public Map<UnitPlus, Set<UnitPlus>> getCFG() {
		return completeCFG;
	}

}

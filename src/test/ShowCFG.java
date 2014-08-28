package test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import bean.UnitPlus;
import dispatcher.CreateCompleteCFG;
import soot.Scene;
import soot.SootClass;
import soot.options.Options;

public class ShowCFG {

	static String classNameString = "example.Example";
	static String sootClassPath = null;
	static String phaseOption1 = "jpt";
	static String phaseOption2 = "use-original-names:true";

	public static void main(String[] args) {
		sootClassPath=System.getProperty("user.dir")+"\\bin";
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_app(true);
		Options.v().set_whole_program(true);
		Options.v().set_keep_line_number(true);
		Options.v().set_soot_classpath(sootClassPath);
		Options.v().setPhaseOption(phaseOption1, phaseOption2);

		SootClass sootclass = Scene.v().loadClassAndSupport(classNameString);
		Scene.v().loadNecessaryClasses();

		CreateCompleteCFG createCFG = new CreateCompleteCFG(sootclass, classNameString);
		Map<UnitPlus, List<UnitPlus>> completeCFG = createCFG.createCFG();
		// Show all Nodes
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
}

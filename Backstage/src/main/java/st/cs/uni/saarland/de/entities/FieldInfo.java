package st.cs.uni.saarland.de.entities;

import st.cs.uni.saarland.de.helpClasses.Info;
import soot.MethodOrMethodContext;
import soot.Unit;
import soot.jimple.internal.JimpleLocal;

// helper data storage class for inter-procedural code analysis
public class FieldInfo extends Info{

	// TODO comment
	// TODO move to ./helpMethods because there is InterprocAnalyis class where it is mainly used (and in all other switches)

	// searchedReg not used
	public FieldInfo() {
		super("");
	}
	public JimpleLocal register = null;
 	public Unit unitToStart = null;
 	public MethodOrMethodContext methodToStart = null;
 	public String value = null;
 	public String className = null;
 	
//	@Override
//	public boolean shouldRunOnInitMethod() {
//		// TODO Auto-generated method stub
//		return false;
//	}


	@Override
	public Info clone() {
		FieldInfo newInfo = new FieldInfo();
		newInfo.register = register;
		newInfo.unitToStart = unitToStart;
		newInfo.methodToStart = methodToStart;
		newInfo.value = value;
		newInfo.className = className;
		return newInfo;
	}

	@Override
	public boolean allValuesFound() {
		return ((unitToStart != null) && register !=null && methodToStart != null && value != null && className != null);
	}
	
	@Override
	public boolean equals(Object obj){
		if(!(obj instanceof FieldInfo)){
			return false;
		}
		FieldInfo toCompare = (FieldInfo)obj;
		if(this.value != null){
			if(toCompare.value == null){
				return false;
			}
			return this.value.equals(toCompare.value);
		}
		else{
			boolean registerEquality = false;
			if(this.register != null && toCompare.register != null){
				registerEquality = this.register.equals(toCompare.register);
			}
			else if (this.register == null && toCompare.register == null){
				registerEquality = true;
			}
			
			boolean unitEquality = false;
			if(this.unitToStart != null && toCompare.unitToStart != null){
				unitEquality = this.unitToStart.toString().equals(toCompare.unitToStart.toString());
			}
			else if (this.unitToStart == null && toCompare.unitToStart == null){
				unitEquality = true;
			}
			
			boolean methodEquality = false;
			if(this.methodToStart != null && toCompare.methodToStart != null){
				methodEquality = this.methodToStart.equals(toCompare.methodToStart);
			}
			else if (this.methodToStart == null && toCompare.methodToStart == null){
				methodEquality = true;
			}
			
			boolean classEquality = false;
			if(this.className != null && toCompare.className != null){
				classEquality = this.className.equals(toCompare.className);
			}
			else if (this.className == null && toCompare.className == null){
				classEquality = true;
			}
			return registerEquality && unitEquality && methodEquality && classEquality;
		}
	}
	
}

package st.cs.uni.saarland.de.reachabilityAnalysis;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import soot.SootClass;
import soot.SootMethod;
import st.cs.uni.saarland.de.helpClasses.Helper;

@XStreamAlias("apiinformation")
public class ApiInfoForForward {
	
	//@XStreamConverter(SootMethodConverter.class)
	@XStreamOmitField
	public SootMethod api;
	@XStreamAlias(value="api")
	@XStreamConverter(SignatureConverter.class)
	public String signature;
	public int depthMethodLevel;
	public int depthComponentLevel;
	public final List<String> additionalInfo;
	
	public ApiInfoForForward(){
		additionalInfo = new ArrayList<String>();
	}
	
	
	@XStreamOmitField
	public SootMethod callerMethod;

	public String callerSignature;

	@Override
	public String toString(){
		return String.format("API: %s; DepthMethodLevel: %s; DepthComponentLevel: %s", Helper.getSignatureOfSootMethod(api), depthMethodLevel, depthComponentLevel);
	}

	@Override
	public int hashCode(){
		return signature.hashCode();
	}
	
	@Override
	public boolean equals(Object obj){
		if(!(obj instanceof ApiInfoForForward)){
			return false;
		}
		
		ApiInfoForForward compareTo = (ApiInfoForForward)obj;
		if(this.api == null || compareTo.api == null){
			return false;
		}
		return (this.api.equals(compareTo.api) && (this.depthComponentLevel == compareTo.depthComponentLevel));
	}

}

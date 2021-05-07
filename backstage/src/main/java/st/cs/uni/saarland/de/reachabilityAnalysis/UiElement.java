package st.cs.uni.saarland.de.reachabilityAnalysis;

import java.util.*;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import soot.SootClass;
import soot.SootMethod;
import st.cs.uni.saarland.de.helpClasses.Helper;

@XStreamAlias("uiElement")
public final class UiElement {
	
	public UiElement(){
		this.superClasses = new ArrayList<>();
		this.intentFilters = new ArrayList<>();
		this.text = new HashMap<>();
	}
	
	//@XStreamConverter(SootMethodConverter.class)
	@XStreamOmitField()
	public SootMethod handlerMethod;
	@XStreamAlias("handlerMethod")
	@XStreamConverter(SignatureConverter.class)
	public String signature;//FIXME still have a crunch here due to legacy results we want to process
	public String declaringSootClass;
	public int globalId;
	@XStreamAlias("elementID")
	public String elementId;
	@XStreamAlias("text")
	public Map<String, String> text;
	public String kindOfElement;
	public List<String> superClasses;
	public List<Map<String, List<String>>> intentFilters;
	@XStreamOmitField()
	public SootClass targetSootClass;
	
	@Override
	public String toString(){
		return String.format("Method: %s, ElementId: %s, GlobalId: %s", Helper.getSignatureOfSootMethod(handlerMethod), elementId, globalId);
	}

	@Override
	public int hashCode(){
		return signature.hashCode() ^ elementId.hashCode() ^ kindOfElement.hashCode();
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof UiElement)){
			return false;
		}
		UiElement toCompare = (UiElement)obj;
		if(!this.kindOfElement.equals(toCompare.kindOfElement)){
			return false;
		}

		boolean signatureEquality = this.signature.equals(toCompare.signature);
		if(!signatureEquality){
			return false;
		}
		if (this.elementId == null && toCompare.elementId == null) {
			return true;
		} else if (this.elementId == null || toCompare.elementId == null) {
			return false;
		} else {
			return this.elementId.equals(toCompare.elementId);
		}
	}

}

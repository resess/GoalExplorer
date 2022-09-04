package st.cs.uni.saarland.de.reachabilityAnalysis;

import java.io.*;
import java.util.*;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import org.apache.commons.lang3.StringUtils;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import st.cs.uni.saarland.de.helpClasses.Helper;


@XStreamAlias("uiElement")
public final class UiElement implements Serializable {
	
	public UiElement(){
		this.superClasses = new ArrayList<>();
		this.intentFilters = new ArrayList<>();
		this.text = new HashMap<>();
	}

	public UiElement(SootMethod handlerMethod, int elementId, String kindOfElement, String text){
		this();
		this.handlerMethod = handlerMethod;
		this.signature = handlerMethod.getSignature();
		this.elementId = Integer.toString(elementId);
		this.globalId = elementId;
		this.kindOfElement = kindOfElement;
		this.text.put("default_value", text);	
	}

	
	//@XStreamConverter(SootMethodConverter.class)
	@XStreamOmitField()
	public transient SootMethod handlerMethod;
	@XStreamAlias("handlerMethod")
	@XStreamConverter(SignatureConverter.class)
	public String signature;//FIXME still have a crunch here due to legacy results we want to process
	public String declaringSootClass;
	public int globalId, parentId = -1;
	public Integer idInCode = null;
	@XStreamAlias("elementID")
	public String elementId;
	@XStreamAlias("text")
	public Map<String, String> text;
	public String kindOfElement;
	public List<String> superClasses;
	public List<Map<String, List<String>>> intentFilters;
	@XStreamOmitField()
	public transient SootClass targetSootClass;

	public boolean hasIdInCode(){
		return this.idInCode != null;
	}
	
	@Override
	public String toString(){
		return String.format("Method: %s, ElementId: %s, GlobalId: %s", signature, elementId, globalId);
	}

	@Override
	public int hashCode(){
		if(declaringSootClass != null)
			return signature.hashCode() ^ elementId.hashCode() ^ kindOfElement.hashCode() ^ declaringSootClass.hashCode();
		else return signature.hashCode() ^ elementId.hashCode() ^ kindOfElement.hashCode();
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
		if(declaringSootClass == null)
			if(toCompare.declaringSootClass != null)
				return false;
		else if(!declaringSootClass.equals(toCompare.declaringSootClass))
			return false;
		if (this.elementId == null && toCompare.elementId == null) {
			return true;
		} else if (this.elementId == null || toCompare.elementId == null) {
			return false;
		} else {
			return this.elementId.equals(toCompare.elementId);
		}
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
		oos.writeObject((targetSootClass != null) ? targetSootClass.getName() : "");
	}

	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		ois.defaultReadObject();
		String className = (String)ois.readObject();
		if(!StringUtils.isBlank(className)){
			SootClass sootClass = Scene.v().getSootClass(className);
			this.targetSootClass = sootClass;
		}
		if(!StringUtils.isBlank(this.signature))
			this.handlerMethod = Scene.v().getMethod(this.signature);
	}

}

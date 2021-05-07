package st.cs.uni.saarland.de.reachabilityAnalysis;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class SignatureConverter implements Converter {

	@Override
	public boolean canConvert(Class object) {
		return object.equals(String.class);
	}

	@Override
	public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
		String signature = (String) value;
		writer.startNode("signature");
		writer.setValue(signature);
		writer.endNode();
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		reader.moveDown();
		String signature = reader.getValue();// Scene.v().getMethod(reader.getValue());
		reader.moveUp();
		return signature;
	}

}

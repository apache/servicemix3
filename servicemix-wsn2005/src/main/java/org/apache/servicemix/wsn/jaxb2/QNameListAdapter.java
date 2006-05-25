package org.apache.servicemix.wsn.jaxb2;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.namespace.QName;

import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;

public class QNameListAdapter extends XmlAdapter<String, List<QName>>{

	@Override
	public String marshal(List<QName> v) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<QName> unmarshal(String v) throws Exception {
		UnmarshallingContext context = UnmarshallingContext.getInstance();
		String[] tokens = v.split(" ");
		List<QName> results = new ArrayList<QName>();
		for (int i = 0; i < tokens.length; i++) {
			results.add(DatatypeConverter.parseQName(tokens[i], context));
		}
		return results;
	}

}

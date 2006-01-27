package org.apache.servicemix.packaging.parts;

import java.util.List;

import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

public abstract class AbstractGraphicalEditPart2 extends
		AbstractGraphicalEditPart {

	public IPropertyDescriptor[] getArray(List<IPropertyDescriptor> descriptors) {
		IPropertyDescriptor[] array = new IPropertyDescriptor[descriptors
				.size()];
		int pos = 0;
		for (IPropertyDescriptor descriptor : descriptors) {
			array[pos] = descriptor;
			pos++;
		}
		return array;
	}

}

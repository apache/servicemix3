/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicemix.packaging.figure;

import org.apache.servicemix.packaging.model.ServiceUnit;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.internal.ui.palette.editparts.ColumnsLayout;

/**
 * The GEF figure for a Service Unit
 * 
 * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
 * 
 */
public class ServiceUnitFigure extends Figure {

	static final MarginBorder MARGIN_BORDER = new MarginBorder(8, 8, 8, 13);

	private ServiceNameFigure serviceName;

	private ComponentImage componentImage;

	public ComponentImage getComponentImage() {
		return componentImage;
	}

	public ServiceUnitFigure(ServiceUnit component) {
		setBorder(MARGIN_BORDER);
		ColumnsLayout columnsLayout = new ColumnsLayout();
		columnsLayout.setMinorAlignment(ColumnsLayout.ALIGN_LEFTTOP);
		setLayoutManager(columnsLayout);
		serviceName = new ServiceNameFigure(component);
		componentImage = new ComponentImage(component);
		add(componentImage);
		add(serviceName);
		refresh();
	}

	public void refresh() {
		serviceName.refresh();
	}

	public Dimension getPreferredSize(int wHint, int hHint) {
		Dimension dim = new Dimension();
		dim.width = serviceName.getPreferredSize().width;
		dim.width += getInsets().getWidth();
		dim.width += componentImage.getPreferredSize().width;
		dim.height = componentImage.getPreferredSize().height;
		dim.height += getInsets().getHeight();
		return dim;
	}
}

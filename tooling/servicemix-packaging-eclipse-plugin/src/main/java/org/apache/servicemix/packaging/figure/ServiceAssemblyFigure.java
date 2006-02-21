/*
 * Copyright 2005-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import org.apache.servicemix.packaging.model.ServiceAssembly;
import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;

/**
 * The GEF figure for a Service Assembly
 * 
 * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
 * 
 */
public class ServiceAssemblyFigure extends RoundedRectangle {

	static final MarginBorder MARGIN_BORDER = new MarginBorder(8, 8, 8, 13);

	private ServiceAssemblyNameFigure serviceAssemblyName;

	private ServiceAssemblyControlsFigure serviceAssemblyControls;

	private ContentFigure contentPane;

	public ServiceAssemblyFigure(ServiceAssembly serviceAssembly,
			ActionListener actionListener) {
		setBorder(MARGIN_BORDER);
		ToolbarLayout toolbarLayout = new ToolbarLayout();
		toolbarLayout.setMinorAlignment(ToolbarLayout.ALIGN_TOPLEFT);
		toolbarLayout.setObserveVisibility(true);
		setLayoutManager(toolbarLayout);
		serviceAssemblyName = new ServiceAssemblyNameFigure(serviceAssembly);
		serviceAssemblyControls = new ServiceAssemblyControlsFigure(
				serviceAssembly, actionListener);
		contentPane = new ContentFigure();

		add(serviceAssemblyName);
		add(serviceAssemblyControls);
		add(contentPane);
	}

	public ContentFigure getContentPaneFigure() {
		return contentPane;
	}

	public Dimension getPreferredSize(int wHint, int hHint) {
		Dimension dim = new Dimension();
		dim.width = serviceAssemblyName.getPreferredSize().width > contentPane
				.getPreferredSize().width ? serviceAssemblyName
				.getPreferredSize().width
				: contentPane.getPreferredSize().width;
		dim.width += getInsets().getWidth();
		dim.height = serviceAssemblyName.getPreferredSize().height
				+ serviceAssemblyControls.getPreferredSize().height
				+ contentPane.getPreferredSize().height;
		dim.height += getInsets().getHeight();

		return dim;
	}

	public void refresh() {
		serviceAssemblyName.refresh();
	}
}

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

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;

/**
 * The Eclipse figure for the content for a Service Assembly
 * 
 * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
 * 
 */
public class ContentFigure extends Figure {

	static final MarginBorder MARGIN_BORDER = new MarginBorder(5, 0, 0, 0);

	private static final int SPACING = 5;

	public ContentFigure() {
		setBorder(MARGIN_BORDER);
		ToolbarLayout toolbarLayout = new ToolbarLayout();
		toolbarLayout.setMinorAlignment(ToolbarLayout.ALIGN_TOPLEFT);
		setLayoutManager(toolbarLayout);
		toolbarLayout.setSpacing(SPACING);
	}

	public Dimension getPreferredSize(int wHint, int hHint) {
		Dimension dim = new Dimension();
		int maxWidth = getInsets().getWidth();
		for (Object object : getChildren()) {
			if (object instanceof IFigure) {
				dim.height += ((IFigure) object).getPreferredSize().height
						+ SPACING;
				if (((IFigure) object).getPreferredSize().width > maxWidth)
					maxWidth = ((IFigure) object).getPreferredSize().width;
			}
		}
		dim.width = maxWidth;
		dim.height += getInsets().getHeight();

		return dim;
	}
}

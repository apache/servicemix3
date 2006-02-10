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
package org.apache.servicemix.packaging;

import java.util.List;

import org.apache.servicemix.descriptors.packaging.assets.Components.Component;
import org.apache.servicemix.packaging.model.ComponentConnection;
import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PaletteSeparator;
import org.eclipse.gef.palette.PanningSelectionToolEntry;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * The GEF Palette Factory for the DeployerEditor
 * 
 * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
 * 
 */
public class DeployerEditorPaletteFactory {

	private static final String PALETTE_DOCK_LOCATION = "DeployerEditorPaletteFactory.Location";

	/** Preference ID used to persist the palette size. */
	private static final String PALETTE_SIZE = "DeployerEditorPaletteFactory.Size";

	/** Preference ID used to persist the flyout palette's state. */
	private static final String PALETTE_STATE = "DeployerEditorPaletteFactory.State";

	public static FlyoutPreferences createPalettePreferences() {
		return new FlyoutPreferences() {
			private IPreferenceStore getPreferenceStore() {
				return DeployerPlugin.getDefault().getPreferenceStore();
			}

			public int getDockLocation() {
				return getPreferenceStore().getInt(PALETTE_DOCK_LOCATION);
			}

			public int getPaletteState() {
				return getPreferenceStore().getInt(PALETTE_STATE);
			}

			public int getPaletteWidth() {
				return getPreferenceStore().getInt(PALETTE_SIZE);
			}

			public void setDockLocation(int location) {
				getPreferenceStore().setValue(PALETTE_DOCK_LOCATION, location);
			}

			public void setPaletteState(int state) {
				getPreferenceStore().setValue(PALETTE_STATE, state);
			}

			public void setPaletteWidth(int width) {
				getPreferenceStore().setValue(PALETTE_SIZE, width);
			}
		};
	}

	public static PaletteRoot createPalette(DeployerEditor editor) {
		PaletteRoot palette = new PaletteRoot();
		palette.add(createToolsGroup(palette));
		palette.add(createComponentDrawer(editor));
		palette.add(createServiceEnginesDrawer(editor));
		return palette;
	}

	private static PaletteEntry createComponentDrawer(DeployerEditor editor) {
		PaletteDrawer componentsDrawer = new PaletteDrawer("Binding Components");

		List<ComponentArtifact> componentArtifacts = ComponentArtifactFactory
				.getComponentArtifacts();

		for (ComponentArtifact artifact : componentArtifacts) {
			for (Component component : artifact.getComponents().getComponent()) {
				if (component.getType().equals("binding-component")) {
					ImageDescriptor imageDescriptor = null;
					if (artifact.getComponentImage(component.getName()) != null)
						imageDescriptor = ImageDescriptor
								.createFromImage(artifact
										.getComponentImage(component.getName()));
					CombinedTemplateCreationEntry componentCreationEntry = new CombinedTemplateCreationEntry(
							component.getDescription(), component
									.getDescription(), Component.class,
							new ComponentCreationFactory(editor, component
									.getName(), component.getType()),
							imageDescriptor, imageDescriptor);
					componentsDrawer.add(componentCreationEntry);
				}
			}
		}

		return componentsDrawer;
	}

	private static PaletteEntry createServiceEnginesDrawer(DeployerEditor editor) {
		PaletteDrawer componentsDrawer = new PaletteDrawer("Service Engines");

		List<ComponentArtifact> serviceArtifacts = ComponentArtifactFactory
				.getComponentArtifacts();

		for (ComponentArtifact artifact : serviceArtifacts) {
			for (Component component : artifact.getComponents().getComponent()) {
				if (component.getType().equals("service-engine")) {
					ImageDescriptor imageDescriptor = null;
					if (artifact.getComponentImage(component.getName()) != null)
						imageDescriptor = ImageDescriptor
								.createFromImage(artifact
										.getComponentImage(component.getName()));
					CombinedTemplateCreationEntry componentCreationEntry = new CombinedTemplateCreationEntry(
							component.getDescription(), component
									.getDescription(), Component.class,
							new ComponentCreationFactory(editor, component
									.getName(), component.getType()),
							imageDescriptor, imageDescriptor);
					componentsDrawer.add(componentCreationEntry);
				}
			}
		}

		return componentsDrawer;
	}

	private static PaletteContainer createToolsGroup(PaletteRoot palette) {
		PaletteGroup toolGroup = new PaletteGroup("Tools");

		// Add a selection tool to the group
		ToolEntry tool = new PanningSelectionToolEntry();
		toolGroup.add(tool);
		palette.setDefaultEntry(tool);

		// Add a marquee tool to the group
		toolGroup.add(new MarqueeToolEntry());

		// Add a (unnamed) separator to the group
		toolGroup.add(new PaletteSeparator());

		// Add (solid-line) connection tool
		tool = new ConnectionCreationToolEntry("Solid connection",
				"Create a solid-line connection", new CreationFactory() {
					public Object getNewObject() {
						return null;
					}

					public Object getObjectType() {
						return ComponentConnection.SOLID_CONNECTION;
					}
				}, ImageDescriptor.createFromFile(DeployerPlugin.class,
						"icons/connection_s16.gif"), ImageDescriptor
						.createFromFile(DeployerPlugin.class,
								"icons/connection_s24.gif"));
		toolGroup.add(tool);

		return toolGroup;
	}
}

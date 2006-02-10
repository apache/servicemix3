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

import org.apache.servicemix.packaging.actions.DeployAction;
import org.apache.servicemix.packaging.actions.UndeployServiceAction;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.actions.ActionFactory;

/**
 * GEF Editor Context Menu for the DeployerEditor
 * 
 * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
 * 
 */
public class DeployerEditorContextMenuProvider extends ContextMenuProvider {

	private ActionRegistry actionRegistry;

	private DeployerEditor editor;

	public DeployerEditorContextMenuProvider(EditPartViewer viewer,
			DeployerEditor editor, ActionRegistry registry) {
		super(viewer);
		if (registry == null) {
			throw new IllegalArgumentException();
		}
		actionRegistry = registry;
		this.editor = editor;
	}

	@Override
	public void buildContextMenu(IMenuManager menu) {
		// Add standard action groups to the menu
		GEFActionConstants.addStandardActionGroups(menu);

		actionRegistry.registerAction(new DeployAction(getViewer(),
				editor));
		actionRegistry.registerAction(new UndeployServiceAction(getViewer(),
				editor));

		menu.appendToGroup(GEFActionConstants.GROUP_EDIT,
				getAction(DeployAction.COMPONENT_ID));
		menu.appendToGroup(GEFActionConstants.GROUP_EDIT,
				getAction(UndeployServiceAction.COMPONENT_ID));

		// Add actions to the menu
		menu.appendToGroup(GEFActionConstants.GROUP_UNDO, // target group id
				getAction(ActionFactory.UNDO.getId())); // action to add
		menu.appendToGroup(GEFActionConstants.GROUP_UNDO,
				getAction(ActionFactory.REDO.getId()));
		menu.appendToGroup(GEFActionConstants.GROUP_EDIT,
				getAction(ActionFactory.DELETE.getId()));

	}

	private IAction getAction(String actionId) {
		return actionRegistry.getAction(actionId);
	}

}

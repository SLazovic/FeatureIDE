/* FeatureIDE - A Framework for Feature-Oriented Software Development
 * Copyright (C) 2005-2015  FeatureIDE team, University of Magdeburg, Germany
 *
 * This file is part of FeatureIDE.
 * 
 * FeatureIDE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * FeatureIDE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with FeatureIDE.  If not, see <http://www.gnu.org/licenses/>.
 *
 * See http://featureide.cs.ovgu.de/ for further information.
 */
package de.ovgu.featureide.fm.ui.editors.featuremodel.operations;

import static de.ovgu.featureide.fm.core.localization.StringTable.CREATE_COMPOUND;
import static de.ovgu.featureide.fm.core.localization.StringTable.DEFAULT_FEATURE_LAYER_CAPTION;

import java.util.LinkedList;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.gef.ui.parts.GraphicalViewerImpl;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TextCellEditor;

import de.ovgu.featureide.fm.core.base.FeatureUtils;
import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.base.IFeatureStructure;
import de.ovgu.featureide.fm.core.base.impl.Feature;
import de.ovgu.featureide.fm.core.functional.Functional;
import de.ovgu.featureide.fm.ui.editors.featuremodel.commands.renaming.FeatureCellEditorLocator;
import de.ovgu.featureide.fm.ui.editors.featuremodel.commands.renaming.FeatureLabelEditManager;
import de.ovgu.featureide.fm.ui.editors.featuremodel.editparts.FeatureEditPart;
import de.ovgu.featureide.fm.ui.editors.featuremodel.layouts.FeatureDiagramLayoutHelper;

/**
 * Operation with functionality to create a compound feature. Enables undo/redo
 * functionality.
 * 
 * @author Fabian Benduhn
 * @author Marcus Pinnecke
 */
public class FeatureCreateCompoundOperation extends AbstractFeatureModelOperation {

	private IFeature newCompound;
	private IFeature parent;
	private Object viewer;
	private LinkedList<IFeature> selectedFeatures;
	private Object diagramEditor;

	/**
	 * @param label
	 */
	public FeatureCreateCompoundOperation(Object viewer, IFeature parent, IFeatureModel featureModel, LinkedList<IFeature> selectedFeatures, Object diagramEditor) {
		super(featureModel, CREATE_COMPOUND);
		this.viewer = viewer;
		this.parent = parent;
		this.selectedFeatures = new LinkedList<IFeature>();
		this.selectedFeatures.addAll(selectedFeatures);
		this.diagramEditor = diagramEditor;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		int number = 0;
		while (FeatureUtils.getFeatureNames(featureModel).contains(DEFAULT_FEATURE_LAYER_CAPTION + ++number))
			;
		newCompound = new Feature(featureModel, DEFAULT_FEATURE_LAYER_CAPTION + number);
		if (parent != null) {
			newCompound.getStructure().setAND(true);
			newCompound.getStructure().setMultiple(parent.getStructure().isMultiple());
		}
		redo(monitor, info);

		// select the new feature
		FeatureEditPart part;
		if (viewer instanceof GraphicalViewerImpl) {
			part = (FeatureEditPart) ((GraphicalViewerImpl) viewer).getEditPartRegistry().get(newCompound);
		} else {
			part = (FeatureEditPart) ((GraphicalViewerImpl) diagramEditor).getEditPartRegistry().get(newCompound);
		}

		if (part != null) {
			if (viewer instanceof GraphicalViewerImpl) {
				((GraphicalViewerImpl) viewer).setSelection(new StructuredSelection(part));
			} else {
				((GraphicalViewerImpl) diagramEditor).setSelection(new StructuredSelection(part));
			}

			part.getViewer().reveal(part);

			// open the renaming command
			DirectEditManager manager = new FeatureLabelEditManager(part, TextCellEditor.class, new FeatureCellEditorLocator(part.getFeatureFigure()),
					featureModel);
			manager.show();
		}
		return Status.OK_STATUS;
	}

	@Override
	protected void redo() {
		if (parent != null) {
			LinkedList<IFeature> newChildren = new LinkedList<IFeature>();
			for (IFeatureStructure featureStructure : parent.getStructure().getChildren()) {
				if (selectedFeatures.contains(featureStructure)) {
					if (!newCompound.getStructure().hasChildren())
						newChildren.add(newCompound);
					featureStructure.setMandatory(false);
					newCompound.getStructure().addChild(featureStructure);
				} else {
					newChildren.add(featureStructure.getFeature());
				}
			}
			parent.getStructure().setChildren(Functional.toList(Functional.map(newChildren, FeatureUtils.FEATURE_TO_STRUCTURE)));
			featureModel.addFeature(newCompound);
		} else {
			newCompound.getStructure().addChild(featureModel.getStructure().getRoot());
			featureModel.addFeature(newCompound);
			featureModel.getStructure().setRoot(newCompound.getStructure());
		}

		//		newCompound = featureModel.getFeature(newCompound.getName());

		FeatureDiagramLayoutHelper.initializeCompoundFeaturePosition(featureModel, selectedFeatures, newCompound);
	}

	@Override
	protected void undo() {
		if (parent == null) {
			featureModel.getStructure().replaceRoot(featureModel.getStructure().getRoot().removeLastChild());
		} else {
			featureModel.deleteFeature(featureModel.getFeature(newCompound.getName()));
		}
	}

}

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
package de.ovgu.featureide.ui.mpl.wizards.page;

import static de.ovgu.featureide.fm.core.localization.StringTable.HERE_YOU_SELECT_THE_FEATURES_FOR_THE_NEW_INTERFACE_;
import static de.ovgu.featureide.fm.core.localization.StringTable.PLEASE_SELECT_A_PROJECT_IN_THE_PREVIOUS_PAGE_;
import static de.ovgu.featureide.fm.core.localization.StringTable.SELECT_AT_LEAST_ONE_FEATURE_;
import static de.ovgu.featureide.fm.core.localization.StringTable.SELECT_FEATURES;

import java.util.HashSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import de.ovgu.featureide.core.IFeatureProject;
import de.ovgu.featureide.fm.core.base.FeatureUtils;
import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.ui.wizards.AbstractWizardPage;
import de.ovgu.featureide.fm.ui.wizards.WizardConstants;

/**
 * A Wizard Page to select the features from the other project to create the
 * interface.
 * 
 * @author Christoph Giesel
 * @author Sebastian Krieter
 * @author Marcus Pinnecke
 */
public class SelectFeaturesWizardPage extends AbstractWizardPage {
	
	private Tree featuresTree;
	private HashSet<String> featureNames = new HashSet<String>();
	
	public SelectFeaturesWizardPage() {
		super(SELECT_FEATURES);
		setTitle(SELECT_FEATURES);
		setDescription(HERE_YOU_SELECT_THE_FEATURES_FOR_THE_NEW_INTERFACE_);
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		setControl(container);
		
		featuresTree = new Tree(container, SWT.MULTI | SWT.CHECK);
	    featuresTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		featuresTree.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.detail == SWT.CHECK) {
					TreeItem item = (TreeItem) e.item;
					if (item.getChecked()) {
						featureNames.add(item.getText());
					} else {
						featureNames.remove(item.getText());
					}
					updatePage();
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				updatePage();
			}
		});
		
		Composite buttonGroup = new Composite(container, 0);
		buttonGroup.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		buttonGroup.setLayout(gridLayout);
		
		Button selectAllButton = new Button(buttonGroup, SWT.PUSH);
		selectAllButton.setText("Select All");
		selectAllButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				checkItems(true);			
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		
		Button deselectAllButton = new Button(buttonGroup, SWT.PUSH);
		deselectAllButton.setText("Deselect All");
		deselectAllButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				checkItems(false);		
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		
//		buttonGroup.pack();
//		container.pack();
		setPageComplete(false);		
	}
	
	private void checkItems(boolean checkStatus) {
		TreeItem[] items = featuresTree.getItems();
		for (int i = 0; i < items.length; i++) {
			check(items[i], checkStatus);
		}
		updatePage();				
	}
	
	private void check(TreeItem parent, boolean checkStatus) {
		parent.setChecked(checkStatus);
		if (checkStatus) {
			featureNames.add(parent.getText());
		} else {
			featureNames.remove(parent.getText());
		}
		TreeItem[] items = parent.getItems();
		for (int i = 0; i < items.length; i++) {
			check(items[i], checkStatus);
		}
	}
	
	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			featuresTree.setItemCount(0);
			featureNames.clear();
			Object featureProject = abstractWizard.getData(WizardConstants.KEY_OUT_PROJECT);
			if (featureProject != null) {
				addFeaturesToTree(((IFeatureProject)featureProject).getFeatureModel().getStructure().getRoot().getFeature());
			} else {
				setErrorMessage(PLEASE_SELECT_A_PROJECT_IN_THE_PREVIOUS_PAGE_);
				setPageComplete(false);
			}
		}
		super.setVisible(visible);
	}

	/**
	 * Add the feature name as an item to the tree.
	 * 
	 * @param root
	 *            the feature to add
	 */
	private void addFeaturesToTree(IFeature root) {
		TreeItem item = new TreeItem(featuresTree, SWT.NORMAL);
		item.setText(root.getName());
		item.setData(root);
		
		for (IFeature feature : FeatureUtils.convertToFeatureList(root.getStructure().getChildren())) {
			addFeaturesToTree(feature, item);
		}
		item.setExpanded(true);
	}

	/**
	 * Add the feature name as an item to the tree.
	 * 
	 * @param root
	 *            the feature to add
	 * @param parent
	 *            the parent item to add the feature as a child
	 */
	private void addFeaturesToTree(IFeature root, TreeItem parent) {
		TreeItem item = new TreeItem(parent, SWT.NORMAL);
		item.setText(root.getName());
		item.setData(root);
		item.setExpanded(true);

		for (IFeature feature : FeatureUtils.convertToFeatureList(root.getStructure().getChildren()))
			addFeaturesToTree(feature, item);

		item.setExpanded(true);
	}
	
	@Override
	protected void putData() {
		abstractWizard.putData(WizardConstants.KEY_OUT_FEATURES, featureNames);
	}
	
	@Override
	protected String checkPage() {
		if (featureNames.isEmpty()) {
			return SELECT_AT_LEAST_ONE_FEATURE_;
		}
		return null;
	}
}

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
package de.ovgu.featureide.ui.statistics.core.composite.lazyimplementations;

import java.util.List;

import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.ui.statistics.core.composite.LazyParent;

/**
 * Displays all dead features of a feature model.
 * 
 * @author Sebastian Krieter
 */
public class DeadFeaturesParentNode extends LazyParent {
	private final IFeatureModel model;

	public DeadFeaturesParentNode(String description, IFeatureModel model) {
		super(description, null);
		this.model = model;
		calculateChidren(false);
	}

	@Override
	protected void initChildren() {
		// TODO Performance: Dead and core features should be calculated together.
		List<IFeature> deadFeatures = model.getAnalyser().getDeadFeatures();
		setValue(deadFeatures.size());

		for (IFeature feature : deadFeatures) {
			addChild(new FeatureNode(feature, true));
		}
	}

}

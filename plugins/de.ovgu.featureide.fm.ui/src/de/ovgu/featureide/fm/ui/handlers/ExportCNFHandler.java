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
package de.ovgu.featureide.fm.ui.handlers;

import static de.ovgu.featureide.fm.core.localization.StringTable.EXPORT_TO_CNF;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.PlatformUI;
import org.prop4j.Node;
import org.prop4j.NodeWriter;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.base.impl.FeatureModelFactory;
import de.ovgu.featureide.fm.core.editing.NodeCreator;
import de.ovgu.featureide.fm.core.io.FeatureModelReaderIFileWrapper;
import de.ovgu.featureide.fm.core.io.UnsupportedModelException;
import de.ovgu.featureide.fm.core.io.xml.XmlFeatureModelReader;
import de.ovgu.featureide.fm.ui.FMUIPlugin;
import de.ovgu.featureide.fm.ui.handlers.base.AFileHandler;

/**
 * 
 * Exports the conjunctive normal form of the feature model.
 * 
 * @author Jens Meinicke
 * @author Marcus Pinnecke
 */
public class ExportCNFHandler extends AFileHandler {

	@Override
	protected void singleAction(final IFile inputFile) {
		final IFeatureModel model = readModel(inputFile);
		Job job = new Job(EXPORT_TO_CNF) {
			protected IStatus run(IProgressMonitor monitor) {
				final String text = getCNF(model);
				// UI access
				final StringBuilder path = new StringBuilder();
				Display.getDefault().syncExec(new Runnable() {

					@Override
					public void run() {
						path.append(openFileDialog(inputFile));
					}

				});
				saveFile(text, path.toString());
				try {
					inputFile.getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
				} catch (CoreException e) {
					FMUIPlugin.getDefault().logError(e);
				}
				return Status.OK_STATUS;
			}

			/**
			 * Calculates the files content for all CNF representations.
			 * 
			 * @param model
			 * @return
			 */
			private String getCNF(IFeatureModel model) {
				Node nodes = NodeCreator.createNodes(model.clone(null)).toCNF();
				StringBuilder cnf = new StringBuilder();
				cnf.append("Logical Symbols:\r\n");
				cnf.append(nodes.toString(NodeWriter.logicalSymbols));
				cnf.append("\r\n\r\nTextual Symbols:\r\n");
				cnf.append(nodes.toString(NodeWriter.textualSymbols));
				cnf.append("\r\n\r\nJava Symbols:\r\n");
				cnf.append(nodes.toString(NodeWriter.javaSymbols));
				cnf.append("\r\n\r\nShort Symbols:\r\n");
				cnf.append(nodes.toString(NodeWriter.shortSymbols));
				return cnf.toString();
			}

		};
		job.setPriority(Job.INTERACTIVE);
		job.schedule();
	}

	/**
	 * saves the given content to a text File at a given path(including
	 * filename)
	 * 
	 * @param content
	 * @param path
	 */
	private void saveFile(String content, String path) {
		if (path == null)
			return;
		File outputFile = new File(path);
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(outputFile));
			out.write(content);
		} catch (IOException e) {
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					FMUIPlugin.getDefault().logError(e);
				}
			}
		}

		return;
	}

	/**
	 * opens a File Dialog and returns the selected path
	 * 
	 * @param inputFile
	 * 
	 * @param text
	 * 
	 */
	private String openFileDialog(IFile inputFile) {
		FileDialog fileDialog = new FileDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.SAVE);
		fileDialog.setFileName("cnf.txt");
		fileDialog.setFilterExtensions(new String[] { "*.txt" });
		fileDialog.setOverwrite(true);
		fileDialog.setFilterPath(inputFile.getProject().getLocation().toOSString());
		return fileDialog.open();

	}

	/**
	 * reads the featureModel from file
	 * 
	 * @param inputFile
	 * @return featureModel
	 * @throws UnsupportedModelException
	 * @throws FileNotFoundException
	 */
	private IFeatureModel readModel(IFile inputFile) {
		IFeatureModel fm = FeatureModelFactory.getInstance().createFeatureModel();
		FeatureModelReaderIFileWrapper fmReader = new FeatureModelReaderIFileWrapper(new XmlFeatureModelReader(fm));

		try {
			fmReader.readFromFile(inputFile);
		} catch (FileNotFoundException e) {
			FMUIPlugin.getDefault().logError(e);
		} catch (UnsupportedModelException e) {
			FMUIPlugin.getDefault().logError(e);
		}
		return fm;
	}
}

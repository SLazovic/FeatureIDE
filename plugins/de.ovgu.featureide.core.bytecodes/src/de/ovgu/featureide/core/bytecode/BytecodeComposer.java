package de.ovgu.featureide.core.bytecode;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.resources.IFile;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import de.ovgu.featureide.core.builder.ComposerExtensionClass;
import de.ovgu.featureide.core.bytecode.utils.BytecodeUtils;
import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.base.impl.ConfigFormatManager;
import de.ovgu.featureide.fm.core.configuration.Configuration;
import de.ovgu.featureide.fm.core.io.manager.SimpleFileHandler;

/**
 * Bytecode composer
 */

public class BytecodeComposer extends ComposerExtensionClass{

	@Override
	public void performFullBuild(IFile config) {
		// TODO Auto-generated method stub
		Map<String, ClassNode> list=new HashMap<String, ClassNode>();
		Configuration configuration = new Configuration(featureProject.getFeatureModel());
		SimpleFileHandler.load(Paths.get(config.getLocationURI()), configuration, ConfigFormatManager.getInstance());
		for (final IFeature f : configuration.getSelectedFeatures()) {
			if (!f.getStructure().isAbstract()) {
				BytecodeUtils.getClassNodes(list, featureProject.getSourceFolder().getFolder(f.getName()).getRawLocation().makeAbsolute().toFile());
			}
		}
		createFiles(featureProject.getBuildFolder().getRawLocation().makeAbsolute().toFile().getAbsolutePath(), list);
	}
	
	/**
	 * Method that creates bytecode files from a map in a given directory
	 * @param dir is the path to use to create bytecode files
	 * @param map contains bytecode ClassNodes
	 */
	
	public void createFiles(String dir, Map<String, ClassNode> map){
		for(String key:map.keySet()){
			ClassNode cn=map.get(key);
			try {
				String []split=cn.name.split("/");
				String packages=cn.name.replace(split[split.length-1], "");
				File f=new File(dir,packages);
				f.mkdirs();
				ClassWriter cw=new ClassWriter(ClassWriter.COMPUTE_MAXS|ClassWriter.COMPUTE_FRAMES);
				cn.accept(cw);
				DataOutputStream dout=new DataOutputStream(new FileOutputStream(new File(dir,cn.name+".class")));
				dout.write(cw.toByteArray());
				dout.close();
				//f.getAbsoluteFile()+"/"+(key.substring(packages.length()+1)+".class")
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

}

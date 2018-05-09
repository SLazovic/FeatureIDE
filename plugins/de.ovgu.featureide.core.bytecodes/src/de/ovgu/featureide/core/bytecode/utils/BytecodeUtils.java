package de.ovgu.featureide.core.bytecode.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public class BytecodeUtils {

public static final byte []magicNumber={(byte)0xCA, (byte)0xFE, (byte)0xBA, (byte)0xBE};
	
	/**
	 * Check if a file is a bytecode based on the magic number (CAFEBABE)
	 * 
	 * @param file
	 * @return true if it is a bytecode
	 */
	public static boolean isBytecodeFile(File file) {
		if(file==null||!file.exists())
			return false;
		BufferedInputStream bis=null;
		byte []buf=new byte[4];
		try {
			bis=new BufferedInputStream(new FileInputStream(file));
			bis.read(buf, 0, 4);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}finally {
			if(bis!=null){
				try {
					bis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return Arrays.equals(buf, magicNumber);
	}

	/**
	 * Check if a file is a bytecode or if a directory contains a bytecode file based on the extension and the magic number using isBytecodeFile
	 * 
	 * @param file
	 * @param name
	 * @return whether it is found or not
	 */
	public static boolean containsBytecodeFile(File file, String name){
		if(file.isDirectory()){
			for (File child : file.listFiles()) {
				if(containsBytecodeFile(child, child.getName())){
					return true;
				}
			}
			return false;
		}else{
			return name.endsWith(".class")&&isBytecodeFile(file);
		}
	}
	public static void getClassNodes(Map<String, ClassNode> map, File file) {
		if(file.isDirectory()){
			for(File f:file.listFiles())
				getClassNodes(map, f);
		}else{
			if(isBytecodeFile(file)){
				try {
					ClassReader cr=new ClassReader(new FileInputStream(file));
					ClassNode cn=new ClassNode();
					cr.accept(cn, ClassReader.EXPAND_FRAMES);
					ClassNode tmp=map.get(cn.name);
					if(tmp==null)
						map.put(cn.name, cn);
					else{
						for(FieldNode fn:cn.fields)
							tmp.fields.add(fn);
						for(MethodNode mn:cn.methods){
							if(mn.name.equals("<init>")&&mn.desc.equals("()V")){
								System.out.println("INIT");
								for(MethodNode tmpMN:tmp.methods){
									if(tmpMN.equals(mn.name)&&tmpMN.desc.equals(mn.desc)&&tmpMN.instructions.size()<mn.instructions.size()){
										tmp.methods.remove(tmpMN);
										tmp.methods.add(mn);
									}
								}
							}else
								tmp.methods.add(mn);
						}
						for(String i:cn.interfaces)
							tmp.interfaces.add(i);
						if(cn.access>tmp.access)
							tmp.access=cn.access;
						if(cn.superName!=null&&cn.equals(""))
							tmp.superName=cn.superName;
					}
				}catch (Exception e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}
		}
	}

}

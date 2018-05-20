package de.ovgu.featureide.core.bytecode.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Bytecode utils
 *
 */

public class BytecodeUtils {

	/**
	 * Bytecode's magic number
	 */
	
public static final byte []magicNumber={(byte)0xCA, (byte)0xFE, (byte)0xBA, (byte)0xBE};
	
	/**
	 * Check if a file is a bytecode based on the magic number (CAFEBABE)
	 * 
	 * @param file The file
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
	 * @param file The file
	 * @param name The file name
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
	
	/**
	 * Add to a map all ClassNodes from a file or a directory
	 * @param map The map used to store ClassNodes 
	 * @param file The file or directory
	 */
	
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
								for(MethodNode tmpMN:tmp.methods){
									if(tmpMN.equals(mn.name)&&tmpMN.desc.equals(mn.desc)&&tmpMN.instructions.size()<mn.instructions.size()){
										tmp.methods.remove(tmpMN);
										tmp.methods.add(mn);
									}
								}
							}else if(mn.name.equals("<clinit>")){
								MethodNode newMethod=null;
								for(MethodNode tmpMN:tmp.methods){
									if(tmpMN.name.equals("<clinit>")){
										newMethod=tmpMN;
										tmp.methods.remove(tmpMN);
										break;
									}
								}
								if(newMethod==null)
									tmp.methods.add(0, mn);
								else{
									newMethod.instructions=insnListMerge(newMethod.instructions, mn.instructions);
									tmp.methods.add(0, newMethod);
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
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Method that merges two lists of instructions
	 * @param il1 The first list
	 * @param il2 The second list
	 * @return The merged instructions list
	 */
	
	public static InsnList insnListMerge(InsnList il1, InsnList il2){
		InsnList ret=new InsnList();
		for(int i=0; i<il1.size()-1; i++)
			ret.add(insnClone(il1.get(i)));
		if(il1.size()>0&&il1.get(il1.size()-1).getOpcode()!=177)
			ret.add(insnClone(il1.get(il1.size()-1)));
		for(int i=0; i<il2.size(); i++)
			ret.add(insnClone(il2.get(i)));
		return ret;
	}
	
	/**
	 * Clone an AbstractInsnNode
	 * @param insn The given AbstractInsnNode 
	 * @return The clone of insn
	 */
	
	public static AbstractInsnNode insnClone(AbstractInsnNode insn){
		AbstractInsnNode ret=null;
		if(insn instanceof InsnNode){
			InsnNode i=(InsnNode)insn;
			ret=new InsnNode(i.getOpcode());
		}else if(insn instanceof FieldInsnNode){
			FieldInsnNode i=(FieldInsnNode)insn;
			ret=new FieldInsnNode(i.getOpcode(), i.owner, i.name, i.desc);
		}else if(insn instanceof LdcInsnNode){
			LdcInsnNode i=(LdcInsnNode)insn;
			ret=new LdcInsnNode(i.cst);
		}else if(insn instanceof IntInsnNode){
			IntInsnNode i=(IntInsnNode)insn;
			ret=new IntInsnNode(i.getOpcode(), i.operand);
		}else if(insn instanceof VarInsnNode){
			VarInsnNode i=(VarInsnNode)insn;
			ret=new VarInsnNode(i.getOpcode(), i.var);
		}else if(insn instanceof TypeInsnNode){
			TypeInsnNode i=(TypeInsnNode)insn;
			ret=new TypeInsnNode(i.getOpcode(), i.desc);
		}else if(insn instanceof InvokeDynamicInsnNode){
			InvokeDynamicInsnNode i=(InvokeDynamicInsnNode)insn;
			ret=new InvokeDynamicInsnNode(i.name, i.desc, i.bsm, i.bsmArgs);
		}else if(insn instanceof JumpInsnNode){
			JumpInsnNode i=(JumpInsnNode)insn;
			ret=new JumpInsnNode(i.getOpcode(), i.label);
		}else if(insn instanceof LabelNode){
			LabelNode i=(LabelNode)insn;
			ret=new LabelNode(i.getLabel());
		}else if(insn instanceof IincInsnNode){
			IincInsnNode i=(IincInsnNode)insn;
			ret=new IincInsnNode(i.var, i.incr);
		}else if(insn instanceof LineNumberNode){
			LineNumberNode i=(LineNumberNode)insn;
			ret=new LineNumberNode(i.line, i.start);
		}else if(insn instanceof MethodInsnNode){
			MethodInsnNode i=(MethodInsnNode)insn;
			ret=new MethodInsnNode(i.getOpcode(), i.owner, i.name, i.desc, i.itf);
		}else if(insn instanceof MultiANewArrayInsnNode){
			MultiANewArrayInsnNode i=(MultiANewArrayInsnNode)insn;
			ret=new MultiANewArrayInsnNode(i.desc, i.dims);
		}else if(insn instanceof LookupSwitchInsnNode){
			LookupSwitchInsnNode i=(LookupSwitchInsnNode)insn;
			int []keys=new int[i.keys.size()];
			int cpt=0;
			for(int j:i.keys)
				keys[cpt++]=j;
			ret=new LookupSwitchInsnNode(i.dflt, keys, (LabelNode[]) i.labels.toArray());
		}else if(insn instanceof TableSwitchInsnNode){
			TableSwitchInsnNode i=(TableSwitchInsnNode)insn;
			ret=new TableSwitchInsnNode(i.min, i.max, i.dflt, (LabelNode[]) i.labels.toArray());
		}else if(insn instanceof FrameNode){
			FrameNode i=(FrameNode)insn;
			ret=new FrameNode(i.type, i.local.size(), i.local.toArray(), i.stack.size(), i.stack.toArray());
		}
		return ret;
	}
}

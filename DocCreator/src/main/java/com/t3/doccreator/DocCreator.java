/*
 * Copyright (c) 2014 tabletoptool.com team.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     rptools.com team - initial implementation
 *     tabletoptool.com team - further development
 */
package com.t3.doccreator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;


public class DocCreator {
	private Multimap<String, MethodDefinition> methods=HashMultimap.create();
	private String name;
	
	public DocCreator(String name, File file) {
		this.name=name;
		try(FileReader r=new FileReader(file)) {
			String content=IOUtils.toString(r);
			
			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setSource(content.toCharArray());
	 
	 
			final CompilationUnit cu = (CompilationUnit) parser.createAST(new NullProgressMonitor());
			
			cu.accept(new ASTVisitor() {
				@Override
				public boolean visit(MethodDeclaration m) {
					if(!m.isConstructor() && 
							!m.getName().getIdentifier().equals("getVariableName") &&
							!m.toString().startsWith("private")) {
						MethodDefinition md=new MethodDefinition(m);
						methods.put(md.getName(),md);
					}
					return false;
				}
			});
			
			for(String key:methods.keySet()) {
				
			}
			
		} catch(FileNotFoundException e) {
			try {
				System.err.println(file.getAbsoluteFile().getCanonicalPath());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void print(PrintStream p) {
		p.print(name);
		if(!name.endsWith("."))
			p.print(":");
		p.println();
		for(MethodDefinition md:methods.values())
			md.print(p);
	}

	public Multimap<String, MethodDefinition> getMethodDefinitions() {
		return methods;
	}
}

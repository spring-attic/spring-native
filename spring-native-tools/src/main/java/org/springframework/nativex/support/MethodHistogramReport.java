/*
 * Copyright 2019-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.nativex.support;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.springframework.nativex.support.MethodHistogram.Datum;


/**
 * Read a method histogram report and produce navigable tree for total code sizes.
 * 
 * @author Andy Clement
 */
public class MethodHistogramReport {

	public static void main(String[] args) throws IOException, URISyntaxException {
		if (args == null || args.length < 1) {
			System.out.println("Usage: MethodHistogramReport <inputDataLocation>");
			System.out.println("(It will process data captured via -H:+PrintMethodHistogram");
			System.exit(0);
		}
		MethodHistogram histogram = MethodHistogram.load(args[0]);
		System.out.println("Loaded "+histogram.getData().size()+" entries");
		for (int i=0;i<10;i++) {
			System.out.println(histogram.getData().get(i));
		}
		
		// Build a simple tree of the package name sections
		ReportNode root = new ReportNode();
		for (Datum d: histogram.getData()) {
			String method = d.getMethod();
			StringTokenizer st = new StringTokenizer(method, ".");
			String lastElement = "";
			ReportNode currentDepth = root;
			while (st.hasMoreElements()) {
				String nameElement = st.nextToken();
				// might be a package element (e.g. 'org') or type name (e.g. 'String')
				ReportNode exists = currentDepth.getChild(nameElement);
				if (exists != null) {
					// This piece already exists, just move into it
					currentDepth = exists;
				} else {
					// The leaf nodes have the size set to 'difference' computation
					ReportNode n = null;
					if (st.hasMoreTokens()) {
						n = new ReportNode(nameElement,-1);
					} else {
						n = new ReportNode(lastElement+"."+nameElement.replace("<", "&lt;").replace(">", "&gt;")
								,d.getCodeSize());
					}
					currentDepth.addChild(n);
					currentDepth = n;
				}
				lastElement = nameElement;
			}
		}

		
		// Collapse empty sections
		collapseCount = 0;
		collapse(null,root);
		System.out.println("Collapsed: "+collapseCount);
		
		walkGraphComputeSize(root);
		
		root.sortChildrenBySize();
		
		StringBuilder html = new StringBuilder();
		walkGraph(root, html);
		
		URI template = MethodHistogramReport.class.getResource("/template-histogram-diff.html").toURI();
		Map<String, String> env = new HashMap<>(); 
		env.put("create", "true");
		List<String> readAllLines = null;
		try {
			readAllLines= Files.readAllLines(Paths.get(template));
		} catch (FileSystemNotFoundException fsnfe) {
			String[] array = template.toString().split("!");
			FileSystem fs = FileSystems.newFileSystem(URI.create(array[0]), env);
			Path path = fs.getPath(array[1]);	
			readAllLines = Files.readAllLines(path);
		}
		
		List<String> lines = new ArrayList<>();
		for (String l: readAllLines)  {
			if (l.contains("Histogram Summary difference (output from -H:+PrintHeapHistogram)")) {
				lines.add(l.replace("Histogram Summary difference (output from -H:+PrintHeapHistogram)", "Method Histogram (-H:+PrintMethodHistogram)"));
			} else if (l.contains("TREE-GOES-HERE")) {
				lines.add(l.replace("TREE-GOES-HERE", ""+html.toString())+"");
			} else if (l.contains("HEADER")) {
				lines.add(l.replace("HEADER",
						"Method histogram for "+args[0]));
			} else {
				lines.add(l);
			}
		}
		String outputLocation = args[0];
		if (outputLocation.contains("/")) {
			outputLocation = outputLocation.substring(outputLocation.lastIndexOf("/")+1);
		}
		outputLocation+=".html";
		System.out.println("Output html method histogram: "+outputLocation);
		File outputHTML = new File(outputLocation);
		Files.write(outputHTML.toPath(),lines);
	}
	
	private static int collapseCount;

	private static void collapse(ReportNode parent, ReportNode node) {
		List<ReportNode> children = node.getChildren();
		if (children.size() == 1 && children.get(0).getChildren().size()!=0) {
			ReportNode singleChild = children.get(0);
			node.setData(node.getData()+"."+singleChild.getData());
			node.setChildren(singleChild.getChildren());
			collapseCount++;
			collapse(parent,node);
		} else {
			for (ReportNode child: children) {
				collapse(node, child);
			}
		}
	}
	private static void walkGraph(ReportNode node, StringBuilder json) {
		List<ReportNode> children = node.getChildren();
		if (node.getData()==null) { // root
			json.append("<ul id=\"root\">\n");
			for (int n=0;n<children.size();n++) {
				if (n>0) { json.append("\n"); }
				walkGraph(children.get(n),json);
			}
			json.append("</ul>\n");
		} else {
			if (children.size()==0) {
				json.append("<li>"+node.getData()+" ("+String.format("%,d", node.getSize())+"bytes)</li>");
			} else {
			String label = node.getData()+" ("+String.format("%,d",node.getSize())+" bytes)";//(children.size()!=0?":"+node.totalChildren():"");
			json.append("<li><span class=\"caret\">"+label+"</span>\n");
			if (children.size()!=0) {
				json.append("<ul class=\"nested\"\n");
				for (int n=0;n<children.size();n++) {
					walkGraph(children.get(n),json);
				}
				json.append("</ul>\n");
			}
			json.append("</li>");
			}
		}
	}

	private static void walkGraphComputeSize(ReportNode node) {
		List<ReportNode> children = node.getChildren();
		int totalSize = 0;
		for (ReportNode child: children) {
			walkGraphComputeSize(child);
			totalSize+= child.getSize();
		}
		if (node.getSize()==-1) {
			node.setSize(totalSize);
		}
	}
}

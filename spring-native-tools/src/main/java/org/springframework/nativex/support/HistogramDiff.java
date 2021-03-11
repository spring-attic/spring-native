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
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.springframework.nativex.support.Histogram.Datum;

/**
 * @author Andy Clement
 */
public class HistogramDiff {

	public static void main(String[] args) throws IOException, URISyntaxException {
		if (args == null || args.length < 3) {
			System.out.println("Usage: HistogramDiff <id>:<fileLocation> <id>:<fileLocation> <htmlDiffFileOutput>");
			System.out.println("e.g. HistogramDiff agent:/path/to/output.txt hybrid:/path/to/output.txt diff.html");
			System.exit(0);
		}
		int idxA = args[0].indexOf(":");
		int idxB = args[1].indexOf(":");
		if (idxA == -1 || idxB == -1) {
			System.out.println("Usage: HistogramDiff <id>:<fileLocation> <id>:<fileLocation> <htmlDiffFileOutput>");
			System.out.println("e.g. HistogramDiff agent:/path/to/output.txt hybrid:/path/to/output.txt diff.html");
			System.exit(0);
		}
		String idA = args[0].substring(0,idxA);
		String fileA = args[0].substring(idxA+1);
		String idB = args[1].substring(0,idxB);
		String fileB = args[1].substring(idxB+1);
		Histogram a = Histogram.load(idA,fileA);
		Histogram b = Histogram.load(idB,fileB);
		System.out.println(a.getData().size()+" entries loaded");
		System.out.println(b.getData().size()+" entries loaded");
		
		// Relevant diffs:
		// - what are in the first, not in the first, etc.
		// - types in both that have a big difference
		// - any packages that are only in one of them?
		
		Set<String> packagesInFirstAndNotInSecond = a.packagesNotIn(b);
		System.out.println("These packages are in the first and not the second:"+packagesInFirstAndNotInSecond.size());
		packagesInFirstAndNotInSecond.stream().forEach(System.out::println);
		Set<Datum> typesInFirstAndNotInSecond = a.typesNotIn(b);
		System.out.println("These types are in the first and not the second:"+typesInFirstAndNotInSecond.size());
		typesInFirstAndNotInSecond.stream().forEach(System.out::println);

		System.out.println("Comparing default vs agent");
		Set<String> packagesInSecondAndNotInFirst = b.packagesNotIn(a);
		System.out.println("These packages are in the second and not the first:"+packagesInSecondAndNotInFirst.size());
		packagesInSecondAndNotInFirst.stream().forEach(System.out::println);
		Set<Datum> typesInSecondAndNotInFirst = b.typesNotIn(a,true);
		System.out.println("These types are in the second and not the first:"+typesInSecondAndNotInFirst.size());
		typesInSecondAndNotInFirst.stream().forEach(System.out::println);

		System.out.println("XX These types (excluding reflection) are in the "+b.getName()+" and not in "+a.getName()+": "+typesInSecondAndNotInFirst.size());
		typesInSecondAndNotInFirst.stream().forEach(System.out::println);
		
		System.out.println("===");
		List<Datum> reflectiveA = a.getReflectiveEntries();
		System.out.println("Reflective in "+a.getName()+": "+reflectiveA.size()+"  Total bytes: "+reflectiveA.stream().mapToInt(Datum::getSize).sum());
		List<Datum> reflectiveB = b.getReflectiveEntries();
		System.out.println("Reflective in "+b.getName()+": "+reflectiveB.size()+"  Total bytes: "+reflectiveB.stream().mapToInt(Datum::getSize).sum());
		System.out.println("Total size for "+a.getName()+" = "+a.getData().stream().mapToInt(Datum::getSize).sum());
		System.out.println("Total size for "+b.getName()+" = "+b.getData().stream().mapToInt(Datum::getSize).sum());
		
		
		// What is 'bigger' in b - and how much is it bigger by
		List<Datum> sortedList = new ArrayList<>();
		sortedList.addAll(b.getData());
		Collections.sort(sortedList,(d1,d2) -> {
			int sizeOfD1_A = a.getSizeOf(d1.getClassname());
			int sizeOfD1_B = b.getSizeOf(d1.getClassname());
			int sizeOfD2_A = a.getSizeOf(d2.getClassname());
			int sizeOfD2_B = b.getSizeOf(d2.getClassname());
			
			int differenceD1 = (sizeOfD1_B-sizeOfD1_A);
			int differenceD2 = (sizeOfD2_B-sizeOfD2_A);
			if (differenceD2<differenceD1) {
				return -1; 
			} else if (differenceD2>differenceD1) {
				return 1;
			} else {
				return 0;
			}
		});
		
		Map<String,Integer> differences = new LinkedHashMap<>();
		Iterator<Datum> di = sortedList.iterator();
		while (di.hasNext()) {
			Datum d = di.next();
			int sizeOfD_A = a.getSizeOf(d.getClassname());
			int sizeOfD_B = b.getSizeOf(d.getClassname());
			int differenceD = (sizeOfD_B-sizeOfD_A);
			if (differenceD == 0) {
				// Consuming same amount of space in both histograms
				di.remove();
			}
			differences.put(d.getClassname(), differenceD);
		}

		
		// Build a simple tree of the package name sections
		ReportNode root = new ReportNode();
		for (Datum d: sortedList) {
			String typename = d.getClassname();
			StringTokenizer st = new StringTokenizer(typename, ".");
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
					System.out.println("differences.get for "+typename+" = "+differences.get(typename));
					ReportNode n = new ReportNode(nameElement,st.hasMoreTokens()?-1:differences.get(typename));
					currentDepth.addChild(n);
					currentDepth = n;
				}
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
		
		URI template = HistogramDiff.class.getResource("/template-histogram-diff.html").toURI();
		Map<String, String> env = new HashMap<>(); 
		env.put("create", "true");
		FileSystem zipfs = FileSystems.newFileSystem(template, env);
		List<String> readAllLines= Files.readAllLines(Paths.get(template));
		List<String> lines = new ArrayList<>();
		for (String l: readAllLines)  {
			if (l.contains("TREE-GOES-HERE")) {
				lines.add(l.replace("TREE-GOES-HERE", ""+html.toString())+"");
			} else if (l.contains("HEADER")) {
				lines.add(l.replace("HEADER",
						"What is consuming more data in "+b.getName()+" than in "+a.getName()));
			} else {
				lines.add(l);
			}
		}
		File outputHTML = new File(args[2]);
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
				json.append("<li>"+node.getData()+" ("+node.getSize()+"bytes)</li>");
			} else {
			String label = node.getData()+" ("+node.getSize()+"bytes)";//(children.size()!=0?":"+node.totalChildren():"");
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

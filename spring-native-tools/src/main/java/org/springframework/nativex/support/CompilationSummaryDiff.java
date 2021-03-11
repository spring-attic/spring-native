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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.nativex.support.CompilationSummary.Compiled;

/**
 * Compute the differences between the 'Compiled...' messages for two compilation reports (allowing comparison
 * between two native-image builds). The first two parameters are the files containing the Compiled... lines
 * whilst the third parameter, if supplied, will be where a HTML tree view of the diff will be created.
 * 
 * @author Andy Clement
 */
public class CompilationSummaryDiff {

	public static void main(String[] args) throws IOException, URISyntaxException {
		if (args == null || args.length < 2) {
			System.out.println("Usage: CompilationSummaryDiff <fileLocation>[:<id>] <fileLocation>[:<id>] [htmlDiffFile]");
			System.out.println("e.g. CompilationSummaryDiff agent:/path/to/output.txt hybrid:/path/to/output.txt diff.html");
			System.exit(0);
		}
		int idxA = args[0].indexOf(":");
		int idxB = args[1].indexOf(":");
		String fileA,idA,fileB,idB;
		if (idxA == -1) {
			fileA = args[0];
			idA = fileA;
		} else {
			fileA = args[0].substring(0,idxA);
			idA = args[0].substring(idxA+1);
		}
		if (idxB == -1) {
			fileB = args[1];
			idB = fileB;
		} else {
			fileB = args[1].substring(0,idxB);
			idB = args[1].substring(idxB+1);
		}

		CompilationSummary a = CompilationSummary.load(idA,fileA);
		CompilationSummary b = CompilationSummary.load(idB,fileB);

		System.out.println("Compiled entries in "+a.getId()+" = "+a.getData().size());
		System.out.println("Compiled entries in "+b.getId()+" = "+b.getData().size());
		
		// Maps from packages to number of types in that package
		Map<String, Long> packagesA = a.getData().stream().map(Compiled::getPackageName)
			.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
		Map<String, Long> packagesB = b.getData().stream().map(Compiled::getPackageName)
			.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
		System.out.println("Number of packages in "+a.getId()+": "+packagesA.size());
		System.out.println("Number of packages in "+b.getId()+": "+packagesB.size());
		
		List<String> typenamesA = a.getData().stream().map(Compiled::getType).collect(Collectors.toList());
		List<String> typenamesB = b.getData().stream().map(Compiled::getType).collect(Collectors.toList());
		Set<String> diff = null;

		diff = new HashSet<>();
		diff.addAll(typenamesA);
		diff.removeAll(typenamesB);
		System.out.println("Types in "+a.getId()+" but not in "+b.getId()+" = "+diff.size());

		diff = new HashSet<>();
		diff.addAll(typenamesB);
		diff.removeAll(typenamesA);
		System.out.println("Types in "+b.getId()+" but not in "+a.getId()+" = "+diff.size());
		
		// Remove Lambdas$$
		Iterator<String> iterator = diff.iterator();
		while (iterator.hasNext()) {
			String next = iterator.next();
			if (next.contains("$$Lambda$")) {
				iterator.remove();
			}
		}
		
//		// Add in the 'why'
//		Set<String> diff2 = new HashSet<>();
//		iterator = diff.iterator();
//		while (iterator.hasNext()) {
//			String typename = iterator.next();
//			String reason = b.getReason(typename);
//			System.out.println("Reason for "+typename+" is "+reason);
//			diff2.add(typename+"::"+reason.replace(".", "·"));
//		}
//		diff = diff2;
		
		// Build a simple tree of the package name sections
		ReportNode root = new ReportNode();
		for (String typename: diff) {
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
					ReportNode n = new ReportNode(nameElement,0);
					currentDepth.addChild(n);
					currentDepth = n;
				}
			}
		}

		
		// Collapse empty sections
		collapseCount = 0;
		collapse(null,root);
		System.out.println("Collapsed: "+collapseCount);
		
		root.sortChildren();
		
		StringBuilder html = new StringBuilder();
		walkGraph(root, html);
		
		URI template = CompilationSummary.class.getResource("/template-compilation-diff.html").toURI();
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
			if (l.contains("TREE-GOES-HERE")) {
				lines.add(l.replace("TREE-GOES-HERE", ""+html.toString())+"");
			} else if (l.contains("HEADER")) {
				lines.add(l.replace("HEADER",
						"Types in "+b.getId()+" but not in "+a.getId()+" = "+diff.size()));
			} else {
				lines.add(l);
			}
		}
		String fname = null;
		if (args.length<3) {
			StringBuilder s = new StringBuilder();
			int i;
			if (!idA.equals("-")) {
				i = idA.lastIndexOf("/");
				s.append(i==-1?idA:idA.substring(i+1));
				s.append("-");
			}
			i = idB.lastIndexOf("/");
			s.append(i==-1?idB:idB.substring(i+1));
			s.append(".html");
			fname = s.toString();
			System.out.println("Dumping html report to "+fname);
		} else {
			fname = args[2];
		}
		File outputHTML = new File(fname);
		Files.write(outputHTML.toPath(),lines);
	}
	
	private static int collapseCount;

	private static void collapse(ReportNode parent, ReportNode node) {
		List<ReportNode> children = node.getChildren();
		if (children.size() == 1 && children.get(0).getChildren().size()!=0) {
			ReportNode singleChild = children.get(0);
			node.setData((parent==null?"":node.getData()+".")+singleChild.getData());
			node.setChildren(singleChild.getChildren());
			collapseCount++;
			collapse(parent,node);
		} else {
			for (ReportNode child: children) {
				collapse(node, child);
			}
		}
	}

//	<ul id="myUL">
//	  <li><span class="caret">Beverages</span>
//	    <ul class="nested">
//	      <li>Water</li>
//	      <li>Coffee</li>
//	      <li><span class="caret">Tea</span>
//	        <ul class="nested">
//	          <li>Black Tea</li>
//	          <li>White Tea</li>
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
				json.append("<li>"+node.getData()+"</li>");
			} else {
			String label = node.getData()+(children.size()!=0?":"+node.totalChildren():"");
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
}

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class ReportNode {
	private String data;
	private int size;
	private List<ReportNode> children = new ArrayList<>();
	
	ReportNode() {
	}

	public void sortChildren() {
		if (children.size()>1) {
			Collections.sort(children,(n1,n2) -> {
				int score = n2.totalChildren()-n1.totalChildren();
				if (score !=0) {
					return score;
				}
				return n1.data.compareTo(n2.data);
			});
			for (ReportNode c: children) {
				c.sortChildren();
			}
		}
	}

	public void sortChildrenBySize() {
		if (children.size()>1) {
			Collections.sort(children,(n1,n2) -> {
				return n2.getSize()-n1.getSize();
			});
			for (ReportNode c: children) {
				c.sortChildrenBySize();
			}
		}
	}
	
	public int getSize() {
		return size;
	}
	
	public void setSize(int s) {
		this.size = s;
	}

	public int totalSize() {
		if (children.size()==0) {
			return size;
		} else {
			int totalsize = 0;
			for (ReportNode c: children) {
				totalsize+=c.totalSize();
			}
			return totalsize;
		}
	}
	public int totalChildren() {
		int totalkids = 0;
		if (children.size()==0) {
			return 1;
		} else {
			for (ReportNode c: children) {
				totalkids+=c.totalChildren();
			}
		}
		return totalkids;
	}
	public void setData(String string) {
		this.data = string;
		
	}
	public void setChildren(List<ReportNode> children2) {
		this.children = children2;
	}
	public String getData() {
		return this.data;
	}
	ReportNode(String data,int size) {
		this.data = data;
		this.size = size;
	}
	
	void addChild(ReportNode n) {
		this.children.add(n);
	}
	
	ReportNode getChild(String data) {
		for (ReportNode n: children) {
			if (n.data.equals(data)) {
				return n;
			}
		}
		return null;
	}
	
	List<ReportNode> getChildren() {
		return this.children;
	}
	
}
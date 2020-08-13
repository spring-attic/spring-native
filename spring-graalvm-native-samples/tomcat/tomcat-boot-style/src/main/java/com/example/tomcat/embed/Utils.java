package com.example.tomcat.embed;

import java.util.Collection;
import java.util.Iterator;

public class Utils {
	public static String collectionToDelimitedString(Collection<?> coll, String delim, String prefix, String suffix) {
		if (coll == null || coll.isEmpty()) {
			return "";
		} else {
			StringBuilder sb = new StringBuilder();
			Iterator it = coll.iterator();

			while(it.hasNext()) {
				sb.append(prefix).append(it.next()).append(suffix);
				if (it.hasNext()) {
					sb.append(delim);
				}
			}

			return sb.toString();
		}
	}
}

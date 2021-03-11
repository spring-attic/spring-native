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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/*
 * 
 * @author Andy Clement
 */
public class CallTreeQuery {

	public static void main(String[] args) throws IOException, URISyntaxException {
		if (args == null || args.length < 3) {
			System.out.println(
					"Usage: CallTreeQuery <fileLocation> [options] command parameter");
			System.out.println(
					"e.g. CallTreeQuery /path/to/output.txt --ignore-overridden routes Foo.toString");
			System.exit(0);
		}
		String file, command, parameter;
		file = args[0];
		int commandIndex = 1;
		List<String> options = new ArrayList<>();
		while (args[commandIndex].startsWith("--")) {
			options.add(args[commandIndex++]);
		}
		command = args[commandIndex];
		parameter = args[commandIndex+1];
		
		CallTree a = CallTree.load("", file);
		int flags = 0;
		if (options.contains("--ignore-overridden")) {
			flags|=CallTree.IGNORE_OVERRIDDEN;
		}

		switch (command) {
		case "routes": 
			System.out.println("Printing routes to "+parameter);
			a.printRoutes(parameter, flags);
			break;
			default:
				throw new IllegalStateException("Don't understand command: "+args[1]);
		}
	}
}

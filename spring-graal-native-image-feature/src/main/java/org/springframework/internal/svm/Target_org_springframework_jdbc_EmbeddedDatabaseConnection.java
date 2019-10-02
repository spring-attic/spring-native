///*
// * Copyright 2019 Contributors
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      https://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package org.springframework.internal.svm;
//
//import org.springframework.beans.BeansException;
//import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
//
//import com.oracle.svm.core.annotate.Substitute;
//import com.oracle.svm.core.annotate.TargetClass;
//
///**
// * Substitution for EmbeddedDatabaseConnection. 
// * 
// * @author Andy Clement
// */
//@TargetClass(org.springframework.boot.jdbc.EmbeddedDatabaseConnection.class)
//public final class Target_org_springframework_jdbc_EmbeddedDatabaseConnection {
//
//	// working around Graal #1196
//	@Substitute
//	public EmbeddedDatabaseConnection[] values() throws BeansException {
//		return new EmbeddedDatabaseConnection[0];
//	}
//}
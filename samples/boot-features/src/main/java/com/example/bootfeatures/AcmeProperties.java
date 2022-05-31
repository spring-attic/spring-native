/*
 * Copyright 2019-2022 the original author or authors.
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

package com.example.bootfeatures;

 import java.net.InetAddress;

 import org.springframework.boot.context.properties.ConfigurationProperties;

 @ConfigurationProperties("acme")
 public class AcmeProperties {

     private final boolean enabled;

     private final InetAddress remoteAddress;

     private final Security security;

     public boolean isEnabled() {
         return enabled;
     }

     public InetAddress getRemoteAddress() {
         return remoteAddress;
     }

     public Security getSecurity() {
         return security;
     }

     public AcmeProperties(boolean enabled, InetAddress remoteAddress, Security security) {
         this.enabled = enabled;
         this.remoteAddress = remoteAddress;
         this.security = security;
     }

     @Override
     public String toString() {
         return enabled+" "+remoteAddress+" "+security;
     }
 } 
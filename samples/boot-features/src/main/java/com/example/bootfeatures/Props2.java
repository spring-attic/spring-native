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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix="funky")
public class Props2 {
    
    Color color;
    
    Resource location;
    
    public Props2(Color color, Resource location) {
        this.color = color;
        this.location = location;
    }
    
    public Resource getLocation() {
        return location;
    }
    
    public Color getColor() {
        return color;
    }
    
    @Override
    public String toString() {
        return color+" "+location;
    }
}

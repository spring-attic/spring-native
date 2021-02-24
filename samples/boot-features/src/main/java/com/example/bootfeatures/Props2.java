package com.example.bootfeatures;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix="funky")
@ConstructorBinding
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

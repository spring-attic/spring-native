package com.example.bootfeatures.nestedcfg;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "school")
@ConstructorBinding
public class School {
    
    List<Kid> kids;
 
    public School(List<Kid> kids) {
        this.kids = kids;
    }
    
    public List<Kid> getKids() {
        return kids;
    }
    
    public void setKids(List<Kid> kids) {
        this.kids = kids;
    }
    
    @Override
    public String toString() {
        return "School"+kids;
    }
    
    @ConstructorBinding
    static class Kid {
        
        String firstname;
        String lastname;
        
        public Kid(String firstname, String lastname) {
            this.firstname = firstname;
            this.lastname = lastname;
        }
        
        public String getFirstname() {
            return firstname;
        }
        
        public void setFirstname(String firstname) {
            this.firstname = firstname;
        }
        
        public String getLastname() {
            return lastname;
        }
        
        public void setLastname(String lastname) {
            this.lastname = lastname;
        }
        
        @Override
        public String toString() {
            return firstname+" "+lastname;
        }
        
    }
 
}
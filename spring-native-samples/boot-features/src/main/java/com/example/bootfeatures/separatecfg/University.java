package com.example.bootfeatures.separatecfg;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "university")
@ConstructorBinding
public class University {
    
    List<Student> students;
 
    public University(List<Student> students) {
        this.students = students;
    }
 
    public List<Student> getStudents() {
        return students;
    }
    
  //  public void setStudents(List<Student> students) {
   //     this.students = students;
    //}
    
    @Override
    public String toString() {
        return "University"+students;
    }
 
}
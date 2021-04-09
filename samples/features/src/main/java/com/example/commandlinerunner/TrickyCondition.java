package com.example.commandlinerunner;

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

public class TrickyCondition extends AnyNestedCondition {
   private TrickyCondition() {
       super(ConfigurationPhase.REGISTER_BEAN);
   } 
   
   @ConditionalOnClass(String.class)
   static class Inner {
       Inner() {
           
       }
       
   }
    
    
}

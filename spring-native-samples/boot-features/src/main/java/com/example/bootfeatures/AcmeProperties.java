package com.example.bootfeatures;

 import java.net.InetAddress;
 import java.util.List;

 import org.springframework.boot.context.properties.ConfigurationProperties;
 import org.springframework.boot.context.properties.ConstructorBinding;
 import org.springframework.boot.context.properties.bind.DefaultValue;

 @ConstructorBinding
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
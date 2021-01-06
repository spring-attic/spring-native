Very basic Spring Boot project using a `@Validated` `@ConfigurationProperties` bean.

To build and run the native application packaged in a lightweight container:
```
mvn spring-boot:build-image
docker-compose up
```

## Validator Usage in Spring Boot Applications

There are three main ways that users plug `javax.validation` (and the Spring flavours of the same thing) into their applications:

* `@Validated` on a `@Component` class - Spring Framework creates a proxy for the bean and validates all method calls on the proxy at runtime. Not widely used, but apparently the majority use case is for MVC controllers to bake in validation in a more transparent way (debatable). Currently impossible to support in a native image, but could be done relatively straightforwardly by generating subclasses at build time in Spring Init.

* `@Validated` on a `@ConfigurationProperties` class - Spring Boot validates the bean after binding to the `Environment`. No proxies involved, but the presence of the `@Validated` annotation causes proxies to be created unless you switch that feature off. Spring Boot controls the bean post processor for this in `ValidationAutoConfiguration` and it isn't conditional, so the only ways to switch it off are to exclude `ValidationAutoConfiguration` or to remove the bean post processor manually in a `BeanDefinitionRegistryPostProcessor`. The latter is the approach this sample takes currently.

* `@Valid` annotations on `@Controller` methods - Spring Framework binds the validation result to the model so it can be rendered. Obviously it needs annotation metadata to do that, but it's not very complex metadata - it's just the presence of absence of the `@Valid` annotation. In a functional controller the validator could be used imperatively, or the proxy approach with `@Validated` might work.

Obviously, since it is an annotation-based programming model, you need reflection access to the validated object classes (some of which are provided by Spring Boot and libraries that depend on it).

In all cases where `javax.validation` is used we rely on Hibernate Validator as the primary implementation (practically speaking it's the only implementation). Hibernate registers a lot of annotations and matches them up with `ConstraintValidator` implementations, but only stores the mapping from annotation class to validator class (not the instances). Instance creation is delegated to a `ConstraintValidatorFactory` and in a Spring Boot application that means usually a `SpringConstraintValidatorFactory` which uses the `ApplicationContext` to create instances of the validators, reflectively. We could plug in a non-reflective version of that, but it might not be practical or efficient to register instances of *all* the validators on the classpath - the vast majority of them will not be used at runtime.
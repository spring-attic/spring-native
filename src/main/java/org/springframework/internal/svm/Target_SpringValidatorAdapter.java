package org.springframework.internal.svm;

import com.oracle.svm.core.annotate.*;
import com.oracle.svm.core.annotate.RecomputeFieldValue.Kind;

import org.springframework.validation.Errors;

@TargetClass(className="org.springframework.validation.beanvalidation.SpringValidatorAdapter", onlyWith = OnlyPresent.class)
public final class Target_SpringValidatorAdapter {

	@Substitute
	public void validate(Object target, Errors errors) {
        // skip it due to:
        
        /*
        https://github.com/oracle/graal/issues/1685
java.lang.UnsatisfiedLinkError: java.lang.reflect.Field.getTypeAnnotationBytes0()[B [symbol: Java_java_lang_reflect_Field_getTypeAnnotationBytes0 or Java_java_lang_reflect_Field_getTypeAnnotationBytes0__]
at com.oracle.svm.jni.access.JNINativeLinkage.getOrFindEntryPoint(JNINativeLinkage.java:145)
at com.oracle.svm.jni.JNIGeneratedMethodSupport.nativeCallAddress(JNIGeneratedMethodSupport.java:57)
at java.lang.reflect.Field.getTypeAnnotationBytes0(Field.java)
at java.lang.reflect.Field.getAnnotatedType(Field.java:1170)
at org.hibernate.validator.internal.metadata.provider.AnnotationMetaDataProvider.findCascadingMetaData(AnnotationMetaDataProvider.java:618)
at org.hibernate.validator.internal.metadata.provider.AnnotationMetaDataProvider.findPropertyMetaData(AnnotationMetaDataProvider.java:236)
at org.hibernate.validator.internal.metadata.provider.AnnotationMetaDataProvider.getFieldMetaData(AnnotationMetaDataProvider.java:225)
at org.hibernate.validator.internal.metadata.provider.AnnotationMetaDataProvider.retrieveBeanConfiguration(AnnotationMetaDataProvider.java:133)
at org.hibernate.validator.internal.metadata.provider.AnnotationMetaDataProvider.getBeanConfiguration(AnnotationMetaDataProvider.java:124)
at org.hibernate.validator.internal.metadata.BeanMetaDataManager.getBeanConfigurationForHierarchy(BeanMetaDataManager.java:232)
at org.hibernate.validator.internal.metadata.BeanMetaDataManager.createBeanMetaData(BeanMetaDataManager.java:199)
at org.hibernate.validator.internal.metadata.BeanMetaDataManager.getBeanMetaData(BeanMetaDataManager.java:166)
at org.hibernate.validator.internal.engine.ValidatorImpl.validate(ValidatorImpl.java:157)
at org.springframework.validation.beanvalidation.SpringValidatorAdapter.validate(SpringValidatorAdapter.java:108)
at org.springframework.validation.DataBinder.validate(DataBinder.java:870)
at org.springframework.web.reactive.result.method.annotation.ModelAttributeMethodArgumentResolver.validateIfApplicable(ModelAttributeMethodArgumentResolver.java:280)
at org.springframework.web.reactive.result.method.annotation.ModelAttributeMethodArgumentResolver.lambda$null$2(ModelAttributeMethodArgumentResolver.java:128)
at reactor.core.publisher.MonoPeekTerminal$MonoTerminalPeekSubscriber.onComplete(MonoPeekTerminal.java:310)
*/
        
    }
}

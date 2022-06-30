package org.springframework.aot.thirdpartyhints;

import java.util.Set;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.util.ClassUtils;

/**
 * @author Moritz Halbritter
 */
// TODO: Contribute these hints to graalvm-reachability-metadata repository
public class ThymeleafRuntimeHints implements RuntimeHintsRegistrar {
    private static final Set<String> EXPRESSIONS = Set.of(
            "org.thymeleaf.standard.expression.AdditionExpression",
            "org.thymeleaf.standard.expression.AndExpression",
            "org.thymeleaf.standard.expression.BooleanTokenExpression",
            "org.thymeleaf.standard.expression.ConditionalExpression",
            "org.thymeleaf.standard.expression.DefaultExpression",
            "org.thymeleaf.standard.expression.DivisionExpression",
            "org.thymeleaf.standard.expression.EqualsExpression",
            "org.thymeleaf.standard.expression.FragmentExpression",
            "org.thymeleaf.standard.expression.GenericTokenExpression",
            "org.thymeleaf.standard.expression.GreaterOrEqualToExpression",
            "org.thymeleaf.standard.expression.GreaterThanExpression",
            "org.thymeleaf.standard.expression.LessOrEqualToExpression",
            "org.thymeleaf.standard.expression.LessThanExpression",
            "org.thymeleaf.standard.expression.LinkExpression",
            "org.thymeleaf.standard.expression.MessageExpression",
            "org.thymeleaf.standard.expression.MinusExpression",
            "org.thymeleaf.standard.expression.MultiplicationExpression",
            "org.thymeleaf.standard.expression.NegationExpression",
            "org.thymeleaf.standard.expression.NoOpTokenExpression",
            "org.thymeleaf.standard.expression.NotEqualsExpression",
            "org.thymeleaf.standard.expression.NullTokenExpression",
            "org.thymeleaf.standard.expression.NumberTokenExpression",
            "org.thymeleaf.standard.expression.OrExpression",
            "org.thymeleaf.standard.expression.RemainderExpression",
            "org.thymeleaf.standard.expression.SelectionVariableExpression",
            "org.thymeleaf.standard.expression.SubtractionExpression",
            "org.thymeleaf.standard.expression.TextLiteralExpression",
            "org.thymeleaf.standard.expression.VariableExpression"
    );

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        if (!ClassUtils.isPresent("org.thymeleaf.spring6.ISpringTemplateEngine", classLoader)) {
            return;
        }
        hints.reflection().registerType(TypeReference.of("org.thymeleaf.spring6.view.ThymeleafView"), hint -> hint.withMembers(MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS));
        hints.reflection().registerType(TypeReference.of("org.thymeleaf.spring6.view.reactive.ThymeleafReactiveView"), hint -> hint.withMembers(MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS));
        hints.reflection().registerType(TypeReference.of("org.thymeleaf.spring6.expression.Mvc$Spring41MvcUriComponentsBuilderDelegate"), hint -> hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS));
        EXPRESSIONS.forEach(expression -> hints.reflection().registerType(TypeReference.of(expression), hint -> hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)));
        hints.reflection().registerType(TypeReference.of("org.thymeleaf.engine.IterationStatusVar"), hint -> hint.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS));
        hints.reflection().registerType(TypeReference.of("org.thymeleaf.extras.springsecurity6.util.Spring6VersionSpecificUtility"), hint -> hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS));
        // TODO: This is used by Thymeleaf but belongs to Spring Security - what to do with it?
        hints.reflection().registerType(TypeReference.of("org.springframework.security.authentication.UsernamePasswordAuthenticationToken"), hint -> hint.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS));
        hints.reflection().registerType(TypeReference.of("org.springframework.security.core.userdetails.User"), hint -> hint.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS));
        // TODO: This should really be in Spring Framework (or in Boot?)
        hints.resources().registerPattern("templates/*.html");
    }
}

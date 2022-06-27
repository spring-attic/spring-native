package com.example.graalvmdemo.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import org.springframework.stereotype.Component;

/**
 * @author Moritz Halbritter
 */
@Aspect
@Component
public class TestAspect {
    @Around("pointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        joinPoint.proceed();
        return "A-from-aspect";
    }

    @Pointcut("execution(* com.example.graalvmdemo.service.Test*.methodA(..))")
    private void pointcut() {
    }

}

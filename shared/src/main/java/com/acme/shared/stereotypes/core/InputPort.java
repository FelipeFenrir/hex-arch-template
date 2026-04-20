package com.acme.shared.stereotypes.core;

import com.acme.shared.enumerator.PortType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InputPort {
    PortType type() default PortType.INPUT;
}

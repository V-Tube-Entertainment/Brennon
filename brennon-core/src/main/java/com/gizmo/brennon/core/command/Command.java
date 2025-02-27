package com.gizmo.brennon.core.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    String name();
    String[] aliases() default {};
    String description() default "";
    String permission() default "";
    String usage() default "";
    boolean async() default false;
    boolean requiresPlayer() default false;
}

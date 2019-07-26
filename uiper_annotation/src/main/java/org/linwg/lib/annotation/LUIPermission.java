package org.linwg.lib.annotation;

import org.linwg.lib.IPerGrant;
import org.linwg.lib.PerRelation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author adr
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface LUIPermission {

    String[] per();

    boolean actingOnClick() default false;

    String toastHint() default "";

    PerRelation relation() default PerRelation.AND;

    Class<? extends IPerGrant> grantStrategy();
}

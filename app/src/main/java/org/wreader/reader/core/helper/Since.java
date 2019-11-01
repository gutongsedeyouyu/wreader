package org.wreader.reader.core.helper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD})
public @interface Since {
    String version() default "";
}

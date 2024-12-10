package top.tslj.dsb2s;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(DemoConfiguration.class)
public @interface EnableDemo {
}

package github.rpc.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface RpcReference {
    // 区分同一接口的不同实现类
    String group() default "";

    // 同一实现类可以给定不同版本
    String version() default  "";

    String value() default "";
}

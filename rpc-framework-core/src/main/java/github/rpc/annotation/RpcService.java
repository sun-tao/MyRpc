package github.rpc.annotation;

import org.checkerframework.checker.units.qual.C;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Component
/*
给 接口的实现类进行注册
*/
public @interface RpcService {
    // 区分同一接口的不同实现类
    String group() default "";

    // 同一实现类可以给定不同版本
    String version() default  "";

    // 服务权重
    String value() default "";
}

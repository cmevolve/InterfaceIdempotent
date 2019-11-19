package com.lfc.springboot1.annotation;

import com.lfc.springboot1.common.ConstantEnum;
import com.lfc.springboot1.common.LockStatus;
import com.lfc.springboot1.exception.DistributionLockException;
import com.lfc.springboot1.service.DistributionLockService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 分布式锁切面
 *
 * @author lfc
 * @create 2019/11/14 16:41
 * @since 1.0.0
 */
@Aspect
@Component
@Slf4j
@Lazy(false)
public class InterfaceIdempotenceAspect {

    @Autowired
    DistributionLockService distributionLockService;
    /**
     * 定义切入点：对要拦截的方法进行定义与限制，如包、类
     *
     * 1、execution(public * *(..)) 任意的公共方法
     * 2、execution（* set*（..）） 以set开头的所有的方法
     * 3、execution（* com.lingyejun.annotation.LoggerApply.*（..））com.lingyejun.annotation.LoggerApply这个类里的所有的方法
     * 4、execution（* com.lingyejun.annotation.*.*（..））com.lingyejun.annotation包下的所有的类的所有的方法
     * 5、execution（* com.lingyejun.annotation..*.*（..））com.lingyejun.annotation包及子包下所有的类的所有的方法
     * 6、execution(* com.lingyejun.annotation..*.*(String,?,Long)) com.lingyejun.annotation包及子包下所有的类的有三个参数，第一个参数为String类型，第二个参数为任意类型，第三个参数为Long类型的方法
     * 7、execution(@annotation(com.lingyejun.annotation.Lingyejun))
     */
    @Pointcut("@annotation(com.lfc.springboot1.annotation.InterfaceIdempotence)")
    private void cutMethod() {}

    /**
     * 前置通知：在目标方法执行前调用
     */
//    @Before("cutMethod()")
//    public void begin() {
//        log.info("==@Before== InterfaceIdempotence blog logger : begin");
//    }

    /**
     * 后置通知：在目标方法执行后调用，若目标方法出现异常，则不执行
     */
//    @AfterReturning("cutMethod()")
//    public void afterReturning() {
//        log.info("==@AfterReturning== InterfaceIdempotence blog logger {}: after returning","后置通知");
//    }

    /**
     * 后置/最终通知：无论目标方法在执行过程中出现一场都会在它之后调用
     */
//    @After("cutMethod()")
//    public void after() {
//        log.info("==@After== InterfaceIdempotence blog logger :{} finally returning","后置/最终通知");
//    }

    /**
     * 异常通知：目标方法抛出异常时执行
     */
//    @AfterThrowing("cutMethod()")
//    public void afterThrowing() {
//        log.info("==@AfterThrowing== InterfaceIdempotence blog logger {} after throwing","异常通知");
//    }

    /**
     * 环绕通知：灵活自由的在目标方法中切入代码
     */
    @Around("cutMethod()")
    public LockStatus around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取目标方法的名称
        String methodName = joinPoint.getSignature().getName();
        // 获取方法传入参数
        Object[] params = joinPoint.getArgs();
        InterfaceIdempotence interfaceIdempotence = getDeclaredAnnotation(joinPoint);
        log.debug("==@Around== interfaceIdempotence-->>>>>> method name {} ",methodName);
        checkParams(params);
        //加锁
        LockStatus lockStatus = distributionLockService.createLock((String)params[0],interfaceIdempotence.expireTime());
        if(ConstantEnum.P_200.getCode() == lockStatus.getCode()){
            // 执行源方法
            LockStatus ret = (LockStatus)joinPoint.proceed();
            log.debug("InterfaceIdempotence AOP 执行完方法返回值为：{}",ret.toString());
            lockStatus = distributionLockService.unLock((String)params[0],lockStatus.getToken());
            //设置返回值
            lockStatus.setData(null == ret ? ret : ret.getData());
        }
        //throw new DistributionLockException("获取锁异常！");
        return lockStatus;
    }

    /**
     * 校验入参 和返回值类型
     * @param params
     */
    public void checkParams(Object[] params){
        if(null == params || params.length < 1){
            throw new DistributionLockException("入参为空，不得使用InterfaceIdempotence注解");
        }
        if(!String.class.equals(params[0].getClass()) || StringUtils.isBlank((String) params[0])){
            throw new DistributionLockException("InterfaceIdempotence第一位入参为String类型的唯一业务号，且不得为空");
        }
    }
    /**
     * 获取方法中声明的注解
     *
     * @param joinPoint
     * @return
     * @throws NoSuchMethodException
     */
    public InterfaceIdempotence getDeclaredAnnotation(ProceedingJoinPoint joinPoint) throws NoSuchMethodException {
        // 获取方法名
        String methodName = joinPoint.getSignature().getName();
        // 反射获取目标类
        Class<?> targetClass = joinPoint.getTarget().getClass();
        // 拿到方法对应的参数类型
        Class<?>[] parameterTypes = ((MethodSignature) joinPoint.getSignature()).getParameterTypes();
        // 根据类、方法、参数类型（重载）获取到方法的具体信息
        Method objMethod = targetClass.getMethod(methodName, parameterTypes);
        // 拿到方法定义的注解信息
        InterfaceIdempotence annotation = objMethod.getDeclaredAnnotation(InterfaceIdempotence.class);
        // 返回
        return annotation;
    }
}
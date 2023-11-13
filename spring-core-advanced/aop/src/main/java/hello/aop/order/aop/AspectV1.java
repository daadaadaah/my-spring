package hello.aop.order.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;


@Slf4j
@Aspect
public class AspectV1 {

    @Around("hello.aop.order.aop.Pointcuts.allOrder()")
    public Object doLog(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("[log] {}", joinPoint.getSignature());
        return joinPoint.proceed();
    }

    // hello.aop.order 패키지와 하위 패키지 + 클래스 이름 패턴이 *Service
    @Before("hello.aop.order.aop.Pointcuts.orderAndService()")
    public void doBefore (JoinPoint joinPoint) {
        log.info("[@before] 트랜잭션 시작 {}", joinPoint.getSignature());
    }

    @AfterReturning(value = "hello.aop.order.aop.Pointcuts.orderAndService()", returning = "result")
    public void doReturn (JoinPoint joinPoint, Object result){
        log.info("[@AfterReturning] 트랜잭션 커밋 {} return={}", joinPoint.getSignature(), result);
    }

    @AfterThrowing(value = "hello.aop.order.aop.Pointcuts.orderAndService()", throwing = "ex")
    public void doThrowing (JoinPoint joinPoint, Exception ex) {
        log.info("[@AfterThrowing] 트랜잭션 롤백 {} message={}", joinPoint.getSignature(), ex.getMessage());
    }

    @After(value = "hello.aop.order.aop.Pointcuts.orderAndService()")
    public void doAfter (JoinPoint joinPoint) {
        log.info("[@After] 리소스 릴리즈 {}", joinPoint.getSignature());
    }
}
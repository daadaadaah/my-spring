package hello.aop.order.aop;

import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;

public class Pointcuts {

    // hello.aop.order 패키지와 하위 패키지
    @Pointcut("execution(* hello.aop.order..*(..))") // @Pointcut 에 포인트컷 표현식을 사용한다.
    public void allOrder() {} // allOrder() : pointcut signature

    // 클래스 이름 패턴이 *Service
    @Pointcut("execution(* *..*Service.*(..))")
    public void allService() {}


    // allOrder 와 allService 조합
    @Pointcut("allOrder() && allService()")
    public void orderAndService() {}

}

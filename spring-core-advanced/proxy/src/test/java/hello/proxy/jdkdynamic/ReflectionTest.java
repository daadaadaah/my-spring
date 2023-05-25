package hello.proxy.jdkdynamic;

import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class ReflectionTest {
    @Test
    void reflection() throws Exception {
        //클래스 정보
        Class classHello = Class.forName("hello.proxy.jdkdynamic.ReflectionTest$Hello");

        Hello target = new Hello();

        //공통 로직1 시작
        Method methodCallA = classHello.getMethod("callA");
        dynamicCall(methodCallA, target);
        //공통 로직1 종료

        //공통 로직2 시작
        Method methodCallB = classHello.getMethod("callB");
        dynamicCall(methodCallB, target);
        //공통 로직2 종료
    }

    private void dynamicCall(Method method, Object target) throws Exception {
        //공통 로직 시작
        log.info("start");
        Object result = method.invoke(target);
        log.info("result={}", result);
        //공통 로직 종료
    }

    @Slf4j
    static class Hello {
        public String callA() {
            log.info("callA");
            return "A";
        }
        public String callB() {
            log.info("callB");
            return "B";
        }
    }
}

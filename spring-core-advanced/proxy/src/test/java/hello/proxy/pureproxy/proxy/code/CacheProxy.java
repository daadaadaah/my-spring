package hello.proxy.pureproxy.proxy.code;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CacheProxy implements Subject { // 프록시도 실제 객체와 같아야 하므로, Subject 인터페이스로 구현해야 함.

    private Subject target; // 실제 객체의 참조 : 클라이언트가 프록시를 호출하면, 프록시가 최종적으로 실제 객체를 호출해야 한다.
    private String cacheValue;

    public CacheProxy(Subject target) {
        this.target = target;
    }

    @Override
    public String operation() {
        log.info("프록시 호출");
        if(cacheValue == null) {
            cacheValue = target.operation();
        }
        return cacheValue;
    }
}

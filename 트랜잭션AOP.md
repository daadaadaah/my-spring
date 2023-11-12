# @Transactional

# 왜 내부 호출시 프록시 적용이 안되는 걸까?
## 원인 


## 해결 방법
- 주로, 내부 호출이 안되도록, 별도의 클래스로 분리하는 방법이 있다.
- 





# 왜 private 메서드에 적용이 안되는 걸까?
- 스프링의 트랜잭션 AOP 기능은 `public`메서드에만 트랜잭션을 적용하도록 기본 설정이 되어있다.
- 그래서, `private` 뿐만 아니라 `protected`,  `default`에는 트랜잭션이 적용되지 않는다.
- `protected`,  `default`은 외부에서 호출이 가능하지만, 스프링이 막아 둔 것이다.

## 스프링에서 `public`에만 트랜잭션을 적용하는 이유
- 클래스 레벨에 트랜잭션을 적용하면, 모든 메서드에 트랜잭션이 걸릴 수 있다.
- 그러면, 의도하지 않은 곳까지 트랜잭션이 과도하게 적용된다.
- 트랜잭션은 주로 비즈니스 로직의 시작점에 걸기 때문에, 대부분 외부에 열어준 곳을 시작점으로 사용한다.
- 이런 이유로 `public` 메서드에만 트랜잭션을 적용하도록 설정되어 있다.
- 참고로, `public`이 아닌 곳에 `@Transactional`이 붙어 있으면, 예외가 발생하지는 않고, 트랜잭션 적용만 무시된다.




- https://giron.tistory.com/140


## @Transactional 주의사항
```java
public class AService {

  public void publicExternal() {
      log.info("call external");

      publicTXInternal();

      privateTXInternal();
  }

  @Transactional
  public void publicTXInternal() {
      log.info("call public tx internal");
  }

  @Transactional
  private void privateTXInternal() {
      log.info("call private tx internal");
  }
}
```


```java
public class AServiceTest {

  @Autowired
  AService aService;

  @Test
  public void internalCall() {
      aService.publicTXInternal(); // internal 트랜잭션 적용 됨
  }

  @Test
  public void externalCall() {
      aService.publicExternal(); // internal 트랜잭션 적용 안됨
  }
}
```
- 내부 메서드를 따로 클래스로 분리한 후 해당 클래스를 주입받아, 내부 호출이 아닌 외부 호출로 호출되도록 하면 됨.

# 왜 스프링 초기화 시점에는 트랜잭션 AOP가 적용되지 않나요?
## 원인
- @PostConstruct는 해당 빈 자체만 생성되었다고 가정하고 호출됩니다.
- 해당 빈에 관련된 AOP 등을 포함한, 전체 스프링 애플리케이션 컨텍스트가 초기화 된 것을 의미하지는 않습니다.

- 초기화 코드가 먼저 호출되고, 그 다음에 트랜잭션 AOP가 적용되기 때문이다.
- 즉, @PostConstruct가 먼저 실행되고 이후에 @Transactional AOP가 적용되기 때문입니다.

- 따라서, 초기화 시점에는 해당 메서드에서 트랜잭션을 회득할 수 없다.

## 대안
- `ApplicationReadyEvent` 이벤트를 사용하는 것이다.
- 이 이벤트는 트랜잭션 AOP를 포함한 스프링이 컨테이너가 완전히 생성되고 난 다음에 이벤트가 붙은 메서드를 호출해주기 때문에












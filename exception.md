# 예외
## 예외란 무엇인가?
- 예외는 반드 처리되어야 한다.
- 모든 예외는 적절하게 복되든지, 아니면 작업을 중단시키고 운영자 또는 개발자에게 분명하게 통보되어야 한다.


## 예외의 종류와 특징
- 자바에서 throw를 통해 발생시킬 수 있는 예외는 크게 3가지가 있다.
1. Error
2. Exception - Unchecked Exception
3. Exception - checked Exception


### 1. Error
- 에러는 시스템에서 뭔가 비정상적인 상황이 발생했을 경우에 사용된다.
- 주로 VM에서 발생하는 것으로, 예를 들어 다음과 같은 것들이 있다.
1) OutOfMemoryError
2) ThreadDeath
- 이런 에러는 catch 블록으로 잡아봤자 아무런 대응 방법이 없기 때문에, 애플리케이션 코드에서 잡으려고 하면 안된다.
- 따라서, 시스템 레벨에서 특별한 작업을 하는게 아니라면, 애플리케이션에서는 이런 에러에 대한 처리는 신경쓰지 않아도 된다.

### 2. Exception - Unchecked Exception (= RuntimeException)
- Exception의 서브 클래스이면서 RuntimeException 클래스를 상속한 클래스
- 주로 프로그래머의 실수에 의해서 발생할 수 있는 예외들로, Java의 프로그래밍 요소들과 관계가 깊다. 예를 들면, 다음과 같은 것들이 있다.
1) NullPointerException
2) IllegalArgumentException
3) ArrayIndexOutOfBoundsException
4) ClassCastException
5) ArithmeticException
- 이러한 것들은 코드에서 미리 조건을 체크하도록 주의깊게 만들면 피할 수 있다.
- 그런데, 개발자도 사람이다보니, 실수를 할 수 있으므로, 이런 개발자의 부주의로 인해 발생할 수 있는 예외를 다루기 위해 만든 것이 RuntimeException이다.
- 다만, 이 RuntimeException은 명시적인 예외처리를 강제하지 않는다.
- 그래서, Unchecked Exception이다.

### 3. Exception - checked Exception
- Exception의 서브 클래스이면서 RuntimeException 클래스를 상속하지 않는 클래스
- 주로 외부 영향으로 발생할 수 있는 것들로서, 프로그램의 사용자들의 동작에 의해서 발생하는 예외들이다.
- 예를 들면, 다음과 같은 것들이 있다.
1) FileNotFoundException
2) DataFormatException
3) ClassNotFoundException
4) IOException
5) SQLException
- 체크 예외가 발생할 수 있는 메서드를 사용할 경우, 반드시 예외 처리를 처리하는 코드를 함께 작성해야 한다.
- 그렇지 않으면, 컴파일 에러가 난다.
- 
## 예외 처리 방법 
### 1. 예외 복구
- 예외 상황을 파악하고 문제를 해결해서, 정상 상태로 돌려놓는 방법



### 2. 예외 회피
- 예외처리들 자신이 담당하지 않고 자신을 호출한 쪽으로 던져버리는 방법
회피 방법 1) throws 문으로 선언해서 예외가 발생하면 알아서 던져지게 하는 방법
회피 방법 2) catch문으로 일단 예외를 잡은 후에 로그를 남기고 다시 예외를 던지는(rethrow) 방법
cf. 예외 처리 회피라는 것이 반드시 다른 오브젝트나 메서드에게 예외를 대신 처리할 수 있도록 던져야 한다. 그냥 try/catch 블럭에 아무 것도 처리하지 않은 것을 말하는게 아니다.
- 다른 오브젝트에게 예외처리 책임을 분명히 지게 하거나, 자신을 사용하는 쪽에서 예외를 다루는 게 최선의 방법이라는 분명한 확신이 있어야 한다.

### 3. 예외 전환
- 발생한 예외 -> 적절한 예외로 전환해서 메서드 밖으로 던지는 방법
예외 전환 목적 1) 로우레벨의 예외를 좀더 의미있고 추상화된 예외로 바꿔서 던져주기 위해
예외 전환 목적 2) 예외 처리를 강제하는 체크 예외 -> 언체크 예외인 RuntimeException으로 포장해서 불필요한 catch/throws를 피하기 위해

cf. 예외 회피의 경우는 발생한 예외 그대로 던진다.


## 스프링에서 예외처리하는 방법


- 스프링에서 다음과 같은 순서로 HandlerExceptionResolverComposition에 기본으로 등록되어 있다.
  1. ExceptionHandlerExceptionResolver
  2. ResponseStatusExceptionResolver
  3. DefaultHandlerExceptionResolver
- 
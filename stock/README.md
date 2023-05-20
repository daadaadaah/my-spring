# 재고시스템으로 알아보는 동시성 이슈

## 작업환경 세팅
```shell
# docker 설치
brew install docker 
brew link docker
docker version

# mysql 설치 및 실행
docker pull mysql
docker run -d -p 3306:3306 -e MYSQL_ROOT_PASSWORD=1234 --name mysql mysql 
docker ps

# mysql 데이터베이스 생성
docker exec -it mysql bash
mysql -u root -p # 비밀번호 : test1234
create database stock_example;
use stock_example;
```

## 문제점 1. 멀티스레드 환경에서 데이터 정합성 불일치 문제 - Application 서버단에서 race condition 해결하는 방법
### 해결방법1. synchronized 활용하기
- 보통 다중 서버 환경이므로, race condition이 또 발생한다.

## 문제점 2. 멀티서버 환경에서 데이터 정합성 불일치 문제 - DB 단에서 MySQL로 race condition 해결하는 방법
### 해결방법 1. Pessimistic Lock (exclusive lock)
<img width="1330" alt="스크린샷 2023-05-20 오후 7 18 49" src="https://github.com/f-lab-edu/hee-commerce/assets/60481383/dd4b19c5-cf44-401b-af1b-452395a2e87e">

- 실제로 데이터에 Lock을 걸어서 정합성을 맞추는 방법이다.
- exclusive lock을 걸게되면 다른 트랜잭션에서는 lock이 해제되기 전에 데이터를 가져갈 수 없게 된다.
- 단, dead lock이 걸리 수 있기 때문에 주의하여 사용해야 한다.
- 장점 : 충돌이 빈번히 일어난다면, Optimistic Lock보다 성능이 좋을 수 있다.
- 단점 : 별도의 Lock을 잡기 때문에, Optimistic Lock보다 성능 감소가 있을 수 있다.

### 해결방법 2. Optimistic Lock
- lock 을 걸지않고 버전을 이용함으로써 정합성을 맞추는 방법이다.
- 먼저 데이터를 읽으 후에 update를 수행할 때 현재 내가 읽은 버전이 맞는지 확인하여 업데이트 한다.
- 만약, 내가 읽은 버전에서 수정사항이 생겼다면, application에서 다시 읽은 후에 작업을 수행해야 한다.
- 장점 : 별도의 Lock을 잡기 않기 때문에, Pessimistic Lock보다 성능상 이점이 있을 수 있다.
- 단점 1 : 업데이트가 실패했을 때, 재시도 로직을 개발자가 직접 작성을 해줘야 한다.
- 단점 2 : 충돌이 빈번히 일어난다면, Optimistic Lock보다 성능이 안 좋을 수 있다.

### 해결방법 3. Named Lock
- 이름을 가진 metadata locking 이다.
- 이름을 가진 lock을 획득한 후 해제할 떄까지 다른 세션은 이 lock을 획득할 수 없도록 한다.
- 주의할 점은 transaction이 종료될 때 lock이 자동으로 해제되지 않으므로, 별도의 명령어로 해제를 수행해주거나 선점시간이 끝나야 해제된다.
- 단점 : transaction 종료 후 lock 해제와 세션 관리를 해줘야 하는 번거로움이 있다. 실 서비스에서 그 구현 방법이 복잡할 수 있다.

#### Named Lock vs Pessimistic Lock 
1. 어디에 Lock을 거느냐의 차이
- Pessimistic Lock : Row, Table 단위로 Lock을 거는 방법
- Name Lock : Row, Table 단위가 아니라 MetaData에 Lock을 거는 방법

2. 타임아웃 구현 난이도
- Pessimistic Lock : 타임아웃을 구현하기 굉장히 힘듬
- Named Lock : 손쉽게 구현할 수 있음

### Reference
- https://dev.mysql.com/doc/refman/8.0/en/
- https://dev.mysql.com/doc/refman/8.0/en/locking-functions.html
- https://dev.mysql.com/doc/refman/8.0/en/metadata-locking.html


## 문제점 2. 멀티서버 환경에서 데이터 정합성 불일치 문제 - DB 단에서 Redis로 race condition 해결하는 방법
### Lettuce
- setnx 명령어를 활용하여 분산락 구현
- `setnx` : set if not existed의 줄임말로, key와 value를 set할 때, 기존의 값이 없을 때에만 set하는 명령어이다.
- setnx 명령어를 활용하는 방식은 spin lock 방식이므로, 개발자가 직접 재시도 로직을 작성해줘야 한다.
- spin lock 이란, lock을 획득하려는 스레드가 lock을 사용할 수 있는지, 반복적으로 확인하면서 lock 획득을 시도하는 방식이다.
- MySQL의 Named Lock과 비슷하다.
- 차이점은 세션 관리에 신경을 안써도 된다는 점이다.
- 장점 1 : 구현이 간단하다
- 장점 2 : spring data redis 를 이용하면 lettuce 가 기본이기 때문에 별도의 라이브러리를 사용하지 않아도 된다.
- 단점 : spin lock 방식이므로, 동시에 많은 스레드가 lock 획득 대기 상태라면 Redis에 부하를 줄 수 있다.

### Redisson
- pub/sub 기반으로 Lock이 구현되어 있다.
- pub/sub 기반은 channel을 하나를 만들고, lock을 점유 중인 스레드가 lock을 획득하려고 대기 중인 스레드에게 해제를 알려주면 안내를 받은 스레드가 lock 획득 시도하는 방식이다.

- 장점 : 락 획득 재시도를 기본으로 제공해주므로, 대부분의 경우 개발자가 직접 재시도 로직을 작성하지 않아도 된다.
- 단점 1 : lettuce에 비해 구현이 다소 복잡하다.
- 단점 2 : 별도의 라이브러리를 사용해야한다.
- 단점 3 : lock 을 라이브러리 차원에서 제공해주기 떄문에 사용법을 공부해야 한다.

#### Lettuce vs Redisson
- 차이점은 계속 lock 획득을 시도하느냐이다.
- Lettuce는 계속 lock 획득을 시도하는 반면, 
- Redisson는 lock 해제가 되었을 때, pub-sub 방식으로 구현이 되어있기 때문에 한번 또는 몇번만 시도를 한다.
- 따라서, Redisson가 lettuce 와 비교했을 때 Redis에 부하가 덜 간다.

### 실무에서는 ?
- 재시도가 필요하지 않은 lock 은 lettuce 활용
- 재시도가 필요한 경우에는 redisson 를 활용

## MySQL vs Redis
### Mysql
- 이미 Mysql 을 사용하고 있다면 별도의 비용없이 사용가능하다.
- 어느정도의 트래픽까지는 문제없이 활용이 가능하다.
- Redis 보다는 성능이 좋지않다.

### Redis
- 활용중인 Redis 가 없다면 별도의 구축비용과 인프라 관리비용이 발생한다.
- Mysql 보다 성능이 좋다.


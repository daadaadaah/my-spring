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
mysql -u root -p # 비밀번호 : 1234
create database stock_example;
use stock_example;
```
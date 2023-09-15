package hello.itemservice.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Data;

@Data
@Entity // JPA가 사용하는 객체라는 의미, 이 어노테이션이 있어야 JPA가 인식할 수 있다. 이렇게 @Entity가 붙은 객체를 JPA에서는 엔티티라 한다.
public class Item {

    @Id // 테이블의 PK와 해당 필드를 매핑한다.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // PK 생성 값을 데이터베이스에서 생성하는 `IDENTITY`방식을 사용한다. 예 : MySQL auto increment
    private Long id;

    @Column(name = "item_name", length = 10) // 객체의 필드를 테이블의 컬럼과 매핑한다.
    // length = 10 : JPA의 매핑 정보로 DDL도 생성할 수 있는데, 그때 컬럼의 길이 값으로 활용된다.
    // 스프링 부트와 통합해서 사용하면, 필드이름을 테이블 컬럼명으로 변경할 때, 객체 필드의 카멜 케이스를 테이블 컬럼의 언더스코어로 자동으로 변환해준다.
    // 따라서, @Column 생략해도 되지만, 학습단계므로 추가해 줌
    private String itemName;
    private Integer price;
    private Integer quantity;

    // JPA는 public 또는 protected의 기본 생성자가 필수이다.
    // 참고로, 이렇게 public 또는 protected의 생성자가 있으면, 프록시 기술을 사용하기 편하다.
    public Item() {
    }

    public Item(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}

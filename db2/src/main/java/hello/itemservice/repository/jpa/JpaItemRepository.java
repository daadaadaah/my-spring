package hello.itemservice.repository.jpa;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Repository
@Transactional // JPA의 모든 변경은 트랜잭션 안에서 이루어진다. 그래서 클래스 또는 메서드에 추가해줘야 한다.
// 일반적으로 비즈니스 로직을 시작하는 서비스 계층에 트랜잭션을 걸어준다.
// 다만, 지금 예제처럼 복잡한 비즈니스 로직이 없어서 서비스 계층 대신 Repository에 트랜잭션을 걸 수도 있다.
public class JpaItemRepository implements ItemRepository {

    // JPA의 모든 동작은 엔티티 매니저를 통해 이루어진다.
    // 엔티티 매니저는 내부에 데이터소스를 가지고 있고, 데이터베이스에 접근할 수 있다.
    private final EntityManager em;

    public JpaItemRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public Item save(Item item) {
        em.persist(item);
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        Item finditem = em.find(Item.class, itemId);
        finditem.setItemName(updateParam.getItemName());
        finditem.setPrice(updateParam.getPrice());
        finditem.setQuantity(updateParam.getQuantity());
    }

    @Override
    public Optional<Item> findById(Long id) {
        Item item = em.find(Item.class, id);

        return Optional.ofNullable(item);
    }

//    @Override
//    public List<Item> findAll(ItemSearchCond cond) {
//        String jpql = "select i from Item i";
//
//        List<Item> result = em.createQuery(jpql, Item.class)
//            .getResultList();
//
//        return result;
//    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        String jpql = "select i from Item i";

        Integer maxPrice = cond.getMaxPrice();
        String itemName = cond.getItemName();

        if (StringUtils.hasText(itemName) || maxPrice != null) {
            jpql += " where";
        }

        boolean andFlag = false;
        List<Object> param = new ArrayList<>();
        if (StringUtils.hasText(itemName)) {
            jpql += " i.itemName like concat('%',:itemName,'%')";
            param.add(itemName);
            andFlag = true;
        }

        if (maxPrice != null) {
            if (andFlag) {
                jpql += " and";
            }
            jpql += " i.price <= :maxPrice";
            param.add(maxPrice);
        }

        log.info("jpql={}", jpql);

        TypedQuery<Item> query = em.createQuery(jpql, Item.class);
        if (StringUtils.hasText(itemName)) {
            query.setParameter("itemName", itemName);
        }
        if (maxPrice != null) {
            query.setParameter("maxPrice", maxPrice);
        }
        return query.getResultList();
    }
}

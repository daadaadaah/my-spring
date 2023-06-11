package com.example.stock.facade;

import com.example.stock.service.OptimisticLockStockService;
import org.springframework.stereotype.Service;

/**
 * Optimistic Lock의 경우, 실패했을 경우에 재시도할 수 있도록 해야 함.
 * 단일 책임 원칙에 의해 재시도 책임을 맡도록 하는 OptimisticLockStockFacade 가 담당하도록 함.
 * 만약, Facade Class 를 만들지 않는다면, OptimisticLockStockService은 2가지 책임이 있다.
 * 1. 재고 감소 책임
 * 2. 재시도 책임
 * 이렇게 될 경우, OptimisticLockStockService이 많게 되는 문제점이 있다.
 */
@Service
public class OptimisticLockStockFacade {

    private OptimisticLockStockService optimisticLockStockService;

    public OptimisticLockStockFacade(OptimisticLockStockService optimisticLockStockService) {
        this.optimisticLockStockService = optimisticLockStockService;
    }

    public void decrease(Long id, Long quantity) throws InterruptedException {
        while (true) { // 실패했을 때 재시도를 해야 하므로, while 사용
            try {

                optimisticLockStockService.decrease(id, quantity);

                break;
            } catch (Exception e) {
                Thread.sleep(50); // 실패했을 때, 50ms 이후에 재시도하도록 함.
            }
        }
    }
}
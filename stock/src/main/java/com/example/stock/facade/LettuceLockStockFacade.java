package com.example.stock.facade;

import com.example.stock.repository.LockRepository;
import com.example.stock.repository.RedisLockRepository;
import com.example.stock.service.StockService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


/**
 * 실제 로직 수행 전/후 로 lock 획득과 lock 해제를 수행해야 하기 때문에, 필요한 Facade 클래스
 */
@Component
public class LettuceLockStockFacade {

    private RedisLockRepository redisLockRepository;

    private StockService stockService;

    public LettuceLockStockFacade(RedisLockRepository redisLockRepository, StockService stockService) {
        this.redisLockRepository = redisLockRepository;
        this.stockService = stockService;
    }

    public void decrease(Long key, Long quantity) throws InterruptedException {
        while (!redisLockRepository.lock(key)) {
            Thread.sleep(100); // Redis에 부하를 줄 수 있으므로, lock 획득 재시도 간에 일정 기간 텀을 준 것
        }

        try {
            stockService.decrease(key, quantity);
        } finally {
            redisLockRepository.unlock(key);
        }
    }
}

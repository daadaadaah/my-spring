package com.example.stock.service;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PessimisticLockStockService {

    private StockRepository stockRepository;

    public PessimisticLockStockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Transactional
    public Long decrease(Long id, Long quantity) {
        // 1. Lock을 걸고 데이터 가져오기
        Stock stock = stockRepository.findByIdWithPessimisticLock(id);

        // 2. 재고 감소시키기
        stock.decrease(quantity);

        // 3. 감소된 재고 데이터 저장하기
        stockRepository.saveAndFlush(stock);

        return stock.getQuantity();
    }
}
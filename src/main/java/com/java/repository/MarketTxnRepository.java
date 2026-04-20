package com.java.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.java.entity.MarketTxn;

public interface MarketTxnRepository extends JpaRepository<MarketTxn, Long> {

	void deleteByMemberId(Long memberId);

	List<MarketTxn> findTop50ByOrderByCreatedAtDesc();

	List<MarketTxn> findTop50ByMemberIdOrderByCreatedAtDesc(Long memberId);

	List<MarketTxn> findByCreatedAtGreaterThanEqualOrderByCreatedAtAsc(LocalDateTime fromInclusive);

	List<MarketTxn> findByCreatedAtGreaterThanEqualOrderByCreatedAtDesc(LocalDateTime fromInclusive, Pageable pageable);

	long countByCreatedAtGreaterThanEqual(LocalDateTime fromInclusive);

	@Query("SELECT COALESCE(SUM(m.coinDelta), 0) FROM MarketTxn m WHERE m.txnType = :t AND m.createdAt >= :from")
	long sumCoinDeltaSince(@Param("t") String txnType, @Param("from") LocalDateTime from);

	@Query(value = "SELECT ITEM_NAME, COUNT(*) AS CNT FROM MARKET_TXN WHERE TXN_TYPE = 'BUY' GROUP BY ITEM_NAME ORDER BY CNT DESC", nativeQuery = true)
	List<Object[]> countPurchasesByItem(Pageable pageable);
}


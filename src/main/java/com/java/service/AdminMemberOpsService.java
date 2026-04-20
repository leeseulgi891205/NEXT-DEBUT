package com.java.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.java.dto.MemberOpsDetailDto;
import com.java.dto.MemberOpsSummaryDto;
import com.java.entity.MarketTxn;
import com.java.entity.Member;
import com.java.entity.MyTrainee;
import com.java.game.entity.Trainee;
import com.java.game.repository.TraineeRepository;
import com.java.photocard.entity.PhotoCardGrade;
import com.java.photocard.entity.PhotoCardMaster;
import com.java.photocard.entity.UserPhotoCard;
import com.java.photocard.repository.PhotoCardMasterRepository;
import com.java.photocard.repository.UserPhotoCardRepository;
import com.java.repository.MarketTxnRepository;
import com.java.repository.MemberRepository;
import com.java.repository.MyTraineeRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class AdminMemberOpsService {

	@PersistenceContext
	private EntityManager em;

	private final MemberRepository memberRepository;
	private final MyTraineeRepository myTraineeRepository;
	private final UserPhotoCardRepository userPhotoCardRepository;
	private final PhotoCardMasterRepository photoCardMasterRepository;
	private final TraineeRepository traineeRepository;
	private final MarketTxnRepository marketTxnRepository;
	private final MarketService marketService;

	public AdminMemberOpsService(MemberRepository memberRepository, MyTraineeRepository myTraineeRepository,
			UserPhotoCardRepository userPhotoCardRepository, PhotoCardMasterRepository photoCardMasterRepository,
			TraineeRepository traineeRepository, MarketTxnRepository marketTxnRepository, MarketService marketService) {
		this.memberRepository = memberRepository;
		this.myTraineeRepository = myTraineeRepository;
		this.userPhotoCardRepository = userPhotoCardRepository;
		this.photoCardMasterRepository = photoCardMasterRepository;
		this.traineeRepository = traineeRepository;
		this.marketTxnRepository = marketTxnRepository;
		this.marketService = marketService;
	}

	@Transactional(readOnly = true)
	public Map<Long, MemberOpsSummaryDto> getMemberSummaries(List<Long> memberIds) {
		Map<Long, MemberOpsSummaryDto> out = new LinkedHashMap<>();
		if (memberIds == null || memberIds.isEmpty()) {
			return out;
		}
		Map<Long, Long> coinMap = toLongMap(
				em.createNativeQuery("SELECT MNO, COALESCE(COIN, 0) FROM MEMBER WHERE MNO IN :ids")
						.setParameter("ids", memberIds)
						.getResultList());
		Map<Long, Long> traineeMap = toLongMap(
				em.createNativeQuery("SELECT MEMBER_ID, COUNT(*) FROM MY_TRAINEE WHERE MEMBER_ID IN :ids GROUP BY MEMBER_ID")
						.setParameter("ids", memberIds)
						.getResultList());
		Map<Long, Long> photoMap = toLongMap(
				em.createNativeQuery(
						"SELECT MEMBER_ID, COUNT(*) FROM USER_PHOTO_CARD WHERE MEMBER_ID IN :ids GROUP BY MEMBER_ID")
						.setParameter("ids", memberIds)
						.getResultList());

		for (Long memberId : memberIds) {
			long coin = coinMap.getOrDefault(memberId, 0L);
			long traineeCount = traineeMap.getOrDefault(memberId, 0L);
			long photoCardCount = photoMap.getOrDefault(memberId, 0L);
			out.put(memberId, new MemberOpsSummaryDto(coin, traineeCount, photoCardCount));
		}
		return out;
	}

	@Transactional(readOnly = true)
	public MemberOpsDetailDto getMemberOpsDetail(Long memberId) {
		long coin = marketService.getCurrentCoin(memberId);
		List<MyTrainee> ownTrainees = myTraineeRepository.findByMemberIdOrderByIdDesc(memberId);
		List<Trainee> allTrainees = traineeRepository.findAll();
		Map<Long, String> traineeNameById = allTrainees.stream()
				.collect(Collectors.toMap(Trainee::getId, t -> t.getName() == null ? "-" : t.getName(), (a, b) -> a));
		List<String> traineeNames = ownTrainees.stream()
				.map(MyTrainee::getTraineeId)
				.map(id -> traineeNameById.getOrDefault(id, "#" + id))
				.filter(Objects::nonNull)
				.distinct()
				.collect(Collectors.toList());

		List<UserPhotoCard> ownCards = userPhotoCardRepository.findByMemberId(memberId);
		List<String> photoCards = ownCards.stream()
				.map(card -> {
					PhotoCardMaster master = card.getPhotoCardMaster();
					if (master == null || master.getTrainee() == null) {
						return "-";
					}
					return master.getTrainee().getName() + " [" + master.getGrade().name() + "]";
				})
				.collect(Collectors.toList());

		return new MemberOpsDetailDto(coin, traineeNames.size(), photoCards.size(), traineeNames, photoCards);
	}

	@Transactional
	public long adjustCoin(Long memberId, String type, long amount, String reason) {
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
		if (amount <= 0) {
			throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
		}

		String normalizedType = type == null ? "" : type.trim().toUpperCase(Locale.ROOT);
		long currentCoin = marketService.getCurrentCoin(member.getMno());
		long signedDelta;
		if ("ADD".equals(normalizedType)) {
			signedDelta = amount;
		} else if ("SUBTRACT".equals(normalizedType)) {
			if (currentCoin < amount) {
				throw new IllegalArgumentException("차감 후 코인이 0 미만이 될 수 없습니다.");
			}
			signedDelta = -amount;
		} else {
			throw new IllegalArgumentException("type은 ADD 또는 SUBTRACT만 가능합니다.");
		}
		if (signedDelta > Integer.MAX_VALUE || signedDelta < Integer.MIN_VALUE) {
			throw new IllegalArgumentException("변경 수량이 너무 큽니다.");
		}

		marketService.addCoin(memberId, (int) signedDelta);
		String note = (reason == null || reason.isBlank()) ? "관리자 코인 조정" : reason.trim();
		marketTxnRepository.save(new MarketTxn(memberId, "ADMIN_ADJUST", null, (int) signedDelta, note));
		return marketService.getCurrentCoin(memberId);
	}

	@Transactional
	public void addTraineeToMember(Long memberId, Long traineeId) {
		memberRepository.findById(memberId)
				.orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
		traineeRepository.findById(traineeId)
				.orElseThrow(() -> new IllegalArgumentException("연습생을 찾을 수 없습니다."));
		if (myTraineeRepository.findByMemberIdAndTraineeId(memberId, traineeId).isPresent()) {
			throw new IllegalArgumentException("이미 보유한 연습생입니다.");
		}
		myTraineeRepository.save(new MyTrainee(memberId, traineeId, 1));
	}

	@Transactional
	public Map<String, Integer> addTraineesToMember(Long memberId, List<Long> traineeIds) {
		if (traineeIds == null || traineeIds.isEmpty()) {
			throw new IllegalArgumentException("추가할 연습생을 선택해주세요.");
		}
		Set<Long> uniqIds = new LinkedHashSet<>(traineeIds);
		int added = 0;
		int skipped = 0;
		for (Long traineeId : uniqIds) {
			try {
				addTraineeToMember(memberId, traineeId);
				added++;
			} catch (IllegalArgumentException e) {
				// 이미 보유/존재하지 않는 연습생은 건너뛰고 계속 진행
				skipped++;
			}
		}
		Map<String, Integer> result = new LinkedHashMap<>();
		result.put("addedCount", added);
		result.put("skippedCount", skipped);
		return result;
	}

	@Transactional
	public void grantPhotoCard(Long memberId, Long traineeId, String grade) {
		memberRepository.findById(memberId)
				.orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
		PhotoCardGrade parsedGrade;
		try {
			parsedGrade = PhotoCardGrade.valueOf(grade == null ? "" : grade.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("포토카드 등급은 R/SR/SSR만 가능합니다.");
		}
		PhotoCardMaster master = photoCardMasterRepository.findByTrainee_IdAndGrade(traineeId, parsedGrade)
				.orElseThrow(() -> new IllegalArgumentException("해당 연습생/등급 포토카드 마스터가 없습니다."));
		if (userPhotoCardRepository.existsByMemberIdAndPhotoCardMaster_Id(memberId, master.getId())) {
			throw new IllegalArgumentException("이미 보유한 포토카드입니다.");
		}
		userPhotoCardRepository.save(new UserPhotoCard(memberId, master));
	}

	@Transactional
	public Map<String, Integer> grantPhotoCards(Long memberId, List<Long> traineeIds, String grade) {
		if (traineeIds == null || traineeIds.isEmpty()) {
			throw new IllegalArgumentException("지급할 연습생을 선택해주세요.");
		}
		Set<Long> uniqIds = new LinkedHashSet<>(traineeIds);
		int granted = 0;
		int skipped = 0;
		for (Long traineeId : uniqIds) {
			try {
				grantPhotoCard(memberId, traineeId, grade);
				granted++;
			} catch (IllegalArgumentException e) {
				// 이미 보유/마스터 없음은 건너뛰고 계속 진행
				skipped++;
			}
		}
		Map<String, Integer> result = new LinkedHashMap<>();
		result.put("grantedCount", granted);
		result.put("skippedCount", skipped);
		return result;
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> getRecentActivities(Long memberId) {
		List<Map<String, Object>> out = new ArrayList<>();
		List<MarketTxn> txns = marketTxnRepository.findTop50ByMemberIdOrderByCreatedAtDesc(memberId).stream()
				.limit(8)
				.toList();
		for (MarketTxn tx : txns) {
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("type", tx.getTxnType());
			row.put("note", tx.getNote() == null ? "" : tx.getNote());
			row.put("coinDelta", tx.getCoinDelta());
			row.put("createdAt", tx.getCreatedAt() == null ? "" : tx.getCreatedAt().toString());
			out.add(row);
		}
		return out;
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> getOwnedTraineeRows(Long memberId) {
		List<MyTrainee> ownTrainees = myTraineeRepository.findByMemberIdOrderByIdDesc(memberId);
		Map<Long, String> traineeNameById = traineeRepository.findAll().stream()
				.collect(Collectors.toMap(Trainee::getId, t -> t.getName() == null ? "-" : t.getName(), (a, b) -> a));
		List<Map<String, Object>> out = new ArrayList<>();
		for (MyTrainee row : ownTrainees) {
			Map<String, Object> item = new LinkedHashMap<>();
			item.put("traineeId", row.getTraineeId());
			item.put("name", traineeNameById.getOrDefault(row.getTraineeId(), "#" + row.getTraineeId()));
			item.put("quantity", Math.max(0, row.getQuantity()));
			item.put("enhanceLevel", Math.max(0, row.getEnhanceLevel()));
			out.add(item);
		}
		return out;
	}

	@Transactional
	public Map<String, Object> adjustMemberEnhance(Long memberId, Long traineeId, Integer level, Integer quantity,
			String actorLabel) {
		memberRepository.findById(memberId)
				.orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
		Trainee trainee = traineeRepository.findById(traineeId)
				.orElseThrow(() -> new IllegalArgumentException("연습생을 찾을 수 없습니다."));
		MyTrainee row = myTraineeRepository.findByMemberIdAndTraineeId(memberId, traineeId)
				.orElseThrow(() -> new IllegalArgumentException("해당 회원이 보유하지 않은 연습생입니다."));

		int prevLevel = row.getEnhanceLevel();
		int prevQuantity = row.getQuantity();
		int nextLevel = level == null ? row.getEnhanceLevel() : level.intValue();
		int nextQuantity = quantity == null ? row.getQuantity() : quantity.intValue();
		if (nextLevel < 0 || nextLevel > 5) {
			throw new IllegalArgumentException("강화 단계는 0~5 범위만 가능합니다.");
		}
		if (nextQuantity < 0) {
			throw new IllegalArgumentException("보유 카드 수량은 0 이상이어야 합니다.");
		}
		row.setEnhanceLevel(nextLevel);
		row.setQuantity(nextQuantity);
		myTraineeRepository.save(row);
		String who = (actorLabel == null || actorLabel.isBlank()) ? "관리자" : actorLabel.trim();
		String traineeName = trainee.getName() == null ? ("#" + traineeId) : trainee.getName().trim();
		String note = String.format("%s 강화 조정: %s (Lv %d→%d, Qty %d→%d)",
				who, traineeName, prevLevel, nextLevel, prevQuantity, nextQuantity);
		marketTxnRepository.save(new MarketTxn(memberId, "ADMIN_ENHANCE", traineeName, 0, note));

		Map<String, Object> out = new LinkedHashMap<>();
		out.put("traineeId", row.getTraineeId());
		out.put("enhanceLevel", row.getEnhanceLevel());
		out.put("quantity", row.getQuantity());
		return out;
	}

	private Map<Long, Long> toLongMap(List<?> rows) {
		Map<Long, Long> out = new LinkedHashMap<>();
		for (Object rowObj : rows) {
			if (!(rowObj instanceof Object[] row) || row.length < 2) {
				continue;
			}
			if (!(row[0] instanceof Number key) || !(row[1] instanceof Number value)) {
				continue;
			}
			out.put(key.longValue(), value.longValue());
		}
		return out;
	}
}

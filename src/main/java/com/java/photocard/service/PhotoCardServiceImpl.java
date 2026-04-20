package com.java.photocard.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.java.dto.AdminPhotoCardDto;
import com.java.dto.PhotoCardBatchResultDto;
import com.java.dto.PhotoCardDrawLineDto;
import com.java.dto.PhotoCardDrawResultDto;
import com.java.dto.TraineePhotoCardSummaryDto;
import com.java.entity.Member;
import com.java.game.entity.Trainee;
import com.java.game.repository.TraineeRepository;
import com.java.photocard.entity.EquippedPhotoCard;
import com.java.photocard.entity.PhotoCardGrade;
import com.java.photocard.entity.PhotoCardMaster;
import com.java.photocard.entity.UserPhotoCard;
import com.java.photocard.repository.EquippedPhotoCardRepository;
import com.java.photocard.repository.PhotoCardMasterRepository;
import com.java.photocard.repository.UserPhotoCardRepository;
import com.java.service.MarketService;
import com.java.service.TraineeGroupService;
import com.java.repository.MemberRepository;

@Service
public class PhotoCardServiceImpl implements PhotoCardService {

	private final PhotoCardMasterRepository photoCardMasterRepository;
	private final UserPhotoCardRepository userPhotoCardRepository;
	private final EquippedPhotoCardRepository equippedPhotoCardRepository;
	private final TraineeRepository traineeRepository;
	private final MarketService marketService;
	private final MemberRepository memberRepository;
	private final TraineeGroupService traineeGroupService;
	private final Random random = new Random();

	public PhotoCardServiceImpl(PhotoCardMasterRepository photoCardMasterRepository,
			UserPhotoCardRepository userPhotoCardRepository,
			EquippedPhotoCardRepository equippedPhotoCardRepository,
			TraineeRepository traineeRepository,
			MarketService marketService,
			MemberRepository memberRepository,
			TraineeGroupService traineeGroupService) {
		this.photoCardMasterRepository = photoCardMasterRepository;
		this.userPhotoCardRepository = userPhotoCardRepository;
		this.equippedPhotoCardRepository = equippedPhotoCardRepository;
		this.traineeRepository = traineeRepository;
		this.marketService = marketService;
		this.memberRepository = memberRepository;
		this.traineeGroupService = traineeGroupService;
	}

	@Override
	@Transactional
	public void ensureMastersInitialized() {
		if (photoCardMasterRepository.count() > 0) {
			return;
		}
		List<Trainee> trainees = traineeRepository.findAll();
		for (Trainee t : trainees) {
			for (PhotoCardGrade g : PhotoCardGrade.values()) {
				String display = t.getName() + " · " + g.name();
				photoCardMasterRepository.save(new PhotoCardMaster(t, g, display));
			}
		}
	}

	@Override
	@Transactional
	public PhotoCardDrawResultDto pull(Long memberId) {
		if (memberId == null) {
			return new PhotoCardDrawResultDto("login", "로그인이 필요합니다.", null, null, null, null, null, null);
		}
		ensureMastersInitialized();
		marketService.ensureMinimumCoin(memberId, MarketService.DEFAULT_MIN_COIN);

		int coin = marketService.getCurrentCoin(memberId);
		if (coin < PULL_COST_COIN) {
			return new PhotoCardDrawResultDto("lack", "코인이 부족합니다.", null, null, null, coin, null, null);
		}

		PhotoCardDrawLineDto line = rollOnePhotocardLine(memberId, false);
		if ("duplicate".equals(line.result())) {
			return new PhotoCardDrawResultDto("duplicate", line.message(), line.grade(), line.displayName(),
					line.traineeId(), coin, line.imagePath(), line.traineeName());
		}
		if ("error".equals(line.result())) {
			return new PhotoCardDrawResultDto("error", line.message(), null, null, null, coin, null, null);
		}
		return new PhotoCardDrawResultDto("success", line.message(), line.grade(), line.displayName(),
				line.traineeId(), marketService.getCurrentCoin(memberId), line.imagePath(), line.traineeName());
	}

	@Override
	@Transactional
	public PhotoCardBatchResultDto pullBatch(Long memberId, int pulls) {
		if (memberId == null) {
			return new PhotoCardBatchResultDto("login", "로그인이 필요합니다.", List.of(), null);
		}
		if (pulls != 5 && pulls != 10) {
			return new PhotoCardBatchResultDto("error", "뽑기 횟수가 올바르지 않습니다.", List.of(), null);
		}
		ensureMastersInitialized();
		marketService.ensureMinimumCoin(memberId, MarketService.DEFAULT_MIN_COIN);

		int price = PhotoCardService.priceForPhotocardPulls(pulls);
		int coin = marketService.getCurrentCoin(memberId);
		if (coin < price) {
			return new PhotoCardBatchResultDto("lack", "코인이 부족합니다.", List.of(), coin);
		}

		marketService.addCoin(memberId, -price);
		marketService.logPurchase(memberId, "포토카드 뽑기 " + pulls + "회", price);

		List<PhotoCardDrawLineDto> lines = new ArrayList<>(pulls);
		for (int i = 0; i < pulls; i++) {
			lines.add(rollOnePhotocardLine(memberId, true));
		}
		return new PhotoCardBatchResultDto("success", null, lines, marketService.getCurrentCoin(memberId));
	}

	/**
	 * @param prepaidBundle true면 코인 차감 없이 1회 처리(묶음 선결제 후).
	 */
	private PhotoCardDrawLineDto rollOnePhotocardLine(Long memberId, boolean prepaidBundle) {
		int unlockMask = resolveUnlockMask(memberId);
		List<Trainee> trainees = traineeRepository.findAll().stream()
				.filter(t -> traineeGroupService.isUnlocked(unlockMask,
						traineeGroupService.resolveTraineeGroup(t.getName())))
				.toList();
		if (trainees.isEmpty()) {
			return new PhotoCardDrawLineDto("error", "해금된 연습생 데이터가 없습니다.", null, null, null, null, null);
		}

		PhotoCardGrade grade = PhotoCardGrade.values()[random.nextInt(PhotoCardGrade.values().length)];
		Trainee pick = trainees.get(random.nextInt(trainees.size()));
		Optional<PhotoCardMaster> masterOpt = photoCardMasterRepository.findByTrainee_IdAndGrade(pick.getId(), grade);
		if (masterOpt.isEmpty()) {
			return new PhotoCardDrawLineDto("error", "카드 마스터를 찾을 수 없습니다.", null, null, null, null, null);
		}
		PhotoCardMaster master = masterOpt.get();
		String img = PhotoCardService.resolvePhotoCardImagePath(pick.getImagePath(), grade.name());
		String tname = pick.getName();

		if (userPhotoCardRepository.existsByMemberIdAndPhotoCardMaster_Id(memberId, master.getId())) {
			return new PhotoCardDrawLineDto("duplicate", "이미 보유한 카드입니다.", grade.name(), master.getDisplayName(),
					pick.getId(), img, tname);
		}

		if (!prepaidBundle) {
			marketService.addCoin(memberId, -PULL_COST_COIN);
			marketService.logPurchase(memberId, "포토카드 뽑기", PULL_COST_COIN);
		}

		userPhotoCardRepository.save(new UserPhotoCard(memberId, master));
		equipHighestOwnedGrade(memberId, pick.getId());

		return new PhotoCardDrawLineDto("success", "획득", grade.name(), master.getDisplayName(), pick.getId(), img,
				tname);
	}

	private int resolveUnlockMask(Long memberId) {
		if (memberId == null) {
			return TraineeGroupService.DEFAULT_UNLOCK_MASK;
		}
		Member member = memberRepository.findById(memberId).orElse(null);
		if (member == null) {
			return TraineeGroupService.DEFAULT_UNLOCK_MASK;
		}
		int mask = member.getGroupUnlockMask();
		return mask > 0 ? mask : TraineeGroupService.DEFAULT_UNLOCK_MASK;
	}

	/** 보유 중 가장 높은 포토카드 등급(SSR &gt; SR &gt; R)을 장착 */
	private void equipHighestOwnedGrade(Long memberId, Long traineeId) {
		for (PhotoCardGrade g : new PhotoCardGrade[] { PhotoCardGrade.SSR, PhotoCardGrade.SR, PhotoCardGrade.R }) {
			if (owned(memberId, traineeId, g)) {
				equip(memberId, traineeId, g);
				return;
			}
		}
	}

	@Override
	@Transactional
	public String equip(Long memberId, Long traineeId, PhotoCardGrade grade) {
		if (memberId == null || traineeId == null || grade == null) {
			return "invalid";
		}
		ensureMastersInitialized();
		Optional<PhotoCardMaster> masterOpt = photoCardMasterRepository.findByTrainee_IdAndGrade(traineeId, grade);
		if (masterOpt.isEmpty()) {
			return "no_master";
		}
		PhotoCardMaster master = masterOpt.get();
		if (!userPhotoCardRepository.existsByMemberIdAndPhotoCardMaster_Id(memberId, master.getId())) {
			return "not_owned";
		}
		Trainee trainee = traineeRepository.findById(traineeId)
				.orElseThrow(() -> new IllegalArgumentException("trainee not found: " + traineeId));

		Optional<EquippedPhotoCard> existing = equippedPhotoCardRepository.findByMemberIdAndTrainee_Id(memberId,
				traineeId);
		if (existing.isPresent()) {
			EquippedPhotoCard e = existing.get();
			e.setPhotoCardMaster(master);
			equippedPhotoCardRepository.save(e);
		} else {
			equippedPhotoCardRepository.save(new EquippedPhotoCard(memberId, trainee, master));
		}
		return "ok";
	}

	@Override
	@Transactional(readOnly = true)
	public TraineePhotoCardSummaryDto getSummary(Long memberId, Long traineeId) {
		if (memberId == null || traineeId == null) {
			return new TraineePhotoCardSummaryDto(false, false, false, null, 0);
		}
		boolean r = owned(memberId, traineeId, PhotoCardGrade.R);
		boolean sr = owned(memberId, traineeId, PhotoCardGrade.SR);
		boolean ssr = owned(memberId, traineeId, PhotoCardGrade.SSR);

		Optional<EquippedPhotoCard> eq = equippedPhotoCardRepository.findByMemberIdAndTrainee_Id(memberId, traineeId);
		if (eq.isEmpty()) {
			return new TraineePhotoCardSummaryDto(r, sr, ssr, null, 0);
		}
		PhotoCardGrade g = eq.get().getPhotoCardMaster().getGrade();
		return new TraineePhotoCardSummaryDto(r, sr, ssr, g.name(), g.getBonusPercent());
	}

	private boolean owned(Long memberId, Long traineeId, PhotoCardGrade grade) {
		return photoCardMasterRepository.findByTrainee_IdAndGrade(traineeId, grade)
				.map(m -> userPhotoCardRepository.existsByMemberIdAndPhotoCardMaster_Id(memberId, m.getId()))
				.orElse(false);
	}

	@Override
	@Transactional(readOnly = true)
	public Map<Long, TraineePhotoCardSummaryDto> getSummariesForTrainees(Long memberId, Iterable<Long> traineeIds) {
		Map<Long, TraineePhotoCardSummaryDto> map = new HashMap<>();
		if (memberId == null || traineeIds == null) {
			return map;
		}
		for (Long tid : StreamSupport.stream(traineeIds.spliterator(), false).collect(Collectors.toSet())) {
			if (tid != null) {
				map.put(tid, getSummary(memberId, tid));
			}
		}
		return map;
	}

	@Override
	@Transactional(readOnly = true)
	public int getEquippedBonusPercent(Long memberId, Long traineeId) {
		if (memberId == null || traineeId == null) {
			return 0;
		}
		return equippedPhotoCardRepository.findByMemberIdAndTrainee_Id(memberId, traineeId)
				.map(e -> e.getPhotoCardMaster().getGrade().getBonusPercent())
				.orElse(0);
	}

	@Override
	@Transactional(readOnly = true)
	public String getEquippedGradeCode(Long memberId, Long traineeId) {
		if (memberId == null || traineeId == null) {
			return null;
		}
		return equippedPhotoCardRepository.findByMemberIdAndTrainee_Id(memberId, traineeId)
				.map(e -> e.getPhotoCardMaster().getGrade().name())
				.orElse(null);
	}

	@Override
	@Transactional
	public AdminPhotoCardDto saveOrUpdateCard(Long traineeId, String grade, MultipartFile image) {
		if (traineeId == null || !StringUtils.hasText(grade)) {
			throw new IllegalArgumentException("연습생/등급 정보가 유효하지 않습니다.");
		}
		PhotoCardGrade targetGrade;
		try {
			targetGrade = PhotoCardGrade.valueOf(grade.trim().toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("포토카드 등급이 올바르지 않습니다.");
		}

		Trainee trainee = traineeRepository.findById(traineeId)
				.orElseThrow(() -> new IllegalArgumentException("연습생을 찾을 수 없습니다."));
		PhotoCardMaster card = photoCardMasterRepository.findByTrainee_IdAndGrade(traineeId, targetGrade)
				.orElseGet(() -> photoCardMasterRepository.save(new PhotoCardMaster(trainee, targetGrade,
						trainee.getName() + " · " + targetGrade.name())));

		if (image != null && !image.isEmpty()) {
			card.setImageUrl(storePhotoCardImage(image));
		}
		card.setDisplayName(trainee.getName() + " · " + targetGrade.name());
		PhotoCardMaster saved = photoCardMasterRepository.save(card);
		return new AdminPhotoCardDto(saved.getId(), traineeId, saved.getGrade().name(), saved.getImageUrl(),
				StringUtils.hasText(saved.getImageUrl()));
	}

	@Override
	@Transactional(readOnly = true)
	public List<AdminPhotoCardDto> getCardsByTrainee(Long traineeId) {
		if (traineeId == null) {
			return List.of();
		}
		return photoCardMasterRepository.findByTrainee_Id(traineeId).stream()
				.map(c -> new AdminPhotoCardDto(c.getId(), c.getTrainee().getId(), c.getGrade().name(), c.getImageUrl(),
						StringUtils.hasText(c.getImageUrl())))
				.collect(Collectors.toList());
	}

	private String storePhotoCardImage(MultipartFile file) {
		String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
		String safeExt = StringUtils.hasText(ext) ? "." + ext.toLowerCase() : ".jpg";
		String saved = UUID.randomUUID().toString().replace("-", "") + safeExt;
		try {
			Path dir = Paths.get(System.getProperty("user.dir"), "uploads", "photocards");
			Files.createDirectories(dir);
			Files.copy(file.getInputStream(), dir.resolve(saved));
			return "/uploads/photocards/" + saved;
		} catch (IOException e) {
			throw new IllegalStateException("포토카드 이미지를 저장하지 못했습니다.", e);
		}
	}
}

package com.java.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.java.game.entity.Gender;
import com.java.game.entity.Grade;
import com.java.game.entity.Trainee;
import com.java.game.repository.TraineeRepository;
import com.java.photocard.entity.PhotoCardGrade;
import com.java.photocard.entity.PhotoCardMaster;
import com.java.photocard.repository.PhotoCardMasterRepository;
import com.java.service.TraineeService;

@Component
@Order(120)
@ConditionalOnProperty(name = "app.seed.curated-trainee-sync", havingValue = "true")
public class CuratedTraineeSyncRunner implements CommandLineRunner {

	private static final Path SOURCE_ZIP = Paths.get("C:/Users/KOSMO/Desktop/라이즈.zip");

	private final TraineeRepository traineeRepository;
	private final PhotoCardMasterRepository photoCardMasterRepository;
	private final TraineeService traineeService;

	public CuratedTraineeSyncRunner(
			TraineeRepository traineeRepository,
			PhotoCardMasterRepository photoCardMasterRepository,
			TraineeService traineeService) {
		this.traineeRepository = traineeRepository;
		this.photoCardMasterRepository = photoCardMasterRepository;
		this.traineeService = traineeService;
	}

	@Override
	@Transactional
	public void run(String... args) throws Exception {
		List<TraineeSpec> specs = curatedSpecs();
		if (specs.isEmpty()) {
			return;
		}

		Map<String, Trainee> byName = new HashMap<>();
		for (Trainee t : traineeRepository.findAll()) {
			byName.put(normalizeKey(t.getName()), t);
		}

		Path uploadsDir = Paths.get(System.getProperty("user.dir"), "uploads", "trainees");
		Files.createDirectories(uploadsDir);

		ZipFile zip = openZipIfExists();
		try {
			int index = 0;
			for (TraineeSpec spec : specs) {
				final int currentIndex = index;
				Trainee trainee = byName.get(normalizeKey(spec.name()));
				if (trainee == null) {
					trainee = new Trainee(spec.name(), spec.gender(), spec.grade(), spec.vocal(), spec.dance(), spec.star(),
							spec.mental(), spec.teamwork(), null);
				}

				trainee.setName(spec.name());
				trainee.setGender(spec.gender());
				trainee.setGrade(spec.grade());
				trainee.setVocal(spec.vocal());
				trainee.setDance(spec.dance());
				trainee.setStar(spec.star());
				trainee.setMental(spec.mental());
				trainee.setTeamwork(spec.teamwork());
				trainee.setAge(spec.age());
				trainee.setBirthday(spec.birthday());
				trainee.setHobby(spec.hobby());
				trainee.setInstagram(spec.instagram());
				trainee.setMotto(null);
				trainee.setWeight(null);
				Trainee saved = traineeRepository.save(trainee);
				String baseImagePath = saved.getImagePath();

				if (zip != null) {
					Optional<? extends ZipEntry> base = findEntry(zip, spec.group(), spec.name(), "기본");
					base.ifPresent(entry -> {
						String savedPath = copyZipImage(zip, entry, uploadsDir, currentIndex, "base");
						if (savedPath != null) {
							saved.setImagePath(savedPath);
						}
					});
					baseImagePath = saved.getImagePath();

					upsertPhotoCardImage(zip, uploadsDir, saved, spec, currentIndex, PhotoCardGrade.R, "r", baseImagePath);
					upsertPhotoCardImage(zip, uploadsDir, saved, spec, currentIndex, PhotoCardGrade.SR, "sr", baseImagePath);
					upsertPhotoCardImage(zip, uploadsDir, saved, spec, currentIndex, PhotoCardGrade.SSR, "ssr", baseImagePath);
				}

				traineeRepository.save(saved);
				index++;
			}
		} finally {
			if (zip != null) {
				zip.close();
			}
		}

		Set<String> targetNames = new HashSet<>();
		for (TraineeSpec spec : specs) {
			targetNames.add(normalizeKey(spec.name()));
		}
		for (Trainee t : traineeRepository.findAll()) {
			if (!targetNames.contains(normalizeKey(t.getName()))) {
				traineeService.deleteTrainee(t.getId());
			}
		}
	}

	private void upsertPhotoCardImage(
			ZipFile zip,
			Path uploadsDir,
			Trainee trainee,
			TraineeSpec spec,
			int index,
			PhotoCardGrade grade,
			String suffix,
			String baseImagePath) {
		Optional<? extends ZipEntry> pcEntry = findEntry(zip, spec.group(), spec.name(), suffix);
		if (pcEntry.isEmpty()) {
			return;
		}
		String imagePath = copyZipImageForPhotocard(zip, pcEntry.get(), uploadsDir, index, suffix, baseImagePath,
				grade.name());
		if (imagePath == null) {
			return;
		}

		PhotoCardMaster card = photoCardMasterRepository.findByTrainee_IdAndGrade(trainee.getId(), grade)
				.orElseGet(() -> new PhotoCardMaster(trainee, grade, trainee.getName() + " · " + grade.name()));
		card.setDisplayName(trainee.getName() + " · " + grade.name());
		card.setImageUrl(imagePath);
		photoCardMasterRepository.save(card);
	}

	private String copyZipImageForPhotocard(
			ZipFile zip,
			ZipEntry entry,
			Path uploadsDir,
			int index,
			String suffix,
			String baseImagePath,
			String gradeCode) {
		String ext = extension(entry.getName());
		if (ext.isBlank()) {
			ext = ".png";
		}

		String fileName;
		if (baseImagePath != null && baseImagePath.startsWith("/uploads/trainees/")) {
			String baseFile = baseImagePath.substring("/uploads/trainees/".length());
			String baseWithoutExt = removeExtension(baseFile);
			fileName = baseWithoutExt + "_pc_" + gradeCode + ext;
		} else {
			fileName = String.format(Locale.ROOT, "curated_%02d_%s_pc_%s%s", index, suffix, gradeCode, ext);
		}

		Path target = uploadsDir.resolve(fileName);
		try (InputStream in = zip.getInputStream(entry)) {
			Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
			return "/uploads/trainees/" + fileName;
		} catch (IOException e) {
			return null;
		}
	}

	private Optional<? extends ZipEntry> findEntry(ZipFile zip, String group, String name, String suffix) {
		String groupKey = normalizeKey(group);
		String nameKey = normalizeKey(name);
		String suffixKey = normalizeKey(suffix);
		return zip.stream()
				.filter(e -> !e.isDirectory())
				.filter(e -> {
					String path = e.getName().replace('\\', '/');
					int slash = path.lastIndexOf('/');
					String dir = slash >= 0 ? path.substring(0, slash) : "";
					String file = slash >= 0 ? path.substring(slash + 1) : path;
					String base = removeExtension(file);
					String dirKey = normalizeKey(dir);
					String baseKey = normalizeKey(base);
					if (!dirKey.equals(groupKey)) {
						return false;
					}
					if (!baseKey.startsWith(nameKey)) {
						return false;
					}
					String tail = baseKey.substring(nameKey.length());
					return tail.equals(suffixKey);
				})
				.findFirst();
	}

	private ZipFile openZipIfExists() {
		if (!Files.exists(SOURCE_ZIP)) {
			return null;
		}
		try {
			return new ZipFile(SOURCE_ZIP.toFile(), Charset.forName("CP949"));
		} catch (IOException e) {
			try {
				return new ZipFile(SOURCE_ZIP.toFile(), Charset.forName("UTF-8"));
			} catch (IOException ignored) {
				return null;
			}
		}
	}

	private String copyZipImage(ZipFile zip, ZipEntry entry, Path uploadsDir, int index, String tag) {
		String ext = extension(entry.getName());
		if (ext.isBlank()) {
			ext = ".png";
		}
		String fileName = String.format(Locale.ROOT, "curated_%02d_%s%s", index, tag, ext);
		Path target = uploadsDir.resolve(fileName);
		try (InputStream in = zip.getInputStream(entry)) {
			Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
			return "/uploads/trainees/" + fileName;
		} catch (IOException e) {
			return null;
		}
	}

	private String extension(String name) {
		int dot = name.lastIndexOf('.');
		if (dot < 0 || dot == name.length() - 1) {
			return "";
		}
		return name.substring(dot).toLowerCase(Locale.ROOT);
	}

	private String removeExtension(String name) {
		int dot = name.lastIndexOf('.');
		return dot < 0 ? name : name.substring(0, dot);
	}

	private String normalizeKey(String value) {
		if (value == null) {
			return "";
		}
		return value.replace(" ", "").replace("_", "").replace("-", "").trim().toLowerCase(Locale.ROOT);
	}

	private List<TraineeSpec> curatedSpecs() {
		List<TraineeSpec> rows = new ArrayList<>();

		// 라이즈
		rows.add(new TraineeSpec("라이즈", "쇼타로", Gender.MALE, Grade.N, 11, 11, 11, 11, 11, 25, LocalDate.of(2000, 11, 25), "모름", "공식 개인 계정 확인 안 됨 / 팀 계정 @riize_official"));
		rows.add(new TraineeSpec("라이즈", "은석", Gender.MALE, Grade.N, 11, 11, 11, 11, 11, 25, LocalDate.of(2001, 3, 19), "모름", "공식 개인 계정 확인 안 됨 / 팀 계정 @riize_official"));
		rows.add(new TraineeSpec("라이즈", "성찬", Gender.MALE, Grade.N, 11, 11, 11, 11, 11, 24, LocalDate.of(2001, 9, 13), "모름", "공식 개인 계정 확인 안 됨 / 팀 계정 @riize_official"));
		rows.add(new TraineeSpec("라이즈", "원빈", Gender.MALE, Grade.R, 13, 13, 13, 13, 13, 24, LocalDate.of(2002, 3, 2), "모름", "공식 개인 계정 확인 안 됨 / 팀 계정 @riize_official"));
		rows.add(new TraineeSpec("라이즈", "소희", Gender.MALE, Grade.R, 13, 13, 13, 13, 13, 22, LocalDate.of(2003, 11, 21), "모름", "공식 개인 계정 확인 안 됨 / 팀 계정 @riize_official"));
		rows.add(new TraineeSpec("라이즈", "앤톤", Gender.MALE, Grade.R, 13, 13, 13, 13, 13, 22, LocalDate.of(2004, 3, 21), "모름", "공식 개인 계정 확인 안 됨 / 팀 계정 @riize_official"));

		// 엑소
		rows.add(new TraineeSpec("엑소", "수호", Gender.MALE, Grade.R, 13, 13, 13, 13, 13, 34, LocalDate.of(1991, 5, 22), "모름", "@kimjuncotton"));
		rows.add(new TraineeSpec("엑소", "찬열", Gender.MALE, Grade.R, 13, 13, 13, 13, 13, 33, LocalDate.of(1992, 11, 27), "모름", "@real__pcy"));
		rows.add(new TraineeSpec("엑소", "디오", Gender.MALE, Grade.R, 13, 13, 13, 13, 13, 33, LocalDate.of(1993, 1, 12), "모름", "@d.o.hkyungsoo"));
		rows.add(new TraineeSpec("엑소", "카이", Gender.MALE, Grade.SR, 15, 15, 15, 15, 15, 32, LocalDate.of(1994, 1, 14), "모름", "@zkdlin"));
		rows.add(new TraineeSpec("엑소", "세훈", Gender.MALE, Grade.SR, 15, 15, 15, 15, 15, 32, LocalDate.of(1994, 4, 12), "모름", "@oohsehun"));
		rows.add(new TraineeSpec("엑소", "레이", Gender.MALE, Grade.SR, 15, 15, 15, 15, 15, 34, LocalDate.of(1991, 10, 7), "모름", "@layzhang"));

		// 하츠투하츠
		rows.add(new TraineeSpec("하츠투하츠", "예온", Gender.FEMALE, Grade.N, 11, 11, 11, 11, 11, 15, LocalDate.of(2010, 4, 19), "모름", "공식 개인 계정 확인 안 됨 / 팀 계정 @hearts2hearts"));
		rows.add(new TraineeSpec("하츠투하츠", "에이나", Gender.FEMALE, Grade.N, 11, 11, 11, 11, 11, 17, LocalDate.of(2008, 12, 20), "모름", "공식 개인 계정 확인 안 됨 / 팀 계정 @hearts2hearts"));
		rows.add(new TraineeSpec("하츠투하츠", "지우", Gender.FEMALE, Grade.N, 11, 11, 11, 11, 11, 19, LocalDate.of(2006, 9, 7), "모름", "공식 개인 계정 확인 안 됨 / 팀 계정 @hearts2hearts"));
		rows.add(new TraineeSpec("하츠투하츠", "카르멘", Gender.FEMALE, Grade.N, 11, 11, 11, 11, 11, 20, LocalDate.of(2006, 3, 28), "모름", "공식 개인 계정 확인 안 됨 / 팀 계정 @hearts2hearts"));
		rows.add(new TraineeSpec("하츠투하츠", "주은", Gender.FEMALE, Grade.R, 13, 13, 13, 13, 13, 17, LocalDate.of(2008, 12, 3), "모름", "공식 개인 계정 확인 안 됨 / 팀 계정 @hearts2hearts"));
		rows.add(new TraineeSpec("하츠투하츠", "이안", Gender.FEMALE, Grade.R, 13, 13, 13, 13, 13, 16, LocalDate.of(2009, 10, 9), "모름", "공식 개인 계정 확인 안 됨 / 팀 계정 @hearts2hearts"));
		rows.add(new TraineeSpec("하츠투하츠", "유하", Gender.FEMALE, Grade.R, 13, 13, 13, 13, 13, 19, LocalDate.of(2007, 4, 12), "모름", "공식 개인 계정 확인 안 됨 / 팀 계정 @hearts2hearts"));
		rows.add(new TraineeSpec("하츠투하츠", "스텔라", Gender.FEMALE, Grade.R, 13, 13, 13, 13, 13, 18, LocalDate.of(2007, 6, 18), "모름", "공식 개인 계정 확인 안 됨 / 팀 계정 @hearts2hearts"));

		// 에스파
		rows.add(new TraineeSpec("에스파", "카리나", Gender.FEMALE, Grade.SSR, 17, 17, 17, 17, 17, 26, LocalDate.of(2000, 4, 11), "모름", "@katarinabluu"));
		rows.add(new TraineeSpec("에스파", "윈터", Gender.FEMALE, Grade.SSR, 17, 17, 17, 17, 17, 25, LocalDate.of(2001, 1, 1), "모름", "@imwinter"));
		rows.add(new TraineeSpec("에스파", "닝닝", Gender.FEMALE, Grade.SR, 15, 15, 15, 15, 15, 23, LocalDate.of(2002, 10, 23), "모름", "@imnotningning"));
		rows.add(new TraineeSpec("에스파", "지젤", Gender.FEMALE, Grade.SR, 15, 15, 15, 15, 15, 25, LocalDate.of(2000, 10, 30), "모름", "@aerichandesu"));

		// 레드벨벳
		rows.add(new TraineeSpec("레드벨벳", "아이린", Gender.FEMALE, Grade.R, 13, 13, 13, 13, 13, 35, LocalDate.of(1991, 3, 29), "모름", "@renebaebae"));
		rows.add(new TraineeSpec("레드벨벳", "웬디", Gender.FEMALE, Grade.R, 13, 13, 13, 13, 13, 32, LocalDate.of(1994, 2, 21), "모름", "@todayis_wendy"));
		rows.add(new TraineeSpec("레드벨벳", "조이", Gender.FEMALE, Grade.SR, 15, 15, 15, 15, 15, 29, LocalDate.of(1996, 9, 3), "모름", "@_imyour_joy"));
		rows.add(new TraineeSpec("레드벨벳", "예리", Gender.FEMALE, Grade.SR, 15, 15, 15, 15, 15, 27, LocalDate.of(1999, 3, 5), "모름", "@yerimiese"));
		rows.add(new TraineeSpec("레드벨벳", "슬기", Gender.FEMALE, Grade.SR, 15, 15, 15, 15, 15, 32, LocalDate.of(1994, 2, 10), "모름", "@hi_sseulgi"));
		rows.add(new TraineeSpec("히든", "문상훈", Gender.MALE, Grade.HIDDEN, 18, 18, 18, 18, 18, 100, LocalDate.of(1991, 5, 9), "먹기", "@moonbdns"));

		return rows;
	}

	private record TraineeSpec(
			String group,
			String name,
			Gender gender,
			Grade grade,
			int vocal,
			int dance,
			int star,
			int mental,
			int teamwork,
			Integer age,
			LocalDate birthday,
			String hobby,
			String instagram) {
	}
}


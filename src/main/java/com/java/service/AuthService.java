package com.java.service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.java.entity.Member;
import com.java.entity.MemberRank;
import com.java.repository.MemberRepository;
import com.java.repository.MyTraineeRepository;

@Service
public class AuthService {

    private static final String MID_REGEX = "^[A-Za-z0-9]{6,20}$";
    private static final String NICK_REGEX = "^[A-Za-z0-9가-힣]{3,12}$";
    private static final String EMAIL_REGEX = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";
    private static final String JUMIN_REGEX = "^\\d{6}-\\d{7}$";

    private final Map<String, String> emailCodeStore = new ConcurrentHashMap<>();
    private final Set<String> verifiedEmails = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final MemberRepository memberRepository;
    private final MyTraineeRepository myTraineeRepository;
    private final StarterTraineeGrantService starterTraineeGrantService;
    private final JuminCryptoService juminCryptoService;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(MemberRepository memberRepository, MyTraineeRepository myTraineeRepository,
            StarterTraineeGrantService starterTraineeGrantService,
            JuminCryptoService juminCryptoService) {
        this.memberRepository = memberRepository;
        this.myTraineeRepository = myTraineeRepository;
        this.starterTraineeGrantService = starterTraineeGrantService;
        this.juminCryptoService = juminCryptoService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public void sendEmailCode(String email) {
        String normalizedEmail = safeTrim(email);
        if (!normalizedEmail.matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException("올바른 이메일 형식을 입력해주세요.");
        }
        if (memberRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        String code = String.format("%06d", new java.util.Random().nextInt(1_000_000));
        emailCodeStore.put(normalizedEmail, code);

        System.out.println("=================================");
        System.out.println("[이메일 인증코드] " + normalizedEmail + " → " + code);
        System.out.println("=================================");
    }

    public boolean verifyEmailCode(String email, String code) {
        String normalizedEmail = safeTrim(email);
        String normalizedCode = safeTrim(code);
        String stored = emailCodeStore.get(normalizedEmail);

        if (stored != null && stored.equals(normalizedCode)) {
            emailCodeStore.remove(normalizedEmail);
            if (memberRepository.existsByEmail(normalizedEmail)) {
                throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
            }
            verifiedEmails.add(normalizedEmail);
            return true;
        }
        return false;
    }

    public boolean isEmailVerified(String email) {
        return verifiedEmails.contains(safeTrim(email));
    }

    @Transactional
    public Member signup(SignupRequest req) {
        String mid = safeTrim(req.mid());
        String email = safeTrim(req.email());
        String mname = safeTrim(req.mname());
        String nickname = safeTrim(req.nickname());
        String address = safeTrim(req.address());
        String jumin = safeTrim(req.jumin());

        if (mid.isBlank() || req.rawPassword() == null || req.rawPassword().isBlank() || email.isBlank()
                || mname.isBlank() || nickname.isBlank() || address.isBlank() || jumin.isBlank()) {
            throw new IllegalArgumentException("필수값이 누락되었습니다.");
        }
        if (!mid.matches(MID_REGEX)) {
            throw new IllegalArgumentException("아이디 규칙(영문+숫자, 6~20자)에 맞지 않습니다.");
        }
        if (!nickname.matches(NICK_REGEX)) {
            throw new IllegalArgumentException("닉네임 규칙(한글/영문/숫자, 3~12자)에 맞지 않습니다.");
        }
        if (!email.matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException("올바른 이메일 형식을 입력해주세요.");
        }
        if (!jumin.matches(JUMIN_REGEX)) {
            throw new IllegalArgumentException("주민등록번호 형식이 올바르지 않습니다.");
        }
        if (!isEmailVerified(email)) {
            throw new IllegalArgumentException("이메일 인증이 필요합니다.");
        }
        if (memberRepository.existsByMid(mid)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        if (memberRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }
        if (memberRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        Member m = new Member();
        m.setMid(mid);
        m.setMpw(passwordEncoder.encode(req.rawPassword()));
        m.setMname(mname);
        m.setNickname(nickname);
        m.setEmail(email);
        m.setPhone(normalizePhone(req.phone()));
        m.setAddress(address);
        m.setAddressDetail(safeTrim(req.addressDetail()));
        m.setJumin(juminCryptoService.encrypt(jumin));

        verifiedEmails.remove(email);
        Member saved = memberRepository.save(m);
        starterTraineeGrantService.grantStarterGroupsForMember(saved.getMno());
        return saved;
    }

    @Transactional(readOnly = true)
    public Member login(String mid, String rawPassword) {
        String id = safeTrim(mid);
        if (id.isBlank() || rawPassword == null) {
            return null;
        }
        return memberRepository.findByMid(id)
                .filter(m -> passwordEncoder.matches(rawPassword, m.getMpw()))
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public boolean isMidAvailable(String mid) {
        String v = safeTrim(mid);
        if (!v.matches(MID_REGEX)) {
            return false;
        }
        return !memberRepository.existsByMid(v);
    }

    @Transactional(readOnly = true)
    public boolean isNicknameAvailable(String nickname) {
        String v = safeTrim(nickname);
        if (!v.matches(NICK_REGEX)) {
            return false;
        }
        return !memberRepository.existsByNickname(v);
    }

    @Transactional(readOnly = true)
    public Member getMember(Long mno) {
        return memberRepository.findById(mno).orElse(null);
    }

    /** rankExp 기준으로 MEMBER_RANK 코드를 맞춤 (마이페이지·네비 등 표시 일관성) */
    @Transactional
    public void syncMemberRankFromExp(Long mno) {
        memberRepository.findById(mno).ifPresent(m -> {
            String expected = MemberRank.getRankByExp(m.getRankExp()).name();
            if (!expected.equals(m.getMemberRankCode())) {
                m.setMemberRankCode(expected);
            }
        });
    }

    private boolean memberOwnsTrainee(Long mno, Long traineeId) {
        if (mno == null || traineeId == null) {
            return false;
        }
        return myTraineeRepository.findByMemberIdAndTraineeId(mno, traineeId)
                .filter(mt -> mt.getQuantity() > 0)
                .isPresent();
    }

    @Transactional
    public void updateMypageRepTrainee(Long mno, Long traineeId) {
        Member m = memberRepository.findById(mno)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        if (traineeId == null) {
            m.setMypageRepTraineeId(null);
            return;
        }
        if (!memberOwnsTrainee(mno, traineeId)) {
            throw new IllegalArgumentException("보유(해금)된 연습생만 대표로 설정할 수 있습니다.");
        }
        m.setMypageRepTraineeId(traineeId);
    }

    @Transactional
    public void updateMypageCardTrainee(Long mno, Long traineeId) {
        Member m = memberRepository.findById(mno)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        if (traineeId == null) {
            m.setMypageCardTraineeId(null);
            return;
        }
        if (!memberOwnsTrainee(mno, traineeId)) {
            throw new IllegalArgumentException("보유(해금)된 연습생만 프로필 카드에 설정할 수 있습니다.");
        }
        m.setMypageCardTraineeId(traineeId);
    }

    /** 보유 목록과 맞지 않는 마이페이지 연습생 선택만 제거 */
    @Transactional
    public void sanitizeMypageTraineeSelections(Long mno, Set<Long> ownedTraineeIds) {
        if (mno == null || ownedTraineeIds == null) {
            return;
        }
        memberRepository.findById(mno).ifPresent(m -> {
            if (m.getMypageRepTraineeId() != null && !ownedTraineeIds.contains(m.getMypageRepTraineeId())) {
                m.setMypageRepTraineeId(null);
            }
            if (m.getMypageCardTraineeId() != null && !ownedTraineeIds.contains(m.getMypageCardTraineeId())) {
                m.setMypageCardTraineeId(null);
            }
        });
    }

    @Transactional
    public void updateNickname(Long mno, String nickname) {
        String v = safeTrim(nickname);
        if (!v.matches(NICK_REGEX)) {
            throw new IllegalArgumentException("닉네임 규칙(한글/영문/숫자, 3~12자)에 맞지 않습니다.");
        }
        if (memberRepository.existsByNickname(v)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }
        memberRepository.findById(mno).ifPresent(m -> m.setNickname(v));
    }

    @Transactional
    public void updateEmail(Long mno, String currentPw, String newEmail) {
        String email = safeTrim(newEmail);
        if (email.isBlank() || !email.matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException("올바른 이메일 형식을 입력해주세요.");
        }
        Member m = memberRepository.findById(mno)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        if (!passwordEncoder.matches(currentPw, m.getMpw())) {
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }
        if (memberRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        m.setEmail(email);
    }

    @Transactional
    public void updatePassword(Long mno, String currentPw, String newPw) {
        Member m = memberRepository.findById(mno)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        if (!passwordEncoder.matches(currentPw, m.getMpw())) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }
        if (newPw == null || newPw.length() < 6) {
            throw new IllegalArgumentException("새 비밀번호는 6자 이상이어야 합니다.");
        }
        m.setMpw(passwordEncoder.encode(newPw));
    }

    @Transactional
    public void deleteMember(Long mno, String password) {
        Member m = memberRepository.findById(mno)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        if (!passwordEncoder.matches(password, m.getMpw())) {
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }
        memberRepository.delete(m);
    }

    @Transactional
    public void updateProfileImage(Long mno, String storedFilename) {
        memberRepository.findById(mno).ifPresent(m -> m.setProfileImage(storedFilename));
    }

    @Transactional(readOnly = true)
    public String findMid(String mname, String email) {
        return memberRepository.findByMnameAndEmail(safeTrim(mname), safeTrim(email))
                .map(Member::getMid)
                .orElse(null);
    }

    @Transactional
    public boolean resetPassword(String mid, String mname, String email, String newPassword) {
        return memberRepository.findByMidAndMnameAndEmail(safeTrim(mid), safeTrim(mname), safeTrim(email))
                .map(m -> {
                    m.setMpw(passwordEncoder.encode(newPassword));
                    return true;
                })
                .orElse(false);
    }

    private static String normalizePhone(String phone) {
        String digits = safeTrim(phone).replaceAll("[^0-9]", "");
        if (digits.isBlank()) {
            return "";
        }
        if (digits.length() == 11) {
            return digits.substring(0, 3) + "-" + digits.substring(3, 7) + "-" + digits.substring(7);
        }
        if (digits.length() == 10) {
            return digits.substring(0, 3) + "-" + digits.substring(3, 6) + "-" + digits.substring(6);
        }
        return digits;
    }

    private static String safeTrim(String v) {
        return v == null ? "" : v.trim();
    }

    public record SignupRequest(
            String mid,
            String rawPassword,
            String mname,
            String nickname,
            String email,
            String phone,
            String address,
            String addressDetail,
            String jumin
    ) {
    }
}

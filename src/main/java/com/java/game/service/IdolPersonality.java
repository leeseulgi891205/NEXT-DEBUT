package com.java.game.service;

/**
 * 연습생 슬롯(픽 순서)별 고정 성격 — 대사 톤 분리용 (DB 없이 4인 전부 다른 성향).
 */
public enum IdolPersonality {
	BUBBLY("활발", "활발하고 텐션을 끌어올림"),
	CALM("차분", "차분하고 이성적"),
	TSUNDERE("츤데레", "투덜대지만 결국 돕는 타입"),
	GENTLE("다정", "다정하고 배려심 많음"),
	PRANKSTER("장난꾸러기", "분위기를 장난으로 푸는 타입"),
	SERIOUS("진지함", "원칙/집중을 중시"),
	PERFECTIONIST("완벽주의", "디테일 집착, 기준이 높음"),
	SENSITIVE("예민함", "작은 변화도 빠르게 캐치"),
	OPTIMISTIC("낙천적", "긍정적으로 밀고 나감"),
	SHY("소심함", "조심스럽고 눈치를 봄"),
	LEADER("리더형", "정리/주도/결정"),
	FREE_SPIRIT("자유분방", "틀에 얽매이지 않음"),
	COOL("냉정함", "감정보다 판단"),
	COMPETITIVE("승부욕강함", "이기고 싶어 함"),
	DEPENDENT("의존적", "확신/지지가 필요"),
	RELIABLE("든든함", "묵직하게 받쳐줌"),
	BLUNT("직설적", "돌려 말하지 않음"),
	SENTIMENTAL("감성적", "감정 표현이 풍부"),
	LAID_BACK("느긋함", "페이스를 느리게 유지"),
	REBELLIOUS("반항적", "규칙/지시에 저항");

	private final String shortLabel;
	private final String styleDescription;

	IdolPersonality(String shortLabel, String styleDescription) {
		this.shortLabel = shortLabel;
		this.styleDescription = styleDescription;
	}

	public String getShortLabel() {
		return shortLabel;
	}

	public String getPromptLine() {
		return name() + ": " + styleDescription;
	}

	/** 프롬프트 아이돌 목록용 — "성격: …" 뒤에 붙는 설명 */
	public String getStyleDescription() {
		return styleDescription;
	}

	public static IdolPersonality forPickOrder(int pickOrder) {
		IdolPersonality[] v = values();
		return v[Math.floorMod(pickOrder - 1, v.length)];
	}

	public static IdolPersonality fromCodeOrNull(String code) {
		if (code == null || code.isBlank()) {
			return null;
		}
		try {
			return IdolPersonality.valueOf(code.trim().toUpperCase(java.util.Locale.ROOT));
		} catch (Exception ignored) {
			return null;
		}
	}
}

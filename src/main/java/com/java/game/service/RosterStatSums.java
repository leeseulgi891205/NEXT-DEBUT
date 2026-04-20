package com.java.game.service;

/**
 * 한 런 로스터의 다섯 능력치 합계(멤버별 값의 합). 랭킹 표시 점수(케미·진행 보정)와는 별개인 원천 합산.
 */
public record RosterStatSums(int vocalSum, int danceSum, int starSum, int mentalSum, int teamworkSum) {

	public int abilityTotal() {
		return vocalSum + danceSum + starSum + mentalSum + teamworkSum;
	}
}

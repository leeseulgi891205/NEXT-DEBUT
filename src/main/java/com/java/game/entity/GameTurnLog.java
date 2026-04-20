package com.java.game.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "GAME_TURN_LOG")
public class GameTurnLog {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;

	@Column(name = "RUN_ID", nullable = false)
	private Long runId;

	/** 1..168 (아침=홀수, 저녁=짝수) / 평가 턴은 별도 phase로 기록 */
	@Column(name = "TURN_INDEX", nullable = false)
	private int turnIndex;

	@Column(name = "PHASE", nullable = false, length = 30)
	private String phase;

	@Column(name = "BUCKET", nullable = false, length = 40)
	private String bucket;

	@Column(name = "SCENE_ID")
	private Long sceneId;

	@Column(name = "EVENT_TYPE", length = 50)
	private String eventType;

	@Column(name = "CHOICE_KEY", nullable = false, length = 10)
	private String choiceKey;

	@Column(name = "STAT_TARGET", nullable = false, length = 20)
	private String statTarget;

	@Column(name = "DELTA", nullable = false)
	private int delta;

	@Column(name = "BEFORE_VAL", nullable = false)
	private int beforeVal;

	@Column(name = "AFTER_VAL", nullable = false)
	private int afterVal;

	@Column(name = "CREATED_AT", nullable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	protected GameTurnLog() {}

	public GameTurnLog(Long runId, int turnIndex, String phase, String bucket, Long sceneId, String eventType,
			String choiceKey, String statTarget, int delta, int beforeVal, int afterVal) {
		this.runId = runId;
		this.turnIndex = turnIndex;
		this.phase = phase;
		this.bucket = bucket;
		this.sceneId = sceneId;
		this.eventType = eventType;
		this.choiceKey = choiceKey;
		this.statTarget = statTarget;
		this.delta = delta;
		this.beforeVal = beforeVal;
		this.afterVal = afterVal;
	}

	public Long getId() { return id; }
	public Long getRunId() { return runId; }
	public int getTurnIndex() { return turnIndex; }
	public String getPhase() { return phase; }
	public String getBucket() { return bucket; }
	public Long getSceneId() { return sceneId; }
	public String getEventType() { return eventType; }
	public String getChoiceKey() { return choiceKey; }
	public String getStatTarget() { return statTarget; }
	public int getDelta() { return delta; }
	public int getBeforeVal() { return beforeVal; }
	public int getAfterVal() { return afterVal; }
	public LocalDateTime getCreatedAt() { return createdAt; }
}


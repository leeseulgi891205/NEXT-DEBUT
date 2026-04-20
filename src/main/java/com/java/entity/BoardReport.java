package com.java.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * 게시글 / 댓글 신고 엔티티
 */
@Entity
@Table(name = "BOARD_REPORT", uniqueConstraints = @UniqueConstraint(columnNames = { "TARGET_TYPE", "TARGET_ID",
		"REPORTER_MNO" }))
public class BoardReport {

	private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "TARGET_TYPE", nullable = false, length = 10)
	private String targetType;

	@Column(name = "TARGET_ID", nullable = false)
	private Long targetId;

	@Column(name = "REPORTER_MNO", nullable = false)
	private Long reporterMno;

	@Column(name = "REPORTER_NICK", length = 60)
	private String reporterNick;

	@Column(name = "REASON", nullable = false, length = 20)
	private String reason;

	@Column(name = "DESCRIPTION", length = 500)
	private String description;

	@Column(name = "STATUS", nullable = false, length = 10)
	private String status = "pending";

	@Column(name = "CREATED_AT", nullable = false)
	private LocalDateTime createdAt;

	@PrePersist
	void onCreate() {
		if (createdAt == null)
			createdAt = LocalDateTime.now();
	}

	protected BoardReport() {
	}

	public BoardReport(String targetType, Long targetId, Long reporterMno, String reporterNick, String reason,
			String description) {
		this.targetType = targetType;
		this.targetId = targetId;
		this.reporterMno = reporterMno;
		this.reporterNick = reporterNick;
		this.reason = reason;
		this.description = description;
	}

	public Long getId() {
		return id;
	}

	public String getTargetType() {
		return targetType;
	}

	public Long getTargetId() {
		return targetId;
	}

	public Long getReporterMno() {
		return reporterMno;
	}

	public String getReporterNick() {
		return reporterNick;
	}

	public String getReason() {
		return reason;
	}

	public String getDescription() {
		return description;
	}

	public String getStatus() {
		return status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCreatedAtStr() {
		return createdAt != null ? createdAt.format(FMT) : "";
	}

	public String getReasonLabel() {
		return switch (reason) {
		case "spam" -> "스팸/광고";
		case "obscene" -> "음란/불쾌";
		case "abuse" -> "욕설/비방";
		case "illegal" -> "불법정보";
		default -> "기타";
		};
	}
}

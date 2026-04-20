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

@Entity
@Table(name = "MEMBER_SANCTION")
public class MemberSanction {

	private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "MEMBER_MNO", nullable = false)
	private Long memberMno;

	@Column(name = "ADMIN_MNO")
	private Long adminMno;

	@Column(name = "ADMIN_NICK", length = 60)
	private String adminNick;

	@Column(name = "SANCTION_DAYS", nullable = false)
	private int sanctionDays;

	@Column(name = "REASON", length = 255)
	private String reason;

	@Column(name = "CREATED_AT", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "EXPIRES_AT", nullable = false)
	private LocalDateTime expiresAt;

	@PrePersist
	void onCreate() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}

	protected MemberSanction() {
	}

	public MemberSanction(Long memberMno, Long adminMno, String adminNick, int sanctionDays, String reason,
			LocalDateTime expiresAt) {
		this.memberMno = memberMno;
		this.adminMno = adminMno;
		this.adminNick = adminNick;
		this.sanctionDays = sanctionDays;
		this.reason = reason;
		this.expiresAt = expiresAt;
	}

	public Long getId() {
		return id;
	}

	public Long getMemberMno() {
		return memberMno;
	}

	public Long getAdminMno() {
		return adminMno;
	}

	public String getAdminNick() {
		return adminNick;
	}

	public int getSanctionDays() {
		return sanctionDays;
	}

	public String getReason() {
		return reason;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getExpiresAt() {
		return expiresAt;
	}

	public String getCreatedAtStr() {
		return createdAt != null ? createdAt.format(FMT) : "";
	}

	public String getExpiresAtStr() {
		return expiresAt != null ? expiresAt.format(FMT) : "";
	}
}

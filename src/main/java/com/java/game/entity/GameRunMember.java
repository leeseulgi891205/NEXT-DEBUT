package com.java.game.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "GAME_RUN_MEMBER")
public class GameRunMember {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "RUN_ID", nullable = false)
    private GameRun run;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "TRAINEE_ID", nullable = false)
    private Trainee trainee;

    @Column(name = "PICK_ORDER", nullable = false)
    private int pickOrder;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "STATUS_CODE", length = 40)
    private String statusCode;

    @Column(name = "STATUS_LABEL", length = 60)
    private String statusLabel;

    @Column(name = "STATUS_DESC", length = 255)
    private String statusDesc;

    @Column(name = "STATUS_TURNS_LEFT")
    private Integer statusTurnsLeft = 0;

    protected GameRunMember() {}

    public GameRunMember(GameRun run, Trainee trainee, int pickOrder) {
        this.run = run;
        this.trainee = trainee;
        this.pickOrder = pickOrder;
        this.createdAt = LocalDateTime.now();
        this.statusTurnsLeft = 0;
    }

    public Long getId()              { return id; }
    public GameRun getRun()          { return run; }
    public Trainee getTrainee()      { return trainee; }
    public int getPickOrder()        { return pickOrder; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public String getStatusCode() { return statusCode; }
    public String getStatusLabel() { return statusLabel; }
    public String getStatusDesc() { return statusDesc; }
    public int getStatusTurnsLeft() { return statusTurnsLeft == null ? 0 : Math.max(0, statusTurnsLeft); }
    public boolean hasActiveStatus() { return statusCode != null && !statusCode.isBlank() && getStatusTurnsLeft() > 0; }

    public void setStatus(String code, String label, String desc, int turnsLeft) {
        this.statusCode = code;
        this.statusLabel = label;
        this.statusDesc = desc;
        this.statusTurnsLeft = Math.max(0, turnsLeft);
        if (this.statusTurnsLeft == 0) {
            clearStatus();
        }
    }

    public void clearStatus() {
        this.statusCode = null;
        this.statusLabel = null;
        this.statusDesc = null;
        this.statusTurnsLeft = 0;
    }

    public void tickStatus() {
        if (!hasActiveStatus()) {
            clearStatus();
            return;
        }
        int next = getStatusTurnsLeft() - 1;
        if (next <= 0) {
            clearStatus();
            return;
        }
        this.statusTurnsLeft = next;
    }
}

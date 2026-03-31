package com.brovko.SsuBench.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "bids")
public class Bid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "task_id", foreignKey = @ForeignKey(name = "fk_bid_task"), nullable = false)
    private Task task;

    @ManyToOne
    @JoinColumn(name = "executor_user_id", foreignKey = @ForeignKey(name = "fk_bid_executorUser"), nullable = false)
    private User executorUser;

    @Column(nullable = false)
    private BidStatus status = BidStatus.NEW;

    public enum BidStatus {
        NEW,
        IN_PROGRESS,
        IN_REVIEW_BY_CUSTOMER,
        DONE
    }
}

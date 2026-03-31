package com.brovko.SsuBench.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_user_id", foreignKey = @ForeignKey(name = "fk_task_customerUser"), nullable = false)
    private User customerUser;

    @OneToOne
    @JoinColumn(name = "confirmed_bid_id", foreignKey = @ForeignKey(name = "fk_task_confirmedBid"))
    private Bid confirmedBid;

    @Column(nullable = false)
    private String text;

    @Column(nullable = false)
    private Long rewardMoney;
}

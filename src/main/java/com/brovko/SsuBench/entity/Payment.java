package com.brovko.SsuBench.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bid_id", foreignKey = @ForeignKey(name = "fk_payment_bid"), nullable = false)
    private Bid bid;

    @ManyToOne
    @JoinColumn(name = "customer_user_id", foreignKey = @ForeignKey(name = "fk_payment_customerUser"), nullable = false)
    private User customerUser;

    @ManyToOne
    @JoinColumn(name = "executor_user_id", foreignKey = @ForeignKey(name = "fk_payment_executorUser"), nullable = false)
    private User executorUser;

    @Column(nullable = false)
    private Long amount;
}

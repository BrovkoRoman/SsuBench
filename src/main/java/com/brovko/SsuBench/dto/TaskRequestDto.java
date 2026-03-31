package com.brovko.SsuBench.dto;

import com.brovko.SsuBench.exception.ValidationException;

public class TaskRequestDto {
    private String text;

    private Long rewardMoney;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getRewardMoney() {
        return rewardMoney;
    }

    public void setRewardMoney(Long rewardMoney) {
        this.rewardMoney = rewardMoney;
    }

    public void validate() {
        if (text == null) {
            throw new ValidationException("text is null");
        }

        if (rewardMoney == null) {
            throw new ValidationException("rewardMoney is null");
        }
    }
}

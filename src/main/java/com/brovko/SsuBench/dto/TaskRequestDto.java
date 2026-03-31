package com.brovko.SsuBench.dto;

import com.brovko.SsuBench.exception.ValidationException;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TaskRequestDto {
    private String text;

    private Long rewardMoney;

    public void validate() {
        if (text == null) {
            throw new ValidationException("text is null");
        }

        if (rewardMoney == null) {
            throw new ValidationException("rewardMoney is null");
        }
    }
}

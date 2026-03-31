package com.brovko.SsuBench.dto;

import com.brovko.SsuBench.exception.ValidationException;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BidRequestDto {
    private Long taskId;

    public void validate() {
        if (taskId == null) {
            throw new ValidationException("taskId is null");
        }
    }
}

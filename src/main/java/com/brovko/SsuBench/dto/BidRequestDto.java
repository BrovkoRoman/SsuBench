package com.brovko.SsuBench.dto;

import com.brovko.SsuBench.exception.ValidationException;

public class BidRequestDto {
    private Long taskId;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public void validate() {
        if (taskId == null) {
            throw new ValidationException("taskId is null");
        }
    }
}

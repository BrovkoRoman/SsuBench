package com.brovko.SsuBench.service;

import com.brovko.SsuBench.dto.TaskRequestDto;
import com.brovko.SsuBench.entity.Bid;
import com.brovko.SsuBench.entity.Task;
import com.brovko.SsuBench.repository.BidRepository;
import com.brovko.SsuBench.repository.TaskRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TaskService {
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private BidRepository bidRepository;

    public Task save(Task task) {
        return taskRepository.save(task);
    }

    public Task findById(Long taskId) {
        return taskRepository.findById(taskId).orElse(null);
    }

    @Transactional
    public Task confirmBid(Bid bid) {
        bid.setStatus(Bid.BidStatus.IN_PROGRESS);
        Task task = bid.getTask();
        task.setConfirmedBid(bid);

        task = taskRepository.save(task);
        bidRepository.save(bid);

        return task;
    }

    public Page<Task> getTasks(Pageable pageable) {
        return taskRepository.findAll(pageable);
    }

    public Task createTask(TaskRequestDto taskRequestDto) {
        taskRequestDto.validate();
        Task task = new Task();
        task.setText(taskRequestDto.getText());
        task.setRewardMoney(taskRequestDto.getRewardMoney());
        return task;
    }

    public void deleteById(Long taskId) {
        taskRepository.deleteById(taskId);
    }
}

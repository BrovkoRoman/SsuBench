package com.brovko.SsuBench.service;

import com.brovko.SsuBench.dto.BidRequestDto;
import com.brovko.SsuBench.entity.Bid;
import com.brovko.SsuBench.repository.BidRepository;
import com.brovko.SsuBench.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class BidService {
    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private TaskRepository taskRepository;

    public Bid save(Bid bid) {
        return bidRepository.save(bid);
    }

    public Bid findById(Long bidId) {
        return bidRepository.findById(bidId).orElse(null);
    }

    public Page<Bid> getBids(Pageable pageable) {
        return bidRepository.findAll(pageable);
    }

    public Bid createBid(BidRequestDto bidRequestDto) {
        bidRequestDto.validate();
        Bid bid = new Bid();
        bid.setTask(taskRepository.findById(bidRequestDto.getTaskId()).orElse(null));
        return bid;
    }

    public void deleteById(Long bidId) {
        bidRepository.deleteById(bidId);
    }
}

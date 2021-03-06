package com.earnest.web.service;

import com.earnest.video.entity.Video;
import com.earnest.web.repository.VideoRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.Assert;

@AllArgsConstructor
public class MongoVideoService implements VideoService {

    private final VideoRepository videoRepository;

    @Override
    public Page<Video> findByCategory(Pageable pageRequest, Video.Category category) {
        Assert.notNull(pageRequest, "pageRequest is required");
        return videoRepository.findByCategory(pageRequest, category);
    }

    @Override
    public Video get(String id) {
        Assert.hasText(id, "id is required");
        return videoRepository.findById(id).orElse(null);
    }

    @Override
    public Page<Video> findAll(Pageable pageRequest) {
        Assert.notNull(pageRequest, "pageRequest is required");
        return videoRepository.findAll(pageRequest);
    }
}

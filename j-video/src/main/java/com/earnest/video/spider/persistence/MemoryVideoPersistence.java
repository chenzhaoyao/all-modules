package com.earnest.video.spider.persistence;

import com.earnest.video.entity.VideoEntity;
import lombok.Getter;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class MemoryVideoPersistence<T extends VideoEntity> implements VideoPersistence<T> {

    @Getter
    private final Map<String, T> videoMap;
    @Getter
    private final Map<VideoEntity.Category, List<T>> categoryTMap;

    private final AtomicLong idLong = new AtomicLong(1000);

    public MemoryVideoPersistence(Map<String, T> videoMap) {
        Assert.notNull(videoMap, "the videoMap is required");
        this.videoMap = videoMap;
        this.categoryTMap = new ConcurrentHashMap<>();
    }

    public MemoryVideoPersistence() {
        this(new ConcurrentSkipListMap<>());
    }


    @Override
    public void save(List<T> entities) {

        if (!CollectionUtils.isEmpty(entities)) {
            //将结果按照id添加
            Map<VideoEntity.Category, List<T>> videosByCategory = entities.stream()
                    .filter(Objects::nonNull)
                    .peek(e -> {//当没有ID时为其设置ID
                        if (e.getId() == null) {
                            e.setId(idLong.incrementAndGet() + "");
                        }
                    })
                    .peek(e -> videoMap.put(e.getId(), e))
                    .collect(Collectors.groupingBy(VideoEntity::getCategory));

            //将结果进行分类添加
            videosByCategory.keySet()
                    .forEach(c ->
                            categoryTMap.compute(c, ((category, ts) -> {
                                ts = Optional.ofNullable(ts).orElse(new ArrayList<>());
                                ts.addAll(videosByCategory.get(category));
                                return ts;
                            }))
                    );

        }
    }

    @Override
    public long count() {
        return videoMap.size();
    }


}
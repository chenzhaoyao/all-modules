package com.earnest.crawler.builder;

import com.earnest.crawler.extractor.EmptyHttpRequestExtractor;
import com.earnest.crawler.extractor.HttpRequestExtractor;
import com.earnest.crawler.scheduler.BlockingUniqueScheduler;
import com.earnest.crawler.scheduler.FixedArrayScheduler;
import com.earnest.crawler.scheduler.Scheduler;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.HttpUriRequest;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
public class SchedulerConfigurer extends SharedSpiderConfigurer {

    private Scheduler scheduler;


    public SharedSpiderConfigurer blockingUnique(int timeout) {
        scheduler = new BlockingUniqueScheduler(timeout);
        return this;
    }

    public SharedSpiderConfigurer blockingUnique() {
        return blockingUnique(0);
    }

    public SharedSpiderConfigurer fixed(int initialCapacity) {
        scheduler = new FixedArrayScheduler(initialCapacity);
        return this;
    }

    //需要在HttpUriRequestExtractorConfigurer后进行configure()。
    @Override
    protected int order() {
        return 4;
    }

    @Override
    @SuppressWarnings("unchecked")
    void configure() {
        //获取请求列表
        List<HttpUriRequest> httpUriRequests = (List<HttpUriRequest>) sharedObjectMap.remove(HttpUriRequest.class);
        Assert.state(!CollectionUtils.isEmpty(httpUriRequests), "httpUriRequest is empty");
        log.debug("Obtained the initial request list:{}", httpUriRequests.stream().map(HttpUriRequest::getURI).collect(toList()));

        if (scheduler == null) {
            //判断请求
            List<?> httpRequestExtractors = sharedObjectMap.get(HttpRequestExtractor.class);

            if (httpRequestExtractors.isEmpty() || (httpRequestExtractors.get(0) instanceof EmptyHttpRequestExtractor)) {
                //初始化固定的调度器
                fixed(httpUriRequests.size());
            } else {//默认初始化调度器
                blockingUnique();
            }

        }

        //加入请求列表
        scheduler.putAll(httpUriRequests);

        sharedObjectMap.put(Scheduler.class, Collections.singletonList(scheduler));
    }
}

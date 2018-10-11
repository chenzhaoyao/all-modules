package com.earnest.crawler.core;


import com.earnest.crawler.core.downloader.Downloader;
import com.earnest.crawler.core.exception.TakeTimeoutException;
import com.earnest.crawler.core.extractor.HttpRequestExtractor;
import com.earnest.crawler.core.pipeline.Pipeline;
import com.earnest.crawler.core.scheduler.Scheduler;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.HttpUriRequest;
import org.springframework.util.CustomizableThreadCreator;

import java.util.concurrent.*;

@Slf4j
public class AsyncSpider extends SyncSpider {

    private final ThreadPoolExecutor threadPool;

    public AsyncSpider(Downloader downloader, Scheduler scheduler, HttpRequestExtractor httpRequestExtractor, Pipeline pipeline, Integer threadNumber) {
        super(downloader, scheduler, httpRequestExtractor, pipeline);
        this.threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadNumber, new SpiderThreadFactory());
    }


    @Override
    public void start() {

        //进行下载
        for (int i = 0; i < threadPool.getMaximumPoolSize(); i++) {
            threadPool.execute(() -> {
                while (true) {
                    try {
                        HttpUriRequest httpUriRequest = scheduler.take();
                        if (httpUriRequest == null) {
                            break;
                        }
                        handleStringResponseResult(downloader.download(httpUriRequest));
                    } catch (TakeTimeoutException e) {
                        break;
                    }
                }
            });
        }
        log.info("download completed, exit...");
        afterCompleted();

    }



    @Override
    public void stop() {
        threadPool.shutdown();
    }


    @Override
    public boolean isRunning() {
        return threadPool.getActiveCount() != 0;
    }


    @Override
    public void close() {
        if (!threadPool.isShutdown()) {
            threadPool.shutdown();
        }
        super.close();
    }


    /**
     * 爬虫的线程工厂
     */
    private class SpiderThreadFactory extends CustomizableThreadCreator implements ThreadFactory {
        final static String DEFAULT_THREAD_GROUP_NAME = "spider";
        final static String DEFAULT_THREAD__NAME_PREFIX = "crawler";

        public SpiderThreadFactory() {
            this(DEFAULT_THREAD__NAME_PREFIX);
        }

        public SpiderThreadFactory(String threadNamePrefix) {
            super(threadNamePrefix);
            setThreadGroupName(DEFAULT_THREAD_GROUP_NAME);
        }

        @Override
        public Thread newThread(Runnable runnable) {
            return createThread(runnable);
        }
    }

}

package com.earnest.web.autoconfigure;

import com.earnest.crawler.Browser;
import com.earnest.crawler.proxy.DefaultApiHttpProxySupplier;
import com.earnest.crawler.proxy.HttpProxySupplier;
import com.earnest.video.episode.EpisodeFetcher;
import com.earnest.video.episode.EpisodeFetcherManager;
import com.earnest.video.episode.IQiYiEpisodeFetcher;
import com.earnest.video.parser.*;
import com.earnest.video.search.DefaultPlatformSearcherManager;
import com.earnest.video.search.IQiYiPlatformHttpClientSearcher;
import com.earnest.video.search.PlatformSearcher;
import com.earnest.video.search.PlatformSearcherManager;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.impl.client.AbstractResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 所有的单例<code>Bean</code>的配置。都交由<code>Spring</code>容器管理。
 */
@Configuration
public class SingletonBeanAutoConfiguration {

    /**
     * 全局公用一个{@link HttpClient}。
     *
     * @return
     */
    @Bean
    public CloseableHttpClient httpClient() {
        return HttpClients.custom()
                .setUserAgent(Browser.GOOGLE.userAgent())
                .setMaxConnTotal(100)
                .build();
    }

    /**
     * 全局共有一个{@link ResponseHandler}。
     *
     * @return
     */
    @Bean
    public ResponseHandler<String> responseHandler() {
        return new AbstractResponseHandler<>() {
            @Override
            public String handleEntity(HttpEntity entity) throws IOException {
                return EntityUtils.toString(entity, Charset.defaultCharset());
            }
        };
    }

    @Bean
    public VideoAddressParser videoAddressParser(HttpClient httpClient,
                                                 ResponseHandler<String> responseHandler,
                                                 @Autowired(required = false) HttpProxySupplier httpProxySupplier) {
        VideoAddressParserManager videoAddressParserManager = new VideoAddressParserManager();

        //add iqiyi
        IQiYiVideoAddressParser iQiYiVideoAddressParser = new IQiYiVideoAddressParser(httpClient, responseHandler);
        iQiYiVideoAddressParser.setHttpProxySupplier(httpProxySupplier);
        videoAddressParserManager.addWork(iQiYiVideoAddressParser);
        //add qq

        //add awk
        AwkVideoAddressParser awkVideoAddressParser = new AwkVideoAddressParser(httpClient, responseHandler);
        awkVideoAddressParser.setHttpProxySupplier(httpProxySupplier);



        return videoAddressParserManager;
    }

    /**
     * @return {@link HttpProxySupplier}
     */
//    @Bean
    public HttpProxySupplier httpProxySupplier(HttpClient httpClient, ResponseHandler<String> responseHandler) {

        return new DefaultApiHttpProxySupplier("http://192.168.10.131:5010", httpClient, responseHandler);
    }

    //==========================episodeFetcher==================

    @Bean
    public EpisodeFetcher episodeFetcher(@Autowired(required = false) List<EpisodeFetcher> episodeFetchers,
                                         CloseableHttpClient httpClient, ResponseHandler<String> stringResponseHandler,
                                         @Autowired(required = false) HttpProxySupplier httpProxySupplier) {
        EpisodeFetcherManager episodeFetcherManager = new EpisodeFetcherManager();
        episodeFetcherManager.setHttpProxySupplier(httpProxySupplier);
        //add iQiYi
        episodeFetcherManager.addWork(new IQiYiEpisodeFetcher(httpClient, stringResponseHandler));

        Optional.ofNullable(episodeFetchers).stream()
                .flatMap(Collection::stream).forEach(episodeFetcherManager::addWork);


        return episodeFetcherManager;
    }


    //==========================//episodeFetcher==================
    @Bean
    public PlatformSearcherManager platformSearcherManager(@Autowired(required = false) List<PlatformSearcher> platformSearchers,
                                                           HttpClient httpClient, ResponseHandler<String> responseHandler,
                                                           @Autowired(required = false) HttpProxySupplier httpProxySupplier) {
        DefaultPlatformSearcherManager platformSearcherManager = new DefaultPlatformSearcherManager();
        platformSearcherManager.setCompletionService(threadPoolTaskExecutor());
        //add IQiYi
        platformSearcherManager.addWork(new IQiYiPlatformHttpClientSearcher(httpClient, responseHandler, httpProxySupplier));
        //add custom
        Optional.ofNullable(platformSearchers).stream()
                .flatMap(Collection::stream).forEach(platformSearcherManager::addWork);

        return platformSearcherManager;
    }

    //==========================//Manager==================


    //=========================Spring Thread Pool=================
    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(10);//线程池维护的最少数量
        threadPoolTaskExecutor.setMaxPoolSize(5000);//线程池维护的最大数量
        threadPoolTaskExecutor.setKeepAliveSeconds(60 * 60);
        threadPoolTaskExecutor.setQueueCapacity(500);
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }
    //=======================//Spring Thread Pool=================


}
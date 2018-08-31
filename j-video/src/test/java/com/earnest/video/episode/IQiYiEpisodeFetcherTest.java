package com.earnest.video.episode;

import com.earnest.video.entity.Episode;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.util.List;


public class IQiYiEpisodeFetcherTest {
    IQiYiEpisodeFetcher episodeFetcher = new IQiYiEpisodeFetcher(CloseableHttpClientFactoryBean.INSTANCE.getObject());

    @Test
    public void fetch() throws Exception {

        List<Episode> episodes = episodeFetcher.fetch("http://www.iqiyi.com/a_19rrh6yhid.html#vfrm=2-4-0-1", null);
        Assert.assertNotNull(episodes);
    }

    @Test
    public void aa() throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse httpResponse = httpClient.execute(new HttpGet("http://vv.video.qq.com/geturl?vid=b0113x7xx0m&otype=xml&platform=1&ran=0%2E9652906153351068"));
        HttpEntity entity = httpResponse.getEntity();
        System.out.println(EntityUtils.toString(entity));
    }

    @Test
    public void bb() {
        System.out.println(URI.create("http://www.iqiyi.com/a_19rrgzldkl.html#vfrm=2-4-0-1").getHost());
        System.out.println(URI.create("http://list.iqiyi.com/www/4/-------------4-1-1-iqiyi--.html").getHost());
        System.out.println(URI.create("http://list.iqiyi.com/www/4/-------------4-1-1-iqiyi--.html").getPath());
        System.out.println(URI.create("http://list.iqiyi.com/www/4/-------------4-1-1-iqiyi--.html").resolve("a"));
    }

}
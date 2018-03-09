package com.earnest.crawler.core.request;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.util.CollectionUtils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


public class HttpUriRequestAdapter extends HttpEntityEnclosingRequestBase {


    private final String method;

    public HttpUriRequestAdapter(HttpRequest httpRequest) {
        //set Method
        this.method = httpRequest.getMethod();
        //set URI
        setURI(URI.create(httpRequest.getUrl()));

        //set Header
        setHeaders(httpRequest.getHeaders());

        //set Parameters
        setHttpEntity(httpRequest);
        //set requestConfig
        setConfig(createRequestConfig(httpRequest));
        //

    }

    private void setHttpEntity(HttpRequest httpRequest) {
        Map<String, String> parameters = httpRequest.getParameters();

        if (!StringUtils.equalsAnyIgnoreCase("GET", "DELETE") && !CollectionUtils.isEmpty(parameters)) {
            HttpEntity httpRequestEntity=null;
            //判断content-type
            String contentType = httpRequest.getHeaders().get("content-type");
            //JSON
            if (StringUtils.equalsIgnoreCase(ContentType.APPLICATION_JSON.toString(), contentType)) {
                httpRequestEntity = new StringEntity(JSONObject.toJSONString(parameters), Consts.UTF_8);
            } else {

                List<BasicNameValuePair> nameValuePairs = parameters.keySet().stream()
                        .map(k -> new BasicNameValuePair(k, parameters.get(k)))
                        .collect(Collectors.toList());

                try {
                    httpRequestEntity = new UrlEncodedFormEntity(nameValuePairs);
                } catch (UnsupportedEncodingException e) {
                   e.printStackTrace();
                }

            }

            setEntity(httpRequestEntity);
        }
    }


    private RequestConfig createRequestConfig(HttpRequest httpRequest) {
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        //set Proxy
        HttpRequest.HttpProxy httpProxy = httpRequest.getHttpProxy();
        if (Objects.nonNull(httpProxy)) {
            requestConfigBuilder.setProxy(httpProxy.getHttpHost());
        }
        //set connectTimeout
        int connectTimeout = httpRequest.getConnectTimeout();
        if (connectTimeout != 0) {
            requestConfigBuilder.setConnectTimeout(connectTimeout);
        }


        return requestConfigBuilder.build();

    }

    private void setConnectTimeout(int connectTimeout, RequestConfig.Builder requestConfigBuilder) {
        if (connectTimeout != 0) {
            requestConfigBuilder.setConnectTimeout(connectTimeout);
        }
    }


    private void setHeaders(Map<String, String> headersMap) {
        if (CollectionUtils.isEmpty(headersMap)) {
            return;
        }

        Header[] headers = headersMap.keySet().stream()
                .map(k -> new BasicHeader(k, headersMap.get(k)))
                .toArray(Header[]::new);

        super.setHeaders(headers);

    }

    @Override
    public String getMethod() {
        return this.method;
    }
}

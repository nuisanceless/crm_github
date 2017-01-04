package com.drugoogle.sellscrm.rest;

import com.drugoogle.sellscrm.common.MyApplication;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;


/**
 * Created by wgh on 2016/3/31.
 */

@EBean
public class MyInterceptor implements ClientHttpRequestInterceptor {

    private static final String TAG = MyInterceptor.class.getSimpleName();

    boolean isFake = false;

    private MyRestClient mRestClient;

//    @Pref
//    MyPrefs_ mPrefs;

    @App
    MyApplication mApp;

    public void setRestClient(MyRestClient restClient)
    {
        mRestClient = restClient;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        if(isFake)
        {
            return new FakeClientHttpResponse(HttpStatus.OK, "{}");
        }

        ClientHttpResponse response = execution.execute(request, body);
        return response;
    }

    static class FakeClientHttpResponse implements ClientHttpResponse
    {
        private HttpStatus mStatus;
        private InputStream mInputStream;
        private HttpHeaders mHttpHeaders;
        public FakeClientHttpResponse(HttpStatus status, String body)
        {
            mStatus = status;
            try {
                mInputStream = new ByteArrayInputStream(body.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            mHttpHeaders = new HttpHeaders();
            mHttpHeaders.setContentType(MediaType.APPLICATION_JSON);
        }

        @Override
        public HttpStatus getStatusCode() throws IOException {
            return mStatus;
        }

        @Override
        public int getRawStatusCode() throws IOException {
            return mStatus.value();
        }

        @Override
        public String getStatusText() throws IOException {
            return mStatus.getReasonPhrase();
        }

        @Override
        public void close() {
            try {
                mInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public InputStream getBody() throws IOException {
            return mInputStream;
        }

        @Override
        public HttpHeaders getHeaders() {
            return mHttpHeaders;
        }
    }
}

package com.drugoogle.sellscrm.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;

/**
 * Created by Lei on 2014/12/29.
 */
public class MyResponseErrorHandler extends DefaultResponseErrorHandler {
    @Override
    protected boolean hasError(HttpStatus statusCode) {
        if (statusCode.value() == 403 || statusCode.value() == 400)
            return false;

        return super.hasError(statusCode);
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        if(mErrorListener != null)
            mErrorListener.onError(response.getStatusCode());

        super.handleError(response);
    }

    public interface ErrorListener
    {
        void onError(HttpStatus status);
    }

    private ErrorListener mErrorListener;
    public void setErrorListener(ErrorListener listener)
    {
        mErrorListener = listener;
    }
}

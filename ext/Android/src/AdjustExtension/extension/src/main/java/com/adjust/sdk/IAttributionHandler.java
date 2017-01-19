package com.adjust.sdk;

/**
 * Created by pfms on 15/12/14.
 */
public interface IAttributionHandler {
    void init(IActivityHandler activityHandler,
                     ActivityPackage attributionPackage,
                     boolean startsSending);

    void getAttribution();

    void checkSessionResponse(SessionResponseData responseData);

    void pauseSending();

    void resumeSending();

    void teardown();
}

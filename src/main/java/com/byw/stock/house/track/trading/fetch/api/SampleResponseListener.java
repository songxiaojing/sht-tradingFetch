package com.byw.stock.house.track.trading.fetch.api;


import com.byw.stock.house.platform.log.PlatformLogger;

public class SampleResponseListener implements IResponseListener {

    final private static PlatformLogger _logger = PlatformLogger.getLogger(SampleResponseListener.class);

    @Override
    public void fireDone(HttpClientReferent hcr, Object content) {

        _logger.info((String) content);
    }

    @Override
    public void fireException(HttpClientReferent hcr, Throwable cause) {

        _logger.exception(cause);

    }

}

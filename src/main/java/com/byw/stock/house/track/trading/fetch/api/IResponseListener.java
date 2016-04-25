package com.byw.stock.house.track.trading.fetch.api;



public interface IResponseListener {

    public void fireDone(HttpClientReferent hcr, Object content);

    public void fireException(HttpClientReferent hcr, Throwable cause);

}

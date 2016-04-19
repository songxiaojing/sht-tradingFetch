package com.byw.stock.house.track.api;



public interface IResponseListener {

    public void fireDone(HttpClientReferent hcr, Object content);

    public void fireException(HttpClientReferent hcr, Throwable cause);

}

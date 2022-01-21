package com.huobi.client;

import java.util.List;

import com.huobi.client.req.etf.ETFSwapListRequest;
import com.huobi.client.req.etf.ETFSwapRequest;
import com.huobi.model.etf.ETFConfig;
import com.huobi.model.etf.ETFSwapRecord;

public interface ETFClient {

    ETFConfig getConfig(String etfName) throws Exception;

    void etfSwap(ETFSwapRequest request) throws Exception;

    List<ETFSwapRecord> getEtfSwapList(ETFSwapListRequest request) throws Exception;

}

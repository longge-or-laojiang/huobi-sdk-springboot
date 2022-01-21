package com.huobi.client;

import com.huobi.client.req.algo.CancelAlgoOrderRequest;
import com.huobi.client.req.algo.CreateAlgoOrderRequest;
import com.huobi.client.req.algo.GetHistoryAlgoOrdersRequest;
import com.huobi.client.req.algo.GetOpenAlgoOrdersRequest;
import com.huobi.constant.Options;
import com.huobi.constant.enums.ExchangeEnum;
import com.huobi.exception.SDKException;
import com.huobi.model.algo.AlgoOrder;
import com.huobi.model.algo.CancelAlgoOrderResult;
import com.huobi.model.algo.CreateAlgoOrderResult;
import com.huobi.model.algo.GetHistoryAlgoOrdersResult;
import com.huobi.model.algo.GetOpenAlgoOrdersResult;
import com.huobi.service.huobi.HuobiAlgoService;

public interface AlgoClient {

    CreateAlgoOrderResult createAlgoOrder(CreateAlgoOrderRequest request) throws Exception;

    CancelAlgoOrderResult cancelAlgoOrder(CancelAlgoOrderRequest request) throws Exception;

    GetOpenAlgoOrdersResult getOpenAlgoOrders(GetOpenAlgoOrdersRequest request) throws Exception;

    GetHistoryAlgoOrdersResult getHistoryAlgoOrders(GetHistoryAlgoOrdersRequest request) throws Exception;

    AlgoOrder getAlgoOrdersSpecific(String clientOrderId) throws Exception;
}

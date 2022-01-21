package com.huobi.client;

import java.util.List;

import com.huobi.client.req.trade.BatchCancelOpenOrdersRequest;
import com.huobi.client.req.trade.CreateOrderRequest;
import com.huobi.client.req.trade.FeeRateRequest;
import com.huobi.client.req.trade.MatchResultRequest;
import com.huobi.client.req.trade.OpenOrdersRequest;
import com.huobi.client.req.trade.OrderHistoryRequest;
import com.huobi.client.req.trade.OrdersRequest;
import com.huobi.client.req.trade.ReqOrderListRequest;
import com.huobi.client.req.trade.SubOrderUpdateRequest;
import com.huobi.client.req.trade.SubOrderUpdateV2Request;
import com.huobi.client.req.trade.SubTradeClearingRequest;
import com.huobi.constant.Options;
import com.huobi.constant.enums.ExchangeEnum;
import com.huobi.exception.SDKException;
import com.huobi.model.trade.BatchCancelOpenOrdersResult;
import com.huobi.model.trade.BatchCancelOrderResult;
import com.huobi.model.trade.FeeRate;
import com.huobi.model.trade.MatchResult;
import com.huobi.model.trade.Order;
import com.huobi.model.trade.OrderDetailReq;
import com.huobi.model.trade.OrderListReq;
import com.huobi.model.trade.OrderUpdateEvent;
import com.huobi.model.trade.OrderUpdateV2Event;
import com.huobi.model.trade.TradeClearingEvent;
import com.huobi.service.huobi.HuobiTradeService;
import com.huobi.utils.ResponseCallback;

public interface TradeClient {

    Long createOrder(CreateOrderRequest request) throws Exception;

    Long cancelOrder(Long orderId) throws Exception;

    Integer cancelOrder(String clientOrderId) throws Exception;

    BatchCancelOpenOrdersResult batchCancelOpenOrders(BatchCancelOpenOrdersRequest request) throws Exception;

    BatchCancelOrderResult batchCancelOrder(List<Long> ids) throws Exception;

    List<Order> getOpenOrders(OpenOrdersRequest request) throws Exception;

    Order getOrder(Long orderId) throws Exception;

    Order getOrder(String clientOrderId) throws Exception;

    List<Order> getOrders(OrdersRequest request) throws Exception;

    List<Order> getOrdersHistory(OrderHistoryRequest request) throws Exception;

    List<MatchResult> getMatchResult(Long orderId) throws Exception;

    List<MatchResult> getMatchResults(MatchResultRequest request) throws Exception;

    List<FeeRate> getFeeRate(FeeRateRequest request) throws Exception;

    void subOrderUpdateV2(SubOrderUpdateV2Request request, ResponseCallback<OrderUpdateV2Event> callback) throws Exception;

    void subTradeClearing(SubTradeClearingRequest request, ResponseCallback<TradeClearingEvent> callback) throws Exception;
}

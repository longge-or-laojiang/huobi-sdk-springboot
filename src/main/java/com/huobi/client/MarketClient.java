package com.huobi.client;

import java.util.List;

import com.huobi.client.req.market.CandlestickRequest;
import com.huobi.client.req.market.MarketDepthRequest;
import com.huobi.client.req.market.MarketDetailMergedRequest;
import com.huobi.client.req.market.MarketDetailRequest;
import com.huobi.client.req.market.MarketHistoryTradeRequest;
import com.huobi.client.req.market.MarketTradeRequest;
import com.huobi.client.req.market.ReqCandlestickRequest;
import com.huobi.client.req.market.ReqMarketDepthRequest;
import com.huobi.client.req.market.ReqMarketDetailRequest;
import com.huobi.client.req.market.ReqMarketTradeRequest;
import com.huobi.client.req.market.SubCandlestickRequest;
import com.huobi.client.req.market.SubMarketBBORequest;
import com.huobi.client.req.market.SubMarketDepthRequest;
import com.huobi.client.req.market.SubMarketDetailRequest;
import com.huobi.client.req.market.SubMarketTradeRequest;
import com.huobi.client.req.market.SubMbpIncrementalUpdateRequest;
import com.huobi.client.req.market.SubMbpRefreshUpdateRequest;
import com.huobi.constant.Options;
import com.huobi.constant.enums.ExchangeEnum;
import com.huobi.exception.SDKException;
import com.huobi.model.market.Candlestick;
import com.huobi.model.market.CandlestickEvent;
import com.huobi.model.market.CandlestickReq;
import com.huobi.model.market.MarketBBOEvent;
import com.huobi.model.market.MarketDepth;
import com.huobi.model.market.MarketDepthEvent;
import com.huobi.model.market.MarketDepthReq;
import com.huobi.model.market.MarketDetail;
import com.huobi.model.market.MarketDetailEvent;
import com.huobi.model.market.MarketDetailMerged;
import com.huobi.model.market.MarketDetailReq;
import com.huobi.model.market.MarketTicker;
import com.huobi.model.market.MarketTrade;
import com.huobi.model.market.MarketTradeEvent;
import com.huobi.model.market.MarketTradeReq;
import com.huobi.model.market.MbpIncrementalUpdateEvent;
import com.huobi.model.market.MbpRefreshUpdateEvent;
import com.huobi.service.huobi.HuobiMarketService;
import com.huobi.service.huobi.connection.HuobiWebSocketConnection;
import com.huobi.utils.ResponseCallback;
import com.huobi.utils.WebSocketConnection;

public interface MarketClient {

    List<Candlestick> getCandlestick(CandlestickRequest request) throws Exception;

    MarketDetailMerged getMarketDetailMerged(MarketDetailMergedRequest request) throws Exception;

    MarketDetail getMarketDetail(MarketDetailRequest request) throws Exception;

    List<MarketTicker> getTickers() throws Exception;

    MarketDepth getMarketDepth(MarketDepthRequest request) throws Exception;

    List<MarketTrade> getMarketTrade(MarketTradeRequest request) throws Exception;

    List<MarketTrade> getMarketHistoryTrade(MarketHistoryTradeRequest request) throws Exception;

    void subCandlestick(SubCandlestickRequest request, ResponseCallback<CandlestickEvent> callback) throws Exception;

    void subMarketDetail(SubMarketDetailRequest request, ResponseCallback<MarketDetailEvent> callback) throws Exception;

    void subMarketDepth(SubMarketDepthRequest request, ResponseCallback<MarketDepthEvent> callback) throws Exception;

    void subMarketTrade(SubMarketTradeRequest request, ResponseCallback<MarketTradeEvent> callback) throws Exception;

    void subMarketBBO(SubMarketBBORequest request, ResponseCallback<MarketBBOEvent> callback) throws Exception;

    void subMbpRefreshUpdate(SubMbpRefreshUpdateRequest request, ResponseCallback<MbpRefreshUpdateEvent> callback) throws Exception;

    WebSocketConnection subMbpIncrementalUpdate(SubMbpIncrementalUpdateRequest request,
                                                ResponseCallback<MbpIncrementalUpdateEvent> callback) throws Exception;

    WebSocketConnection reqMbpIncrementalUpdate(SubMbpIncrementalUpdateRequest request,
                                                WebSocketConnection connection) throws Exception;

    void reqCandlestick(ReqCandlestickRequest request, ResponseCallback<CandlestickReq> callback) throws Exception;

    void reqMarketDepth(ReqMarketDepthRequest request, ResponseCallback<MarketDepthReq> callback) throws Exception;

    void reqMarketTrade(ReqMarketTradeRequest request, ResponseCallback<MarketTradeReq> callback) throws Exception;

    void reqMarketDetail(ReqMarketDetailRequest request, ResponseCallback<MarketDetailReq> callback) throws Exception;

}

package com.huobi.service.huobi;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.huobi.client.MarketClient;
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
import com.huobi.constant.HuobiConstants;
import com.huobi.constant.enums.DepthLevels;
import com.huobi.constant.enums.DepthSizeEnum;
import com.huobi.constant.enums.DepthStepEnum;
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
import com.huobi.service.huobi.connection.HuobiRestConnection;
import com.huobi.service.huobi.connection.HuobiWebSocketConnection;
import com.huobi.service.huobi.parser.market.CandlestickEventParser;
import com.huobi.service.huobi.parser.market.CandlestickParser;
import com.huobi.service.huobi.parser.market.CandlestickReqParser;
import com.huobi.service.huobi.parser.market.MarketBBOEventParser;
import com.huobi.service.huobi.parser.market.MarketDepthEventParser;
import com.huobi.service.huobi.parser.market.MarketDepthParser;
import com.huobi.service.huobi.parser.market.MarketDepthReqParser;
import com.huobi.service.huobi.parser.market.MarketDetailEventParser;
import com.huobi.service.huobi.parser.market.MarketDetailMergedParser;
import com.huobi.service.huobi.parser.market.MarketDetailParser;
import com.huobi.service.huobi.parser.market.MarketDetailReqParser;
import com.huobi.service.huobi.parser.market.MarketTickerParser;
import com.huobi.service.huobi.parser.market.MarketTradeEventParser;
import com.huobi.service.huobi.parser.market.MarketTradeParser;
import com.huobi.service.huobi.parser.market.MarketTradeReqParser;
import com.huobi.service.huobi.parser.market.MbpIncrementalUpdateEventParser;
import com.huobi.service.huobi.parser.market.MbpRefreshUpdateEventParser;
import com.huobi.service.huobi.signature.UrlParamsBuilder;
import com.huobi.utils.InputChecker;
import com.huobi.utils.ResponseCallback;
import com.huobi.utils.SymbolUtils;
import com.huobi.utils.WebSocketConnection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Slf4j
@Lazy
@Service
public class HuobiMarketService implements MarketClient {

    @Autowired
    private HuobiRestConnection restConnection;

    @Autowired
    private HuobiConstants huobiConstants;

    @Override
    public List<Candlestick> getCandlestick(CandlestickRequest request) throws Exception {

        // ????????????
        InputChecker.checker()
                .checkSymbol(request.getSymbol())
                .checkRange(request.getSize(), 1, 2000, "size")
                .shouldNotNull(request.getInterval(), "CandlestickInterval");

        // ????????????
        UrlParamsBuilder paramBuilder = UrlParamsBuilder.build()
                .putToUrl("symbol", request.getSymbol())
                .putToUrl("period", request.getInterval().getCode())
                .putToUrl("size", request.getSize());

        JSONObject json = restConnection.executeGet(HuobiMarketEnum.REST_CANDLESTICK_PATH.getCode(), paramBuilder);
        JSONArray data = json.getJSONArray("data");
        return new CandlestickParser().parseArray(data);
    }

    @Override
    public void subCandlestick(SubCandlestickRequest request, ResponseCallback<CandlestickEvent> callback) throws Exception {

        // ????????????
        InputChecker.checker()
                .shouldNotNull(request.getSymbol(), "symbol")
                .shouldNotNull(request.getInterval(), "interval");
        // ?????????symbol?????????
        List<String> symbolList = SymbolUtils.parseSymbols(request.getSymbol());

        // ????????????
        InputChecker.checker()
                .checkSymbolList(symbolList);

        List<String> commandList = new ArrayList<>(symbolList.size());
        symbolList.forEach(symbol -> {

            String topic = HuobiMarketEnum.WEBSOCKET_CANDLESTICK_TOPIC.getCode()
                    .replace("$symbol$", symbol)
                    .replace("$period$", request.getInterval().getCode());

            JSONObject command = new JSONObject();
            command.put("sub", topic);
            command.put("id", System.nanoTime());
            commandList.add(command.toJSONString());
        });

        HuobiWebSocketConnection.createMarketConnection(restConnection.getOptions(), commandList,
                new CandlestickEventParser(), callback,
                false);
    }

    @Override
    public MarketDetailMerged getMarketDetailMerged(MarketDetailMergedRequest request) throws Exception {

        // ????????????
        InputChecker.checker()
                .shouldNotNull(request.getSymbol(), "symbol");

        // ????????????
        UrlParamsBuilder paramBuilder = UrlParamsBuilder.build()
                .putToUrl("symbol", request.getSymbol());

        JSONObject json = restConnection.executeGet(HuobiMarketEnum.REST_MARKET_DETAIL_MERGED_PATH.getCode(),
                paramBuilder);
        JSONObject data = json.getJSONObject("tick");
        return new MarketDetailMergedParser().parse(data);
    }

    @Override
    public MarketDetail getMarketDetail(MarketDetailRequest request) throws Exception {

        // ????????????
        InputChecker.checker()
                .shouldNotNull(request.getSymbol(), "symbol");

        // ????????????
        UrlParamsBuilder paramBuilder = UrlParamsBuilder.build()
                .putToUrl("symbol", request.getSymbol());

        JSONObject json = restConnection.executeGet(HuobiMarketEnum.REST_MARKET_DETAIL_PATH.getCode(), paramBuilder);
        JSONObject data = json.getJSONObject("tick");
        return new MarketDetailParser().parse(data);
    }

    @Override
    public void subMarketDetail(SubMarketDetailRequest request, ResponseCallback<MarketDetailEvent> callback) throws Exception {
        // ????????????
        InputChecker.checker()
                .shouldNotNull(request.getSymbol(), "symbol");

        // ?????????symbol?????????
        List<String> symbolList = SymbolUtils.parseSymbols(request.getSymbol());

        // ????????????
        InputChecker.checker()
                .checkSymbolList(symbolList);

        List<String> commandList = new ArrayList<>(symbolList.size());
        symbolList.forEach(symbol -> {

            String topic = HuobiMarketEnum.WEBSOCKET_MARKET_DETAIL_TOPIC.getCode()
                    .replace("$symbol", symbol);

            JSONObject command = new JSONObject();
            command.put("sub", topic);
            command.put("id", System.nanoTime());
            commandList.add(command.toJSONString());
        });

        HuobiWebSocketConnection.createMarketConnection(restConnection.getOptions(), commandList,
                new MarketDetailEventParser(), callback
                , false);
    }

    @Override
    public List<MarketTicker> getTickers() throws Exception {

        JSONObject json = restConnection.executeGet(HuobiMarketEnum.REST_MARKET_TICKERS_PATH.getCode(),
                UrlParamsBuilder.build());
        JSONArray data = json.getJSONArray("data");
        return new MarketTickerParser().parseArray(data);
    }

    @Override
    public MarketDepth getMarketDepth(MarketDepthRequest request) throws Exception {

        // ????????????
        InputChecker.checker()
                .checkSymbol(request.getSymbol())
                .shouldNotNull(request.getStep(), "step");

        int size = request.getDepth() == null ? DepthSizeEnum.SIZE_20.getSize() : request.getDepth().getSize();

        // ????????????
        UrlParamsBuilder paramBuilder = UrlParamsBuilder.build()
                .putToUrl("symbol", request.getSymbol())
                .putToUrl("depth", size)
                .putToUrl("type", request.getStep().getStep());

        JSONObject json = restConnection.executeGet(HuobiMarketEnum.REST_MARKET_DEPTH_PATH.getCode(), paramBuilder);
        JSONObject data = json.getJSONObject("tick");
        return new MarketDepthParser().parse(data);
    }

    @Override
    public void subMarketDepth(SubMarketDepthRequest request, ResponseCallback<MarketDepthEvent> callback) throws Exception {
        // ????????????
        InputChecker.checker()
                .shouldNotNull(request.getSymbol(), "symbol");

        // ?????????symbol?????????
        List<String> symbolList = SymbolUtils.parseSymbols(request.getSymbol());

        // ????????????
        InputChecker.checker()
                .checkSymbolList(symbolList);

        String step = request.getStep() == null ? DepthStepEnum.STEP0.getStep() : request.getStep().getStep();
        List<String> commandList = new ArrayList<>(symbolList.size());
        symbolList.forEach(symbol -> {

            String topic = HuobiMarketEnum.WEBSOCKET_MARKET_DEPTH_TOPIC.getCode()
                    .replace("$symbol", symbol)
                    .replace("$type", step);

            JSONObject command = new JSONObject();
            command.put("sub", topic);
            command.put("id", System.nanoTime());
            commandList.add(command.toJSONString());
        });

        HuobiWebSocketConnection.createMarketConnection(restConnection.getOptions(), commandList,
                new MarketDepthEventParser(), callback,
                false);
    }

    @Override
    public List<MarketTrade> getMarketTrade(MarketTradeRequest request) throws Exception {
        // ????????????
        InputChecker.checker()
                .checkSymbol(request.getSymbol());

        // ????????????
        UrlParamsBuilder paramBuilder = UrlParamsBuilder.build()
                .putToUrl("symbol", request.getSymbol());

        JSONObject json = restConnection.executeGet(HuobiMarketEnum.REST_MARKET_TRADE_PATH.getCode(), paramBuilder);
        JSONArray data = json.getJSONObject("tick").getJSONArray("data");
        return new MarketTradeParser().parseArray(data);
    }

    @Override
    public void subMarketTrade(SubMarketTradeRequest request, ResponseCallback<MarketTradeEvent> callback) throws Exception {

        // ????????????
        InputChecker.checker()
                .shouldNotNull(request.getSymbol(), "symbol");

        // ?????????symbol?????????
        List<String> symbolList = SymbolUtils.parseSymbols(request.getSymbol());

        // ????????????
        InputChecker.checker()
                .checkSymbolList(symbolList);

        List<String> commandList = new ArrayList<>(symbolList.size());
        symbolList.forEach(symbol -> {

            String topic = HuobiMarketEnum.WEBSOCKET_MARKET_TRADE_TOPIC.getCode()
                    .replace("$symbol", symbol);

            JSONObject command = new JSONObject();
            command.put("sub", topic);
            command.put("id", System.nanoTime());
            commandList.add(command.toJSONString());
        });

        HuobiWebSocketConnection.createMarketConnection(restConnection.getOptions(), commandList,
                new MarketTradeEventParser(), callback,
                false);
    }

    @Override
    public List<MarketTrade> getMarketHistoryTrade(MarketHistoryTradeRequest request) throws Exception {
        // ????????????
        InputChecker.checker()
                .checkSymbol(request.getSymbol());

        int size = request.getSize() == null ? 2000 : request.getSize();

        // ????????????
        UrlParamsBuilder paramBuilder = UrlParamsBuilder.build()
                .putToUrl("symbol", request.getSymbol())
                .putToUrl("size", size);

        JSONObject json = restConnection.executeGet(HuobiMarketEnum.REST_MARKET_HISTORY_TRADE_PATH.getCode(),
                paramBuilder);
        JSONArray jsonArray = json.getJSONArray("data");
        if (jsonArray == null || jsonArray.size() <= 0) {
            return new ArrayList<>();
        }

        // ????????????
        List<MarketTrade> resList = new ArrayList<>();
        MarketTradeParser parser = new MarketTradeParser();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject data = jsonArray.getJSONObject(i);
            JSONArray dataArray = data.getJSONArray("data");
            List<MarketTrade> dataList = parser.parseArray(dataArray);
            if (dataList != null && dataList.size() > 0) {
                resList.addAll(dataList);
            }
        }
        return resList;
    }

    @Override
    public void subMarketBBO(SubMarketBBORequest request, ResponseCallback<MarketBBOEvent> callback) throws Exception {

        // ????????????
        InputChecker.checker()
                .shouldNotNull(request.getSymbol(), "symbol");

        // ?????????symbol?????????
        List<String> symbolList = SymbolUtils.parseSymbols(request.getSymbol());

        // ????????????
        InputChecker.checker()
                .checkSymbolList(symbolList);

        List<String> commandList = new ArrayList<>(symbolList.size());
        symbolList.forEach(symbol -> {

            String topic = HuobiMarketEnum.WEBSOCKET_MARKET_BBO_TOPIC.getCode()
                    .replace("$symbol", symbol);

            JSONObject command = new JSONObject();
            command.put("sub", topic);
            command.put("id", System.nanoTime());
            commandList.add(command.toJSONString());
        });

        HuobiWebSocketConnection.createMarketConnection(restConnection.getOptions(), commandList,
                new MarketBBOEventParser(), callback,
                false);

    }

    public void subMbpRefreshUpdate(SubMbpRefreshUpdateRequest request,
                                    ResponseCallback<MbpRefreshUpdateEvent> callback) throws Exception {

        // ????????????
        InputChecker.checker()
                .shouldNotNull(request.getSymbols(), "symbols");

        // ?????????symbol?????????
        List<String> symbolList = SymbolUtils.parseSymbols(request.getSymbols());

        // ????????????
        InputChecker.checker()
                .checkSymbolList(symbolList);

        int level = request.getLevels() == null ? DepthLevels.LEVEL_20.getLevel() : request.getLevels().getLevel();
        if (level >= DepthLevels.LEVEL_150.getLevel()) {
            throw new SDKException(SDKException.INPUT_ERROR, " Unsupport Levels : " + request.getLevels());
        }
        List<String> commandList = new ArrayList<>(symbolList.size());
        symbolList.forEach(symbol -> {

            String topic = HuobiMarketEnum.WEBSOCKET_MARKET_MBP_REFRESH_TOPIC.getCode()
                    .replace("$symbol", symbol)
                    .replace("$levels", level + "");

            JSONObject command = new JSONObject();
            command.put("sub", topic);
            command.put("id", System.nanoTime());
            commandList.add(command.toJSONString());
        });

        HuobiWebSocketConnection.createMarketConnection(restConnection.getOptions(), commandList,
                new MbpRefreshUpdateEventParser(),
                callback, false);
    }

    public WebSocketConnection subMbpIncrementalUpdate(SubMbpIncrementalUpdateRequest request,
                                                       ResponseCallback<MbpIncrementalUpdateEvent> callback) throws Exception {

        // ????????????
        InputChecker.checker()
                .checkSymbol(request.getSymbol());

        int level = request.getLevels() == null ? DepthLevels.LEVEL_150.getLevel() : request.getLevels().getLevel();
        List<String> commandList = new ArrayList<>(1);

        String topic = HuobiMarketEnum.WEBSOCKET_MARKET_MBP_INCREMENT_TOPIC.getCode()
                .replace("$symbol", request.getSymbol())
                .replace("$levels", level + "");

        JSONObject command = new JSONObject();
        command.put("sub", topic);
        command.put("id", System.nanoTime());
        commandList.add(command.toJSONString());

        return HuobiWebSocketConnection.createMarketConnection(restConnection.getOptions(), commandList,
                new MbpIncrementalUpdateEventParser(), callback, false);
    }

    public WebSocketConnection reqMbpIncrementalUpdate(SubMbpIncrementalUpdateRequest request,
                                                       WebSocketConnection connection) throws Exception {

        // ????????????
        InputChecker.checker()
                .checkSymbol(request.getSymbol());

        int level = request.getLevels() == null ? DepthLevels.LEVEL_150.getLevel() : request.getLevels().getLevel();
        if (level != DepthLevels.LEVEL_150.getLevel()) {
            throw new SDKException(SDKException.INPUT_ERROR, " Unsupport Levels : " + request.getLevels() + " " +
                    "incremental update only support level_150");
        }
        List<String> commandList = new ArrayList<>(1);

        String topic = HuobiMarketEnum.WEBSOCKET_MARKET_MBP_INCREMENT_TOPIC.getCode()
                .replace("$symbol", request.getSymbol())
                .replace("$levels", level + "");

        JSONObject command = new JSONObject();
        command.put("req", topic);
        command.put("id", System.nanoTime());

        connection.send(command.toJSONString());
        return connection;
    }

    public void reqCandlestick(ReqCandlestickRequest request, ResponseCallback<CandlestickReq> callback) throws Exception {

        // ????????????
        InputChecker.checker()
                .shouldNotNull(request.getSymbol(), "symbol")
                .shouldNotNull(request.getInterval(), "interval");

        String topic = HuobiMarketEnum.WEBSOCKET_CANDLESTICK_TOPIC.getCode()
                .replace("$symbol$", request.getSymbol())
                .replace("$period$", request.getInterval().getCode());

        JSONObject command = new JSONObject();
        command.put(huobiConstants.getOpReq(), topic);
        command.put("id", System.nanoTime());
        if (request.getFrom() != null) {
            command.put("from", request.getFrom());
        }
        if (request.getTo() != null) {
            command.put("to", request.getTo());
        }
        List<String> commandList = new ArrayList<>(1);
        commandList.add(command.toJSONString());

        HuobiWebSocketConnection.createMarketConnection(restConnection.getOptions(), commandList,
                new CandlestickReqParser(), callback,
                true);
    }

    public void reqMarketDepth(ReqMarketDepthRequest request, ResponseCallback<MarketDepthReq> callback) throws Exception {
        // ????????????
        InputChecker.checker()
                .shouldNotNull(request.getSymbol(), "symbol")
                .shouldNotNull(request.getStep(), "step");

        String topic = HuobiMarketEnum.WEBSOCKET_MARKET_DEPTH_TOPIC.getCode()
                .replace("$symbol", request.getSymbol())
                .replace("$type", request.getStep().getStep());

        JSONObject command = new JSONObject();
        command.put(huobiConstants.getOpReq(), topic);
        command.put("id", System.nanoTime());

        List<String> commandList = new ArrayList<>(1);
        commandList.add(command.toJSONString());
        HuobiWebSocketConnection.createMarketConnection(restConnection.getOptions(), commandList,
                new MarketDepthReqParser(), callback,
                true);

    }

    public void reqMarketTrade(ReqMarketTradeRequest request, ResponseCallback<MarketTradeReq> callback) throws Exception {
        // ????????????
        InputChecker.checker()
                .shouldNotNull(request.getSymbol(), "symbol");

        String topic = HuobiMarketEnum.WEBSOCKET_MARKET_TRADE_TOPIC.getCode()
                .replace("$symbol", request.getSymbol());

        JSONObject command = new JSONObject();
        command.put(huobiConstants.getOpReq(), topic);
        command.put("id", System.nanoTime());

        List<String> commandList = new ArrayList<>(1);
        commandList.add(command.toJSONString());
        HuobiWebSocketConnection.createMarketConnection(restConnection.getOptions(), commandList,
                new MarketTradeReqParser(), callback,
                true);
    }

    public void reqMarketDetail(ReqMarketDetailRequest request, ResponseCallback<MarketDetailReq> callback) throws Exception {
        // ????????????
        InputChecker.checker()
                .shouldNotNull(request.getSymbol(), "symbol");

        String topic = HuobiMarketEnum.WEBSOCKET_MARKET_DETAIL_TOPIC.getCode()
                .replace("$symbol", request.getSymbol());

        JSONObject command = new JSONObject();
        command.put(huobiConstants.getOpReq(), topic);
        command.put("id", System.nanoTime());

        List<String> commandList = new ArrayList<>(1);
        commandList.add(command.toJSONString());
        HuobiWebSocketConnection.createMarketConnection(restConnection.getOptions(), commandList,
                new MarketDetailReqParser(), callback,
                true);
    }


    @Getter
    @AllArgsConstructor
    enum HuobiMarketEnum {

        REST_CANDLESTICK_PATH("?????????????????????K????????????K????????????????????????????????????????????????????????????K????????????????????????????????????0???????????????????????????0???", "/market/history/kline"),

        REST_MARKET_DETAIL_MERGED_PATH("???????????????ticker????????????????????????24???????????????????????????", "/market/detail/merged"),

        REST_MARKET_DETAIL_PATH("?????????????????????24???????????????????????????", "/market/detail"),

        REST_MARKET_TICKERS_PATH("?????????????????????????????? tickers", "/market/tickers"),

        REST_MARKET_DEPTH_PATH("?????????????????????????????????????????????????????????", "/market/depth"),

        REST_MARKET_TRADE_PATH("?????????????????????????????????????????????????????????", "/market/trade"),

        REST_MARKET_HISTORY_TRADE_PATH("?????????????????????????????????????????????????????????", "/market/history/trade"),

        WEBSOCKET_CANDLESTICK_TOPIC("??????K??????????????????Websocket?????????????????????????????????????????????????????????", "market.$symbol$.kline.$period$"),

        WEBSOCKET_MARKET_DETAIL_TOPIC("???????????????24???????????????????????????????????????????????????????????????10???", "market.$symbol.detail"),

        WEBSOCKET_MARKET_DEPTH_TOPIC("???????????????????????????????????????????????????????????????1???", "market.$symbol.depth.$type"),

        WEBSOCKET_MARKET_TRADE_TOPIC("?????????????????????????????????????????????", "market.$symbol.trade.detail"),

        WEBSOCKET_MARKET_BBO_TOPIC("??????????????????????????????????????????????????????????????????????????????????????????????????????????????????", "market.$symbol.bbo"),

        WEBSOCKET_MARKET_MBP_REFRESH_TOPIC("???????????????????????????????????????????????????Market By Price (MBP) ?????????????????????????????????????????????100????????????", "market" +
                ".$symbol.mbp.refresh.$levels"),

        WEBSOCKET_MARKET_MBP_INCREMENT_TOPIC("?????????????????????", "market.$symbol.mbp.$levels");

        private String mess;
        private String code;
    }
}

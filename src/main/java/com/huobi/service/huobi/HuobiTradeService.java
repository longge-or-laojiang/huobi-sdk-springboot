package com.huobi.service.huobi;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.huobi.client.TradeClient;
import com.huobi.client.req.trade.BatchCancelOpenOrdersRequest;
import com.huobi.client.req.trade.CreateOrderRequest;
import com.huobi.client.req.trade.FeeRateRequest;
import com.huobi.client.req.trade.MatchResultRequest;
import com.huobi.client.req.trade.OpenOrdersRequest;
import com.huobi.client.req.trade.OrderHistoryRequest;
import com.huobi.client.req.trade.OrdersRequest;
import com.huobi.client.req.trade.SubOrderUpdateV2Request;
import com.huobi.client.req.trade.SubTradeClearingRequest;
import com.huobi.constant.HuobiConstants;
import com.huobi.constant.enums.OrderTypeEnum;
import com.huobi.model.trade.BatchCancelOpenOrdersResult;
import com.huobi.model.trade.BatchCancelOrderResult;
import com.huobi.model.trade.FeeRate;
import com.huobi.model.trade.MatchResult;
import com.huobi.model.trade.Order;
import com.huobi.model.trade.OrderUpdateV2Event;
import com.huobi.model.trade.TradeClearingEvent;
import com.huobi.service.huobi.connection.HuobiRestConnection;
import com.huobi.service.huobi.connection.HuobiWebSocketConnection;
import com.huobi.service.huobi.parser.trade.BatchCancelOpenOrdersResultParser;
import com.huobi.service.huobi.parser.trade.BatchCancelOrderResultParser;
import com.huobi.service.huobi.parser.trade.FeeRateParser;
import com.huobi.service.huobi.parser.trade.MatchResultParser;
import com.huobi.service.huobi.parser.trade.OrderParser;
import com.huobi.service.huobi.parser.trade.OrderUpdateEventV2Parser;
import com.huobi.service.huobi.parser.trade.TradeClearingEventParser;
import com.huobi.service.huobi.signature.UrlParamsBuilder;
import com.huobi.utils.InputChecker;
import com.huobi.utils.ResponseCallback;
import com.huobi.utils.SymbolUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Lazy
@Service
public class HuobiTradeService implements TradeClient {

    @Autowired
    private HuobiRestConnection restConnection;
    @Autowired
    private HuobiConstants huobiConstants;

    @Override
    public Long createOrder(CreateOrderRequest request) throws Exception {

        InputChecker.checker().checkSymbol(request.getSymbol())
                .shouldNotNull(request.getAccountId(), "Account-Id")
                .shouldNotNull(request.getAmount(), "Amount")
                .shouldNotNull(request.getType(), "Type")
                .shouldNotNull(request.getOrderSource(), "order source");

        if (request.getType() == OrderTypeEnum.SELL_LIMIT
                || request.getType() == OrderTypeEnum.BUY_LIMIT
                || request.getType() == OrderTypeEnum.BUY_LIMIT_MAKER
                || request.getType() == OrderTypeEnum.SELL_LIMIT_MAKER) {
            InputChecker.checker()
                    .shouldNotNull(request.getPrice(), "Price");
        }
        if (request.getType() == OrderTypeEnum.SELL_MARKET
                || request.getType() == OrderTypeEnum.BUY_MARKET) {
            InputChecker.checker()
                    .shouldNull(request.getPrice(), "Price");
        }
        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToPost("account-id", request.getAccountId())
                .putToPost("amount", request.getAmount())
                .putToPost("price", request.getPrice())
                .putToPost("symbol", request.getSymbol())
                .putToPost("type", request.getType().getCode())
                .putToPost("client-order-id", request.getClientOrderId())
                .putToPost("source", request.getOrderSource().getCode())
                .putToPost("stop-price", request.getStopPrice())
                .putToPost("operator", request.getOperator() == null ? null : request.getOperator().getOperator());

        JSONObject jsonObject = restConnection.executePostWithSignature(HuobiTradeEnum.CREATE_ORDER_PATH.getCode(),
                builder);
        return jsonObject.getLong("data");
    }

    @Override
    public Long cancelOrder(Long orderId) throws Exception {

        InputChecker.checker()
                .shouldNotNull(orderId, "order-id");

        String path = HuobiTradeEnum.CANCEL_ORDER_PATH.getCode().replace("{order-id}", orderId + "");

        JSONObject jsonObject = restConnection.executePostWithSignature(path, UrlParamsBuilder.build());
        return jsonObject.getLong("data");
    }

    @Override
    public Integer cancelOrder(String clientOrderId) throws Exception {

        InputChecker.checker()
                .shouldNotNull(clientOrderId, "client-order-id");

        UrlParamsBuilder builder = UrlParamsBuilder.build();
        builder.putToPost("client-order-id", clientOrderId);

        JSONObject jsonObject =
                restConnection.executePostWithSignature(HuobiTradeEnum.CANCEL_ORDER_BY_CLIENT_ORDER_ID_PATH.getCode()
                        , builder);
        return jsonObject.getInteger("data");
    }

    @Override
    public BatchCancelOpenOrdersResult batchCancelOpenOrders(BatchCancelOpenOrdersRequest request) throws Exception {

        InputChecker.checker()
                .shouldNotNull(request.getAccountId(), "account-id");
        if (request.getSize() != null) {
            InputChecker.checker()
                    .checkRange(request.getSize(), 1, 100, "size");
        }

        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToPost("account-id", request.getAccountId())
                .putToPost("symbol", request.getSymbol())
                .putToPost("side", request.getSide() == null ? null : request.getSide().getCode())
                .putToPost("size", request.getSize());

        JSONObject jsonObject =
                restConnection.executePostWithSignature(HuobiTradeEnum.BATCH_CANCEL_OPEN_ORDERS_PATH.getCode(),
                        builder);
        JSONObject data = jsonObject.getJSONObject("data");
        return new BatchCancelOpenOrdersResultParser().parse(data);
    }

    @Override
    public BatchCancelOrderResult batchCancelOrder(List<Long> ids) throws Exception {

        InputChecker.checker()
                .shouldNotNull(ids, "order-ids")
                .checkList(ids, 1, 50, "orderIds");

        List<String> stringList = new LinkedList<>();
        for (Object obj : ids) {
            stringList.add(obj.toString());
        }
        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToPost("order-ids", stringList);

        JSONObject jsonObject =
                restConnection.executePostWithSignature(HuobiTradeEnum.BATCH_CANCEL_ORDERS_PATH.getCode(), builder);
        JSONObject data = jsonObject.getJSONObject("data");
        return new BatchCancelOrderResultParser().parse(data);
    }

    @Override
    public List<Order> getOpenOrders(OpenOrdersRequest request) throws Exception {

        InputChecker.checker()
                .checkSymbol(request.getSymbol())
                .shouldNotNull(request.getAccountId(), "account-id")
                .checkRange(request.getSize(), 1, 500, "size");


        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("account-id", request.getAccountId())
                .putToUrl("symbol", request.getSymbol())
                .putToUrl("side", request.getSide() == null ? null : request.getSide().getCode())
                .putToUrl("size", request.getSize())
                .putToUrl("direct", request.getDirect() == null ? null : request.getDirect().getCode())
                .putToUrl("from", request.getFrom());

        JSONObject jsonObject = restConnection.executeGetWithSignature(HuobiTradeEnum.GET_OPEN_ORDERS_PATH.getCode(),
                builder);
        JSONArray data = jsonObject.getJSONArray("data");
        return new OrderParser().parseArray(data);
    }

    @Override
    public Order getOrder(Long orderId) throws Exception {

        InputChecker.checker()
                .shouldNotNull(orderId, "order-id");

        String path = HuobiTradeEnum.GET_ORDER_PATH.getCode().replace("{order-id}", orderId + "");

        JSONObject jsonObject = restConnection.executeGetWithSignature(path, UrlParamsBuilder.build());
        JSONObject data = jsonObject.getJSONObject("data");
        return new OrderParser().parse(data);
    }

    @Override
    public Order getOrder(String clientOrderId) throws Exception {

        InputChecker.checker()
                .shouldNotNull(clientOrderId, "client-order-id");

        UrlParamsBuilder builder = UrlParamsBuilder.build();
        builder.putToUrl("clientOrderId", clientOrderId);

        JSONObject jsonObject =
                restConnection.executeGetWithSignature(HuobiTradeEnum.GET_ORDER_BY_CLIENT_ORDER_ID_PATH.getCode(),
                        builder);
        JSONObject data = jsonObject.getJSONObject("data");
        return new OrderParser().parse(data);
    }

    @Override
    public List<Order> getOrders(OrdersRequest request) throws Exception {

        InputChecker.checker()
                .shouldNotNull(request.getSymbol(), "symbol")
                .checkList(request.getStates(), 1, 100, "states");

        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("symbol", request.getSymbol())
                .putToUrl("types", request.getTypesString())
                .putToUrl("start-date", request.getStartDate(), "yyyy-MM-dd")
                .putToUrl("end-date", request.getEndDate(), "yyyy-MM-dd")
                .putToUrl("from", request.getStartId())
                .putToUrl("states", request.getStatesString())
                .putToUrl("size", request.getSize())
                .putToUrl("direct", request.getDirect() == null ? null : request.getDirect().getCode());

        JSONObject jsonObject = restConnection.executeGetWithSignature(HuobiTradeEnum.GET_ORDERS_PATH.getCode(),
                builder);
        JSONArray array = jsonObject.getJSONArray("data");
        return new OrderParser().parseArray(array);
    }

    @Override
    public List<Order> getOrdersHistory(OrderHistoryRequest request) throws Exception {

        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("symbol", request.getSymbol())
                .putToUrl("start-time", request.getStartTime())
                .putToUrl("end-time", request.getEndTime())
                .putToUrl("size", request.getSize())
                .putToUrl("direct", request.getDirection() == null ? null : request.getDirection().getCode());

        JSONObject jsonObject =
                restConnection.executeGetWithSignature(HuobiTradeEnum.GET_ORDERS_HISTORY_PATH.getCode(), builder);
        JSONArray array = jsonObject.getJSONArray("data");
        return new OrderParser().parseArray(array);
    }

    @Override
    public List<MatchResult> getMatchResult(Long orderId) throws Exception {

        InputChecker.checker()
                .shouldNotNull(orderId, "order-id");

        String path = HuobiTradeEnum.GET_SINGLE_ORDER_MATCH_RESULT_PATH.getCode().replace("{order-id}", orderId + "");

        JSONObject jsonObject = restConnection.executeGetWithSignature(path, UrlParamsBuilder.build());
        JSONArray array = jsonObject.getJSONArray("data");
        return new MatchResultParser().parseArray(array);
    }

    @Override
    public List<MatchResult> getMatchResults(MatchResultRequest request) throws Exception {

        InputChecker.checker()
                .shouldNotNull(request.getSymbol(), "symbol");

        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("symbol", request.getSymbol())
                .putToUrl("types", request.getTypeString())
                .putToUrl("start-date", request.getStartDate(), "yyyy-MM-dd")
                .putToUrl("end-date", request.getEndDate(), "yyyy-MM-dd")
                .putToUrl("from", request.getFrom())
                .putToUrl("direct", request.getDirection() == null ? null : request.getDirection().getCode())
                .putToUrl("size", request.getSize());

        JSONObject jsonObject = restConnection.executeGetWithSignature(HuobiTradeEnum.GET_MATCH_RESULT_PATH.getCode()
                , builder);
        JSONArray array = jsonObject.getJSONArray("data");
        return new MatchResultParser().parseArray(array);
    }

    @Override
    public List<FeeRate> getFeeRate(FeeRateRequest request) throws Exception {

        InputChecker.checker()
                .shouldNotNull(request.getSymbols(), "symbol");

        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("symbols", request.getSymbols());

        JSONObject jsonObject = restConnection.executeGetWithSignature(HuobiTradeEnum.GET_FEE_RATE_PATH.getCode(),
                builder);
        JSONArray array = jsonObject.getJSONArray("data");
        return new FeeRateParser().parseArray(array);
    }


    public void subOrderUpdateV2(SubOrderUpdateV2Request request, ResponseCallback<OrderUpdateV2Event> callback) throws Exception {
        // ????????????
        InputChecker.checker()
                .shouldNotNull(request.getSymbols(), "symbols");

        // ?????????symbol?????????
        List<String> symbolList = SymbolUtils.parseSymbols(request.getSymbols());

        // ????????????
        InputChecker.checker().checkSymbolList(symbolList);

        List<String> commandList = new ArrayList<>(symbolList.size());
        symbolList.forEach(symbol -> {

            String topic = HuobiTradeEnum.WEBSOCKET_ORDER_UPDATE_V2_TOPIC.getCode()
                    .replace("${symbol}", symbol);

            JSONObject command = new JSONObject();
            command.put("action", huobiConstants.getActionSub());
            command.put("ch", topic);
            command.put("id", System.nanoTime());
            commandList.add(command.toJSONString());
        });

        HuobiWebSocketConnection.createAssetV2Connection(restConnection.getOptions(), commandList,
                new OrderUpdateEventV2Parser(),
                callback, false);
    }

    public void subTradeClearing(SubTradeClearingRequest request, ResponseCallback<TradeClearingEvent> callback) throws Exception {
        // ????????????
        InputChecker.checker()
                .shouldNotNull(request.getSymbols(), "symbols");

        // ?????????symbol?????????
        List<String> symbolList = SymbolUtils.parseSymbols(request.getSymbols());

        // ????????????
        InputChecker.checker().checkSymbolList(symbolList);

        List<String> commandList = new ArrayList<>(symbolList.size());
        symbolList.forEach(symbol -> {

            String topic = HuobiTradeEnum.WEBSOCKET_TRADE_CLEARING_TOPIC.getCode()
                    .replace("${symbol}", symbol);

            JSONObject command = new JSONObject();
            command.put("action", huobiConstants.getActionSub());
            command.put("ch", topic);
            command.put("id", System.nanoTime());
            commandList.add(command.toJSONString());
        });

        HuobiWebSocketConnection.createAssetV2Connection(restConnection.getOptions(), commandList,
                new TradeClearingEventParser(),
                callback, false);

    }


    @Getter
    @AllArgsConstructor
    enum HuobiTradeEnum {

        CREATE_ORDER_PATH("", "/v1/order/orders/place"),

        CANCEL_ORDER_PATH("", "/v1/order/orders/{order-id}/submitcancel"),

        CANCEL_ORDER_BY_CLIENT_ORDER_ID_PATH("", "/v1/order/orders/submitCancelClientOrder"),

        BATCH_CANCEL_OPEN_ORDERS_PATH("", "/v1/order/orders/batchCancelOpenOrders"),

        BATCH_CANCEL_ORDERS_PATH("", "/v1/order/orders/batchcancel"),

        GET_OPEN_ORDERS_PATH("", "/v1/order/openOrders"),

        GET_ORDER_PATH("", "/v1/order/orders/{order-id}"),

        GET_ORDERS_PATH("", "/v1/order/orders"),

        GET_ORDERS_HISTORY_PATH("", "/v1/order/history"),

        GET_ORDER_BY_CLIENT_ORDER_ID_PATH("", "/v1/order/orders/getClientOrder"),

        GET_SINGLE_ORDER_MATCH_RESULT_PATH("", "/v1/order/orders/{order-id}/matchresults"),

        GET_MATCH_RESULT_PATH("", "/v1/order/matchresults"),

        GET_FEE_RATE_PATH("", "/v2/reference/transact-fee-rate"),

        WEBSOCKET_ORDER_UPDATE_V2_TOPIC("", "orders#${symbol}"),

        WEBSOCKET_TRADE_CLEARING_TOPIC("", "trade.clearing#${symbol}");

        private String mess;
        private String code;
    }
}

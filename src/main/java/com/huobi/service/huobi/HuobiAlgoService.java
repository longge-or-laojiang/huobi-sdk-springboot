package com.huobi.service.huobi;

import com.alibaba.fastjson.JSONObject;

import com.huobi.client.AlgoClient;
import com.huobi.client.req.algo.CancelAlgoOrderRequest;
import com.huobi.client.req.algo.CreateAlgoOrderRequest;
import com.huobi.client.req.algo.GetHistoryAlgoOrdersRequest;
import com.huobi.client.req.algo.GetOpenAlgoOrdersRequest;
import com.huobi.constant.Options;
import com.huobi.constant.enums.algo.AlgoOrderTypeEnum;
import com.huobi.model.algo.AlgoOrder;
import com.huobi.model.algo.CancelAlgoOrderResult;
import com.huobi.model.algo.CreateAlgoOrderResult;
import com.huobi.model.algo.GetHistoryAlgoOrdersResult;
import com.huobi.model.algo.GetOpenAlgoOrdersResult;
import com.huobi.service.huobi.connection.HuobiRestConnection;
import com.huobi.service.huobi.parser.algo.AlgoOrderParser;
import com.huobi.service.huobi.parser.algo.CancelAlgoOrderResultParser;
import com.huobi.service.huobi.parser.algo.CreateAlgoOrderResultParser;
import com.huobi.service.huobi.parser.algo.GetHistoryAlgoOrdersResultParser;
import com.huobi.service.huobi.parser.algo.GetOpenAlgoOrdersResultParser;
import com.huobi.service.huobi.signature.UrlParamsBuilder;
import com.huobi.utils.InputChecker;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Lazy
@Service
public class HuobiAlgoService implements AlgoClient {

    @Autowired
    private HuobiRestConnection restConnection;


    @Override
    public CreateAlgoOrderResult createAlgoOrder(CreateAlgoOrderRequest request) throws Exception {

        InputChecker checker = InputChecker.checker()
                .shouldNotNull(request.getAccountId(), "accountId")
                .shouldNotNull(request.getSymbol(), "symbol")
                .shouldNotNull(request.getOrderSide(), "orderSide")
                .shouldNotNull(request.getOrderType(), "orderType")
                .shouldNotNull(request.getClientOrderId(), "clientOrderId")
                .shouldNotNull(request.getStopPrice(), "stopPrice");

        if (request.getOrderType() == AlgoOrderTypeEnum.LIMIT) {
            checker
                    .shouldNotNull(request.getOrderPrice(), "orderPrice")
                    .shouldNotNull(request.getOrderSize(), "orderSize");
        } else {
            checker.shouldNotNull(request.getOrderValue(), "orderValue");
        }

        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToPost("accountId", request.getAccountId())
                .putToPost("symbol", request.getSymbol())
                .putToPost("orderPrice", request.getOrderPrice())
                .putToPost("orderSide", request.getOrderSide().getSide())
                .putToPost("orderSize", request.getOrderSize())
                .putToPost("orderValue", request.getOrderValue())
                .putToPost("timeInForce", request.getTimeInForce() == null ? null :
                        request.getTimeInForce().getTimeInForce())
                .putToPost("orderType", request.getOrderType().getType())
                .putToPost("clientOrderId", request.getClientOrderId())
                .putToPost("stopPrice", request.getStopPrice());

        JSONObject jsonObject =
                restConnection.executePostWithSignature(HuobiAlgoEnum.CREATE_ALGO_ORDER_PATH.getCode(), builder);
        return new CreateAlgoOrderResultParser().parse(jsonObject.getJSONObject("data"));
    }

    @Override
    public CancelAlgoOrderResult cancelAlgoOrder(CancelAlgoOrderRequest request) throws Exception {

        InputChecker.checker().checkList(request.getClientOrderIds(), 1, 50, "clientOrderIds");

        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToPost("clientOrderIds", request.getClientOrderIds());

        JSONObject jsonObject =
                restConnection.executePostWithSignature(HuobiAlgoEnum.CANCEL_ALGO_ORDER_PATH.getCode(), builder);
        JSONObject data = jsonObject.getJSONObject("data");
        return new CancelAlgoOrderResultParser().parse(data);
    }

    @Override
    public GetOpenAlgoOrdersResult getOpenAlgoOrders(GetOpenAlgoOrdersRequest request) throws Exception {

        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("accountId", request.getAccountId())
                .putToUrl("symbol", request.getSymbol())
                .putToUrl("orderSide", request.getOrderSide() == null ? null : request.getOrderSide().getSide())
                .putToUrl("orderType", request.getOrderType() == null ? null : request.getOrderType().getType())
                .putToUrl("sort", request.getSort() == null ? null : request.getSort().getSort())
                .putToUrl("limit", request.getLimit())
                .putToUrl("fromId", request.getFromId());

        JSONObject jsonObject =
                restConnection.executeGetWithSignature(HuobiAlgoEnum.GET_OPEN_ALGO_ORDERS_PATH.getCode(), builder);
        return new GetOpenAlgoOrdersResultParser().parse(jsonObject);
    }

    @Override
    public GetHistoryAlgoOrdersResult getHistoryAlgoOrders(GetHistoryAlgoOrdersRequest request) throws Exception {

        InputChecker.checker()
                .shouldNotNull(request.getSymbol(), "symbol")
                .shouldNotNull(request.getOrderStatus(), "orderStatus")
        ;

        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("accountId", request.getAccountId())
                .putToUrl("symbol", request.getSymbol())
                .putToUrl("orderSide", request.getOrderSide() == null ? null : request.getOrderSide().getSide())
                .putToUrl("orderType", request.getOrderType() == null ? null : request.getOrderType().getType())
                .putToUrl("orderStatus", request.getOrderStatus() == null ? null : request.getOrderStatus().getStatus())
                .putToUrl("startTime", request.getStartTime())
                .putToUrl("endTime", request.getEndTime())
                .putToUrl("sort", request.getSort() == null ? null : request.getSort().getSort())
                .putToUrl("limit", request.getLimit())
                .putToUrl("fromId", request.getFromId());

        JSONObject jsonObject =
                restConnection.executeGetWithSignature(HuobiAlgoEnum.GET_HISTORY_ALGO_ORDERS_PATH.getCode(), builder);
        return new GetHistoryAlgoOrdersResultParser().parse(jsonObject);
    }

    @Override
    public AlgoOrder getAlgoOrdersSpecific(String clientOrderId) throws Exception {

        InputChecker.checker().shouldNotNull(clientOrderId, "clientOrderId");

        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("clientOrderId", clientOrderId);

        JSONObject jsonObject =
                restConnection.executeGetWithSignature(HuobiAlgoEnum.GET_ALGO_ORDERS_SPECIFIC_PATH.getCode(), builder);
        return new AlgoOrderParser().parse(jsonObject.getJSONObject("data"));
    }

    @Getter
    @AllArgsConstructor
    enum HuobiAlgoEnum {

        GET_ALGO_ORDERS_SPECIFIC_PATH("查询特定策略委托", "/v2/algo-orders/specific"),

        GET_OPEN_ALGO_ORDERS_PATH("查询未触发OPEN策略委托", "/v2/algo-orders/opening"),

        GET_HISTORY_ALGO_ORDERS_PATH("查询策略委托历史", "/v2/algo-orders/history"),

        CREATE_ALGO_ORDER_PATH("策略委托下单", "/v2/algo-orders"),

        CANCEL_ALGO_ORDER_PATH("策略委托（触发前）撤单", "/v2/algo-orders/cancellation");

        private String mess;
        private String code;
    }
}

package com.huobi.service.huobi;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huobi.client.IsolatedMarginClient;
import com.huobi.client.req.margin.*;
import com.huobi.constant.enums.MarginTransferDirectionEnum;
import com.huobi.model.isolatedmargin.IsolatedMarginAccount;
import com.huobi.model.isolatedmargin.IsolatedMarginLoadOrder;
import com.huobi.model.isolatedmargin.IsolatedMarginSymbolInfo;
import com.huobi.service.huobi.connection.HuobiRestConnection;
import com.huobi.service.huobi.parser.isolatedmargin.IsolatedMarginAccountParser;
import com.huobi.service.huobi.parser.isolatedmargin.IsolatedMarginLoadOrderParser;
import com.huobi.service.huobi.parser.isolatedmargin.IsolatedMarginSymbolInfoParser;
import com.huobi.service.huobi.signature.UrlParamsBuilder;
import com.huobi.utils.InputChecker;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Lazy
@Service
public class HuobiIsolatedMarginService implements IsolatedMarginClient {

    @Autowired
    private HuobiRestConnection restConnection;


    @Override
    public Long transfer(IsolatedMarginTransferRequest request) throws Exception {

        InputChecker.checker()
                .shouldNotNull(request.getDirection(), "direction")
                .checkSymbol(request.getSymbol())
                .checkCurrency(request.getCurrency())
                .shouldNotNull(request.getAmount(), "amount");

        String path = null;
        if (request.getDirection() == MarginTransferDirectionEnum.SPOT_TO_MARGIN) {
            path = HuobiIsolatedMarginEnum.TRANSFER_TO_MARGIN_PATH.getCode();
        } else {
            path = HuobiIsolatedMarginEnum.TRANSFER_TO_SPOT_PATH.getCode();
        }

        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToPost("currency", request.getCurrency())
                .putToPost("symbol", request.getSymbol())
                .putToPost("amount", request.getAmount());

        JSONObject jsonObject = restConnection.executePostWithSignature(path, builder);
        return jsonObject.getLong("data");
    }

    @Override
    public Long applyLoan(IsolatedMarginApplyLoanRequest request) throws Exception {

        InputChecker.checker()
                .checkSymbol(request.getSymbol())
                .checkCurrency(request.getCurrency())
                .shouldNotNull(request.getAmount(), "amount");

        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToPost("currency", request.getCurrency())
                .putToPost("symbol", request.getSymbol())
                .putToPost("amount", request.getAmount());

        JSONObject jsonObject =
                restConnection.executePostWithSignature(HuobiIsolatedMarginEnum.APPLY_LOAN_PATH.getCode(), builder);
        return jsonObject.getLong("data");
    }

    @Override
    public Long repayLoan(IsolatedMarginRepayLoanRequest request) throws Exception {

        InputChecker.checker()
                .shouldNotNull(request.getOrderId(), "order-id")
                .shouldNotNull(request.getAmount(), "amount");

        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToPost("amount", request.getAmount());

        String path = HuobiIsolatedMarginEnum.REPAY_LOAN_PATH.getCode().replace("{order-id}",
                request.getOrderId().toString());
        JSONObject jsonObject = restConnection.executePostWithSignature(path, builder);
        return jsonObject.getLong("data");
    }

    @Override
    public List<IsolatedMarginLoadOrder> getLoanOrders(IsolatedMarginLoanOrdersRequest request) throws Exception {

        InputChecker.checker()
                .checkSymbol(request.getSymbol());

        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("symbol", request.getSymbol())
                .putToUrl("start-date", request.getStartDate(), "yyyy-MM-dd")
                .putToUrl("end-date", request.getEndDate(), "yyyy-MM-dd")
                .putToUrl("states", request.getStatesString())
                .putToUrl("from", request.getFrom())
                .putToUrl("size", request.getSize())
                .putToUrl("direct", request.getDirection() == null ? null : request.getDirection().getCode());

        JSONObject jsonObject =
                restConnection.executeGetWithSignature(HuobiIsolatedMarginEnum.GET_LOAN_ORDER_PATH.getCode(), builder);
        JSONArray data = jsonObject.getJSONArray("data");
        return new IsolatedMarginLoadOrderParser().parseArray(data);
    }

    @Override
    public List<IsolatedMarginAccount> getLoanBalance(IsolatedMarginAccountRequest request) throws Exception {

        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("symbol", request.getSymbol())
                .putToUrl("sub-uid", request.getSubUid());

        JSONObject jsonObject =
                restConnection.executeGetWithSignature(HuobiIsolatedMarginEnum.GET_BALANCE_PATH.getCode(), builder);
        JSONArray data = jsonObject.getJSONArray("data");
        return new IsolatedMarginAccountParser().parseArray(data);
    }

    @Override
    public List<IsolatedMarginSymbolInfo> getLoanInfo(IsolatedMarginLoanInfoRequest request) throws Exception {
        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("symbols", request.getSymbols());

        JSONObject jsonObject =
                restConnection.executeGetWithSignature(HuobiIsolatedMarginEnum.GET_LOAN_INFO_PATH.getCode(), builder);
        JSONArray data = jsonObject.getJSONArray("data");
        return new IsolatedMarginSymbolInfoParser().parseArray(data);
    }


    @Getter
    @AllArgsConstructor
    enum HuobiIsolatedMarginEnum {

        TRANSFER_TO_MARGIN_PATH("从现货账户划转至逐仓杠杆账户 transfer-in", "/v1/dw/transfer-in/margin"),

        TRANSFER_TO_SPOT_PATH("从逐仓杠杆账户划转至现货账户 transfer-out", "/v1/dw/transfer-out/margin"),

        GET_BALANCE_PATH("此接口返回借币账户详情", "/v1/margin/accounts/balance"),

        GET_LOAN_ORDER_PATH("此接口基于指定搜索条件返回借币订单", "/v1/margin/loan-orders"),

        GET_LOAN_INFO_PATH("此接口返回用户级别的借币币息率及借币额度", "/v1/margin/loan-info"),

        APPLY_LOAN_PATH("此接口用于申请借币", "/v1/margin/orders"),

        REPAY_LOAN_PATH("此接口用于归还借币", "/v1/margin/orders/{order-id}/repay");

        private String mess;
        private String code;
    }
}

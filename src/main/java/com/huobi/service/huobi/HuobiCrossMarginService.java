package com.huobi.service.huobi;

import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;

import com.huobi.client.CrossMarginClient;
import com.huobi.client.req.crossmargin.CrossMarginApplyLoanRequest;
import com.huobi.client.req.crossmargin.CrossMarginLoanOrdersRequest;
import com.huobi.client.req.crossmargin.CrossMarginRepayLoanRequest;
import com.huobi.client.req.crossmargin.CrossMarginTransferRequest;
import com.huobi.client.req.crossmargin.GeneralLoanOrdersRequest;
import com.huobi.client.req.crossmargin.GeneralRepayLoanRequest;
import com.huobi.constant.Options;
import com.huobi.constant.enums.MarginTransferDirectionEnum;
import com.huobi.model.crossmargin.CrossMarginAccount;
import com.huobi.model.crossmargin.CrossMarginCurrencyInfo;
import com.huobi.model.crossmargin.CrossMarginLoadOrder;
import com.huobi.model.crossmargin.GeneralRepayLoanRecord;
import com.huobi.model.crossmargin.GeneralRepayLoanResult;
import com.huobi.service.huobi.connection.HuobiRestConnection;
import com.huobi.service.huobi.parser.account.GeneralRepayLoanRecordParser;
import com.huobi.service.huobi.parser.account.GeneralRepayLoanResultParser;
import com.huobi.service.huobi.parser.crossmargin.CrossMarginAccountParser;
import com.huobi.service.huobi.parser.crossmargin.CrossMarginCurrencyInfoParser;
import com.huobi.service.huobi.parser.crossmargin.CrossMarginLoadOrderParser;
import com.huobi.service.huobi.signature.UrlParamsBuilder;
import com.huobi.utils.InputChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Lazy
@Service
public class HuobiCrossMarginService implements CrossMarginClient {

    @Autowired
    private HuobiRestConnection restConnection;

    @Override
    public Long transfer(CrossMarginTransferRequest request) throws Exception {

        InputChecker.checker()
                .shouldNotNull(request.getDirection(), "direction")
                .checkCurrency(request.getCurrency())
                .shouldNotNull(request.getAmount(), "amount");

        String path = null;
        if (request.getDirection() == MarginTransferDirectionEnum.SPOT_TO_MARGIN) {
            path = HuobiCrossMarginEnum.TRANSFER_TO_MARGIN_PATH.getCode();
        } else {
            path = HuobiCrossMarginEnum.TRANSFER_TO_SPOT_PATH.getCode();
        }

        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToPost("currency", request.getCurrency())
                .putToPost("amount", request.getAmount());
        JSONObject jsonObject = restConnection.executePostWithSignature(path, builder);
        return jsonObject.getLong("data");
    }

    @Override
    public Long applyLoan(CrossMarginApplyLoanRequest request) throws Exception {

        InputChecker.checker()
                .checkCurrency(request.getCurrency())
                .shouldNotNull(request.getAmount(), "amount");

        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToPost("currency", request.getCurrency())
                .putToPost("amount", request.getAmount());

        JSONObject jsonObject =
                restConnection.executePostWithSignature(HuobiCrossMarginEnum.APPLY_LOAN_PATH.getCode(), builder);
        return jsonObject.getLong("data");
    }

    @Override
    public void repayLoan(CrossMarginRepayLoanRequest request) throws Exception {
        InputChecker.checker()
                .shouldNotNull(request.getOrderId(), "order-id")
                .shouldNotNull(request.getAmount(), "amount");

        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToPost("amount", request.getAmount());

        String path = HuobiCrossMarginEnum.REPAY_LOAN_PATH.getCode().replace("{order-id}",
                request.getOrderId().toString());
        restConnection.executePostWithSignature(path, builder);
    }

    @Override
    public List<CrossMarginLoadOrder> getLoanOrders(CrossMarginLoanOrdersRequest request) throws Exception {

        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("currency", request.getCurrency())
                .putToUrl("start-date", request.getStartDate(), "yyyy-MM-dd")
                .putToUrl("end-date", request.getEndDate(), "yyyy-MM-dd")
                .putToUrl("states", request.getStatesString())
                .putToUrl("from", request.getFrom())
                .putToUrl("size", request.getSize())
                .putToUrl("direct", request.getDirection() == null ? null : request.getDirection().getCode());

        JSONObject jsonObject =
                restConnection.executeGetWithSignature(HuobiCrossMarginEnum.GET_LOAN_ORDER_PATH.getCode(), builder);
        JSONArray data = jsonObject.getJSONArray("data");
        return new CrossMarginLoadOrderParser().parseArray(data);
    }

    @Override
    public CrossMarginAccount getLoanBalance() throws Exception {

        JSONObject jsonObject =
                restConnection.executeGetWithSignature(HuobiCrossMarginEnum.GET_BALANCE_PATH.getCode(),
                        UrlParamsBuilder.build());
        JSONObject data = jsonObject.getJSONObject("data");
        return new CrossMarginAccountParser().parse(data);
    }

    public List<CrossMarginCurrencyInfo> getLoanInfo() throws Exception {
        JSONObject jsonObject =
                restConnection.executeGetWithSignature(HuobiCrossMarginEnum.GET_LOAN_INFO_PATH.getCode(),
                        UrlParamsBuilder.build());
        JSONArray data = jsonObject.getJSONArray("data");
        return new CrossMarginCurrencyInfoParser().parseArray(data);
    }

    @Override
    public List<GeneralRepayLoanResult> repayLoan(GeneralRepayLoanRequest request) throws Exception {
        InputChecker.checker()
                .shouldNotNull(request.getAccountId(), "accountId")
                .shouldNotNull(request.getCurrency(), "currency")
                .shouldNotNull(request.getAmount(), "amount");

        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToPost("accountId", request.getAccountId())
                .putToPost("currency", request.getCurrency())
                .putToPost("amount", request.getAmount())
                .putToPost("transactId", request.getTransactId());
        JSONObject jsonObject =
                restConnection.executePostWithSignature(HuobiCrossMarginEnum.GENERAL_REPAY_LOAN_PATH.getCode(),
                        builder);
        JSONArray data = jsonObject.getJSONArray("data");
        return new GeneralRepayLoanResultParser().parseArray(data);
    }

    @Override
    public List<GeneralRepayLoanRecord> getRepaymentLoanRecords(GeneralLoanOrdersRequest request) throws Exception {
        UrlParamsBuilder builder = UrlParamsBuilder.build();
        if (StringUtils.isNotEmpty(request.getRepayId()))
            builder.putToUrl("repayId", request.getRepayId());
        if (StringUtils.isNotEmpty(request.getAccountId()))
            builder.putToUrl("accountId", request.getAccountId());
        if (StringUtils.isNotEmpty(request.getCurrency()))
            builder.putToUrl("currency", request.getCurrency());
        if (request.getStartTime() != 0)
            builder.putToUrl("startTime", request.getStartTime());
        if (request.getEndTime() != 0)
            builder.putToUrl("endTime", request.getEndTime());
        if (request.getSort() != null)
            builder.putToUrl("sort", request.getSort().getSort());
        if (request.getLimit() != 0)
            builder.putToUrl("limit", request.getLimit());
        if (request.getFromId() != 0)
            builder.putToUrl("fromId", request.getFromId());
        JSONObject jsonObject =
                restConnection.executeGetWithSignature(HuobiCrossMarginEnum.GENERAL_REPAY_LOAN_PATH.getCode(),
                        builder);
        JSONArray data = jsonObject.getJSONArray("data");
        return new GeneralRepayLoanRecordParser().parseArray(data);
    }


    @Getter
    @AllArgsConstructor
    enum HuobiCrossMarginEnum {

        TRANSFER_TO_MARGIN_PATH("从现货账户划转至全仓杠杆账户 transfer-in", "/v1/cross-margin/transfer-in"),

        TRANSFER_TO_SPOT_PATH("从全仓杠杆账户划转至现货账户 transfer-out", "/v1/cross-margin/transfer-out"),

        APPLY_LOAN_PATH("申请借币（全仓）此接口用于申请借币", "/v1/cross-margin/orders"),

        REPAY_LOAN_PATH("此接口用于归还借币.", "/v1/cross-margin/orders/{order-id}/repay"),

        GET_BALANCE_PATH("此接口返回借币账户详情。", "/v1/cross-margin/accounts/balance"),

        GET_LOAN_INFO_PATH("此接口返回用户级别的借币币息率及借币额度。", "/v1/cross-margin/loan-info"),

        GET_LOAN_ORDER_PATH("此接口基于指定搜索条件返回借币订单。", "/v1/cross-margin/loan-orders"),

        GENERAL_REPAY_LOAN_PATH("还币交易记录查询", "/v2/account/repayment");

        private String mess;
        private String code;
    }
}

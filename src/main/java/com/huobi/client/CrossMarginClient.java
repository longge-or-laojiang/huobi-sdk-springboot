package com.huobi.client;

import java.util.List;

import com.huobi.client.req.crossmargin.CrossMarginApplyLoanRequest;
import com.huobi.client.req.crossmargin.CrossMarginLoanOrdersRequest;
import com.huobi.client.req.crossmargin.CrossMarginRepayLoanRequest;
import com.huobi.client.req.crossmargin.CrossMarginTransferRequest;
import com.huobi.client.req.crossmargin.GeneralLoanOrdersRequest;
import com.huobi.client.req.crossmargin.GeneralRepayLoanRequest;
import com.huobi.constant.Options;
import com.huobi.constant.enums.ExchangeEnum;
import com.huobi.exception.SDKException;
import com.huobi.model.crossmargin.CrossMarginAccount;
import com.huobi.model.crossmargin.CrossMarginCurrencyInfo;
import com.huobi.model.crossmargin.CrossMarginLoadOrder;
import com.huobi.model.crossmargin.GeneralRepayLoanRecord;
import com.huobi.model.crossmargin.GeneralRepayLoanResult;
import com.huobi.service.huobi.HuobiCrossMarginService;

public interface CrossMarginClient {

    Long transfer(CrossMarginTransferRequest request) throws Exception;

    Long applyLoan(CrossMarginApplyLoanRequest request) throws Exception;

    void repayLoan(CrossMarginRepayLoanRequest request) throws Exception;

    List<CrossMarginLoadOrder> getLoanOrders(CrossMarginLoanOrdersRequest request) throws Exception;

    CrossMarginAccount getLoanBalance() throws Exception;

    List<CrossMarginCurrencyInfo> getLoanInfo() throws Exception;

    List<GeneralRepayLoanResult> repayLoan(GeneralRepayLoanRequest request) throws Exception;

    List<GeneralRepayLoanRecord> getRepaymentLoanRecords(GeneralLoanOrdersRequest request) throws Exception;

}

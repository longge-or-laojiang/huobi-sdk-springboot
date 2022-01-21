package com.huobi.client;

import java.util.List;

import com.huobi.client.req.account.AccountAssetValuationRequest;
import com.huobi.client.req.account.AccountBalanceRequest;
import com.huobi.client.req.account.AccountFuturesTransferRequest;
import com.huobi.client.req.account.AccountHistoryRequest;
import com.huobi.client.req.account.AccountLedgerRequest;
import com.huobi.client.req.account.AccountTransferRequest;
import com.huobi.client.req.account.PointRequest;
import com.huobi.client.req.account.PointTransferRequest;
import com.huobi.client.req.account.SubAccountUpdateRequest;
import com.huobi.constant.Options;
import com.huobi.constant.enums.ExchangeEnum;
import com.huobi.exception.SDKException;
import com.huobi.model.account.Account;
import com.huobi.model.account.AccountAssetValuationResult;
import com.huobi.model.account.AccountBalance;
import com.huobi.model.account.AccountFuturesTransferResult;
import com.huobi.model.account.AccountHistory;
import com.huobi.model.account.AccountLedgerResult;
import com.huobi.model.account.AccountTransferResult;
import com.huobi.model.account.AccountUpdateEvent;
import com.huobi.model.account.Point;
import com.huobi.model.account.PointTransferResult;
import com.huobi.model.subuser.SubUserState;
import com.huobi.service.huobi.HuobiAccountService;
import com.huobi.utils.ResponseCallback;

public interface AccountClient {

    List<Account> getAccounts() throws Exception;

    AccountBalance getAccountBalance(AccountBalanceRequest request) throws Exception;

    List<AccountHistory> getAccountHistory(AccountHistoryRequest request) throws Exception;

    AccountLedgerResult getAccountLedger(AccountLedgerRequest request) throws Exception;

    AccountTransferResult accountTransfer(AccountTransferRequest request) throws Exception;

    AccountFuturesTransferResult accountFuturesTransfer(AccountFuturesTransferRequest request) throws Exception;

    Point getPoint(PointRequest request) throws Exception;

    PointTransferResult pointTransfer(PointTransferRequest request) throws Exception;

    AccountAssetValuationResult accountAssetValuation(AccountAssetValuationRequest request) throws Exception;

    void subAccountsUpdate(SubAccountUpdateRequest request, ResponseCallback<AccountUpdateEvent> callback) throws Exception;
}

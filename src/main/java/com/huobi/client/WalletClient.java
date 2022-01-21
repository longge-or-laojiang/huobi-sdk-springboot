package com.huobi.client;

import java.util.List;

import com.huobi.client.req.wallet.CreateWithdrawRequest;
import com.huobi.client.req.wallet.DepositAddressRequest;
import com.huobi.client.req.wallet.DepositWithdrawRequest;
import com.huobi.client.req.wallet.WithdrawAddressRequest;
import com.huobi.client.req.wallet.WithdrawQuotaRequest;
import com.huobi.constant.Options;
import com.huobi.constant.enums.ExchangeEnum;
import com.huobi.exception.SDKException;
import com.huobi.model.wallet.DepositAddress;
import com.huobi.model.wallet.DepositWithdraw;
import com.huobi.model.wallet.WithdrawAddressResult;
import com.huobi.model.wallet.WithdrawQuota;
import com.huobi.service.huobi.HuobiWalletService;

public interface WalletClient {

    List<DepositAddress> getDepositAddress(DepositAddressRequest request) throws Exception;

    WithdrawQuota getWithdrawQuota(WithdrawQuotaRequest request) throws Exception;

    WithdrawAddressResult getWithdrawAddress(WithdrawAddressRequest request) throws Exception;

    Long createWithdraw(CreateWithdrawRequest request) throws Exception;

    Long cancelWithdraw(Long withdrawId) throws Exception;

    List<DepositWithdraw> getDepositWithdraw(DepositWithdrawRequest request) throws Exception;
}

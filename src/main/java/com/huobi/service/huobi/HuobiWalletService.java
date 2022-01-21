package com.huobi.service.huobi;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huobi.client.WalletClient;
import com.huobi.client.req.wallet.*;
import com.huobi.model.wallet.DepositAddress;
import com.huobi.model.wallet.DepositWithdraw;
import com.huobi.model.wallet.WithdrawAddressResult;
import com.huobi.model.wallet.WithdrawQuota;
import com.huobi.service.huobi.connection.HuobiRestConnection;
import com.huobi.service.huobi.parser.wallet.DepositAddressParser;
import com.huobi.service.huobi.parser.wallet.DepositWithdrawParser;
import com.huobi.service.huobi.parser.wallet.WithdrawAddressParser;
import com.huobi.service.huobi.parser.wallet.WithdrawQuotaParser;
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
public class HuobiWalletService implements WalletClient {

    @Autowired
    private HuobiRestConnection restConnection;


    @Override
    public List<DepositAddress> getDepositAddress(DepositAddressRequest request) throws Exception {

        // 验证参数
        InputChecker.checker()
                .shouldNotNull(request.getCurrency(), "currency");

        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("currency", request.getCurrency());

        JSONObject jsonObject =
                restConnection.executeGetWithSignature(HuobiWalletEnum.GET_DEPOSIT_ADDRESS_PATH.getCode(), builder);
        JSONArray array = jsonObject.getJSONArray("data");
        return new DepositAddressParser().parseArray(array);
    }

    @Override
    public WithdrawQuota getWithdrawQuota(WithdrawQuotaRequest request) throws Exception {
        // 验证参数
        InputChecker.checker()
                .shouldNotNull(request.getCurrency(), "currency");

        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("currency", request.getCurrency());

        JSONObject jsonObject =
                restConnection.executeGetWithSignature(HuobiWalletEnum.GET_WITHDRAW_QUOTA_PATH.getCode(), builder);
        JSONObject data = jsonObject.getJSONObject("data");
        return new WithdrawQuotaParser().parse(data);
    }

    public WithdrawAddressResult getWithdrawAddress(WithdrawAddressRequest request) throws Exception {

        // 验证参数
        InputChecker.checker()
                .shouldNotNull(request.getCurrency(), "currency");

        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("currency", request.getCurrency())
                .putToUrl("chain", request.getChain())
                .putToUrl("note", request.getNote())
                .putToUrl("limit", request.getLimit())
                .putToUrl("fromId", request.getFromId());

        JSONObject jsonObject =
                restConnection.executeGetWithSignature(HuobiWalletEnum.GET_WITHDRAW_ADDRESS_PATH.getCode(), builder);
        JSONArray array = jsonObject.getJSONArray("data");

        return WithdrawAddressResult.builder()
                .nextId(jsonObject.getLong("next-id"))
                .withdrawAddressList(new WithdrawAddressParser().parseArray(array))
                .build();
    }

    @Override
    public Long createWithdraw(CreateWithdrawRequest request) throws Exception {

        InputChecker.checker()
                .shouldNotNull(request.getAddress(), "address")
                .shouldNotNull(request.getAmount(), "amount")
                .shouldNotNull(request.getCurrency(), "currency")
                .shouldNotNull(request.getFee(), "fee");

        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToPost("address", request.getAddress())
                .putToPost("amount", request.getAmount())
                .putToPost("currency", request.getCurrency())
                .putToPost("fee", request.getFee())
                .putToPost("addr-tag", request.getAddrTag())
                .putToPost("chain", request.getChain());

        JSONObject jsonObject =
                restConnection.executePostWithSignature(HuobiWalletEnum.CREATE_WITHDRAW_PATH.getCode(), builder);
        return jsonObject.getLong("data");
    }

    @Override
    public Long cancelWithdraw(Long withdrawId) throws Exception {

        InputChecker.checker()
                .shouldNotNull(withdrawId, "withdraw-id");

        String path = HuobiWalletEnum.CANCEL_WITHDRAW_PATH.getCode().replace("{withdraw-id}", withdrawId + "");

        JSONObject jsonObject = restConnection.executePostWithSignature(path, UrlParamsBuilder.build());
        return jsonObject.getLong("data");
    }

    @Override
    public List<DepositWithdraw> getDepositWithdraw(DepositWithdrawRequest request) throws Exception {

        InputChecker.checker()
                .shouldNotNull(request.getType(), "type");
        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("type", request.getType().getType())
                .putToUrl("currency", request.getCurrency())
                .putToUrl("from", request.getFrom())
                .putToUrl("size", request.getSize())
                .putToUrl("direct", request.getDirection() == null ? null : request.getDirection().getCode());

        JSONObject jsonObject =
                restConnection.executeGetWithSignature(HuobiWalletEnum.DEPOSIT_WITHDRAW_PATH.getCode(), builder);
        JSONArray data = jsonObject.getJSONArray("data");
        return new DepositWithdrawParser().parseArray(data);
    }


    @Getter
    @AllArgsConstructor
    enum HuobiWalletEnum {

        GET_DEPOSIT_ADDRESS_PATH("", "/v2/account/deposit/address"),

        GET_WITHDRAW_ADDRESS_PATH("", "/v2/account/withdraw/address"),

        GET_WITHDRAW_QUOTA_PATH("", "/v2/account/withdraw/quota"),

        CREATE_WITHDRAW_PATH("", "/v1/dw/withdraw/api/create"),

        CANCEL_WITHDRAW_PATH("", "/v1/dw/withdraw-virtual/{withdraw-id}/cancel"),

        DEPOSIT_WITHDRAW_PATH("", "/v1/query/deposit-withdraw");

        private String mess;
        private String code;
    }

}

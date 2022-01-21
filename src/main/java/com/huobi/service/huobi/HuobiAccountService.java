package com.huobi.service.huobi;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huobi.client.AccountClient;
import com.huobi.client.req.account.*;
import com.huobi.constant.HuobiConstants;
import com.huobi.model.account.*;
import com.huobi.service.huobi.connection.HuobiRestConnection;
import com.huobi.service.huobi.connection.HuobiWebSocketConnection;
import com.huobi.service.huobi.parser.account.*;
import com.huobi.service.huobi.signature.UrlParamsBuilder;
import com.huobi.utils.InputChecker;
import com.huobi.utils.ResponseCallback;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Lazy
@Service
public class HuobiAccountService implements AccountClient {

    @Autowired
    private HuobiRestConnection restConnection;

    @Autowired
    private HuobiConstants huobiConstants;

    @Override
    public List<Account> getAccounts() throws Exception {

        JSONObject jsonObject = restConnection.executeGetWithSignature(HuobiAccountEnum.GET_ACCOUNTS_PATH.getCode(),
                UrlParamsBuilder.build());
        JSONArray data = jsonObject.getJSONArray("data");
        List<Account> accountList = new AccountParser().parseArray(data);
        return accountList;
    }

    @Override
    public AccountBalance getAccountBalance(AccountBalanceRequest request) throws Exception {

        InputChecker.checker()
                .shouldNotNull(request.getAccountId(), "account-id");

        String path = HuobiAccountEnum.GET_ACCOUNT_BALANCE_PATH.getCode().replace("{account-id}",
                request.getAccountId() + "");
        JSONObject jsonObject = restConnection.executeGetWithSignature(path, UrlParamsBuilder.build());
        JSONObject data = jsonObject.getJSONObject("data");
        return new AccountBalanceParser().parse(data);
    }

    public List<AccountHistory> getAccountHistory(AccountHistoryRequest request) throws Exception {

        InputChecker.checker()
                .shouldNotNull(request.getAccountId(), "account-id");

        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("account-id", request.getAccountId())
                .putToUrl("currency", request.getCurrency())
                .putToUrl("transact-types", request.getTypesString())
                .putToUrl("start-time", request.getStartTime())
                .putToUrl("end-time", request.getEndTime())
                .putToUrl("sort", request.getSort() == null ? null : request.getSort().getSort())
                .putToUrl("size", request.getSize());

        JSONObject jsonObject =
                restConnection.executeGetWithSignature(HuobiAccountEnum.GET_ACCOUNT_HISTORY_PATH.getCode(), builder);
        JSONArray jsonArray = jsonObject.getJSONArray("data");
        AccountHistoryParser parser = new AccountHistoryParser();
        List<AccountHistory> list = new ArrayList<>(jsonArray.size());
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonItem = jsonArray.getJSONObject(i);
            list.add(parser.parse(jsonItem));
        }
        list.get(list.size() - 1).setNextId(jsonObject.getLong("next-id"));
        return list;
    }

    public AccountLedgerResult getAccountLedger(AccountLedgerRequest request) throws Exception {

        InputChecker.checker()
                .shouldNotNull(request.getAccountId(), "accountId");

        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("accountId", request.getAccountId())
                .putToUrl("currency", request.getCurrency())
                .putToUrl("transactTypes", request.getTypesString())
                .putToUrl("startTime", request.getStartTime())
                .putToUrl("endTime", request.getEndTime())
                .putToUrl("sort", request.getSort() == null ? null : request.getSort().getSort())
                .putToUrl("limit", request.getLimit())
                .putToUrl("fromId", request.getFromId());

        JSONObject jsonObject =
                restConnection.executeGetWithSignature(HuobiAccountEnum.GET_ACCOUNT_LEDGER_PATH.getCode(), builder);
        Long nextId = jsonObject.getLong("nextId");
        JSONArray jsonArray = jsonObject.getJSONArray("data");
        return AccountLedgerResult.builder()
                .nextId(nextId)
                .ledgerList(new AccountLedgerParser().parseArray(jsonArray))
                .build();
    }

    public AccountTransferResult accountTransfer(AccountTransferRequest request) throws Exception {

        InputChecker.checker()
                .shouldNotNull(request.getFromUser(), "from-user")
                .shouldNotNull(request.getFromAccount(), "from-account")
                .shouldNotNull(request.getFromAccountType(), "from-account-type")
                .shouldNotNull(request.getToUser(), "to-user")
                .shouldNotNull(request.getToAccount(), "to-account")
                .shouldNotNull(request.getToAccountType(), "to-account-type")
                .shouldNotNull(request.getCurrency(), "currency")
                .shouldNotNull(request.getAmount(), "amount");

        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToPost("from-user", request.getFromUser())
                .putToPost("from-account", request.getFromAccount())
                .putToPost("from-account-type", request.getFromAccountType().getAccountType())
                .putToPost("to-user", request.getToUser())
                .putToPost("to-account", request.getToAccount())
                .putToPost("to-account-type", request.getToAccountType().getAccountType())
                .putToPost("currency", request.getCurrency())
                .putToPost("amount", request.getAmount());

        JSONObject jsonObject =
                restConnection.executePostWithSignature(HuobiAccountEnum.ACCOUNT_TRANSFER_PATH.getCode(), builder);
        return new AccountTransferResultParser().parse(jsonObject.getJSONObject("data"));
    }

    public AccountFuturesTransferResult accountFuturesTransfer(AccountFuturesTransferRequest request) throws Exception {
        InputChecker.checker()
                .shouldNotNull(request.getCurrency(), "currency")
                .shouldNotNull(request.getAmount(), "amount")
                .shouldNotNull(request.getType(), "type");

        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToPost("currency", request.getCurrency())
                .putToPost("amount", request.getAmount())
                .putToPost("type", request.getType().getType());

        JSONObject jsonObject =
                restConnection.executePostWithSignature(HuobiAccountEnum.ACCOUNT_FUTURES_TRANSFER_PATH.getCode(),
                        builder);

        return new AccountFuturesTransferResultParser().parse(jsonObject);
    }

    @Override
    public Point getPoint(PointRequest request) throws Exception {

        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("subUid", request.getSubUid());

        JSONObject jsonObject = restConnection.executeGetWithSignature(HuobiAccountEnum.POINT_ACCOUNT_PATH.getCode(),
                builder);
        return new PointParser().parse(jsonObject);
    }

    @Override
    public PointTransferResult pointTransfer(PointTransferRequest request) throws Exception {
        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToPost("fromUid", request.getFromUid())
                .putToPost("toUid", request.getToUid())
                .putToPost("groupId", request.getGroupId())
                .putToPost("amount", request.getAmount());
        JSONObject jsonObject =
                restConnection.executePostWithSignature(HuobiAccountEnum.POINT_TRANSFER_PATH.getCode(), builder);
        return new PointTransferResultParser().parse(jsonObject);
    }

    @Override
    public AccountAssetValuationResult accountAssetValuation(AccountAssetValuationRequest request) throws Exception {
        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("accountType", request.getAccountType().getCode())
                .putToUrl("valuationCurrency", request.getValuationCurrency())
                .putToUrl("subUid", request.getSubUid());
        JSONObject jsonObject =
                restConnection.executeGetWithSignature(HuobiAccountEnum.ACCOUNT_ASSET_VALUATION_PATH.getCode(),
                        builder);
        return new AccountAssetValuationResultParser().parse(jsonObject);
    }

    public void subAccountsUpdate(SubAccountUpdateRequest request, ResponseCallback<AccountUpdateEvent> callback) throws Exception {
        InputChecker.checker()
                .shouldNotNull(request.getAccountUpdateMode(), "account update model");

        JSONObject command = new JSONObject();
        command.put("action", huobiConstants.getActionSub());
        command.put("cid", System.currentTimeMillis() + "");
        command.put("ch", HuobiAccountEnum.SUB_ACCOUNT_UPDATE_TOPIC.getCode().replace("${mode}",
                request.getAccountUpdateMode().getCode()));
        command.put("model", request.getAccountUpdateMode().getCode());

        List<String> commandList = new ArrayList<>();
        commandList.add(command.toJSONString());

        HuobiWebSocketConnection.createAssetV2Connection(restConnection.getOptions(), commandList,
                new AccountUpdateEventParser(),
                callback, false);
    }

    @Getter
    @AllArgsConstructor
    enum HuobiAccountEnum {

        GET_ACCOUNTS_PATH("查询当前用户的所有账户 ID account-id 及其相关信息", "/v1/account/accounts"),

        GET_ACCOUNT_BALANCE_PATH("查询指定账户的余额", "/v1/account/accounts/{account-id}/balance"),

        GET_ACCOUNT_HISTORY_PATH("查询账户流水", "/v1/account/history"),

        GET_ACCOUNT_LEDGER_PATH("查询财务流水", "/v2/account/ledger"),

        ACCOUNT_TRANSFER_PATH("新增币币账户与逐仓杠杠账户的划转，逐仓杠杠账户内部的划转", "/v1/account/transfer"),

        ACCOUNT_FUTURES_TRANSFER_PATH("提供币币与合约账户间的资产划转", "/v1/futures/transfer"),

        POINT_ACCOUNT_PATH("查询点卡余额", "/v2/point/account"),

        POINT_TRANSFER_PATH("点卡划转", "/v2/point/transfer"),

        ACCOUNT_ASSET_VALUATION_PATH("新增账户资产估值查询节点", "/v2/account/asset-valuation"),

        SUB_ACCOUNT_UPDATE_TOPIC("新增账户变更事件类型deposit，withdraw", "accounts.update#${mode}");

        private String mess;
        private String code;
    }
}

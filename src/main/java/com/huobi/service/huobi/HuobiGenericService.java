package com.huobi.service.huobi;

import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.huobi.client.GenericClient;
import com.huobi.client.req.generic.CurrencyChainsRequest;
import com.huobi.constant.HuobiOptions;
import com.huobi.constant.Options;
import com.huobi.model.generic.CurrencyChain;
import com.huobi.model.generic.MarketStatus;
import com.huobi.model.generic.Symbol;
import com.huobi.service.huobi.connection.HuobiRestConnection;
import com.huobi.service.huobi.parser.generic.CurrencyChainParser;
import com.huobi.service.huobi.parser.generic.MarketStatusParser;
import com.huobi.service.huobi.parser.generic.SymbolParser;
import com.huobi.service.huobi.signature.UrlParamsBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Lazy
@Service
public class HuobiGenericService implements GenericClient {

    @Autowired
    private HuobiRestConnection restConnection;


    @Override
    public String getSystemStatus() throws Exception {
        String response = restConnection.executeGetString(HuobiGenericEnum.GET_SYSTEM_STATUS_URL.getCode(),
                UrlParamsBuilder.build());
        return response;
    }

    @Override
    public MarketStatus getMarketStatus() throws Exception {
        JSONObject jsonObject = restConnection.executeGet(HuobiGenericEnum.GET_MARKET_STATUS_PATH.getCode(),
                UrlParamsBuilder.build());
        JSONObject data = jsonObject.getJSONObject("data");
        return new MarketStatusParser().parse(data);
    }

    @Override
    public List<Symbol> getSymbols() throws Exception {

        JSONObject jsonObject = restConnection.executeGet(HuobiGenericEnum.GET_SYMBOLS_PATH.getCode(),
                UrlParamsBuilder.build());
        JSONArray data = jsonObject.getJSONArray("data");
        return new SymbolParser().parseArray(data);
    }

    @Override
    public List<String> getCurrencys() throws Exception {

        JSONObject jsonObject = restConnection.executeGet(HuobiGenericEnum.GET_CURRENCY_PATH.getCode(),
                UrlParamsBuilder.build());
        JSONArray data = jsonObject.getJSONArray("data");
        return data.toJavaList(String.class);

    }

    @Override
    public List<CurrencyChain> getCurrencyChains(CurrencyChainsRequest request) throws Exception {

        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("currency", request.getCurrency())
                .putToUrl("authorizedUser", request.isAuthorizedUser() + "");

        JSONObject jsonObject = restConnection.executeGet(HuobiGenericEnum.GET_CURRENCY_CHAINS_PATH.getCode(), builder);
        JSONArray data = jsonObject.getJSONArray("data");
        return new CurrencyChainParser().parseArray(data);
    }

    @Override
    public Long getTimestamp() throws Exception {
        JSONObject jsonObject = restConnection.executeGet(HuobiGenericEnum.GET_TIMESTAMP.getCode(),
                UrlParamsBuilder.build());
        return jsonObject.getLong("data");
    }


    @Getter
    @AllArgsConstructor
    enum HuobiGenericEnum {

        GET_SYSTEM_STATUS_URL("此接口返回当前的系统状态，包含当前系统维护计划和故障进度等", "https://status.huobigroup.com/api/v2/summary.json"),

        GET_MARKET_STATUS_PATH("此节点返回当前最新市场状态", "/v2/market-status"),

        GET_SYMBOLS_PATH("此接口返回所有火币全球站支持的交易对", "/v1/common/symbols"),

        GET_CURRENCY_PATH("此接口返回所有火币全球站支持的币种", "/v1/common/currencys"),

        GET_CURRENCY_CHAINS_PATH("此节点用于查询各币种及其所在区块链的静态参考信息（公共数据）", "/v2/reference/currencies"),

        GET_TIMESTAMP("此接口返回当前的系统时间戳，即从 UTC 1970年1月1日0时0分0秒0毫秒到现在的总毫秒数", "/v1/common/timestamp");

        private String mess;
        private String code;
    }
}

package com.huobi.client;

import java.util.List;

import com.huobi.client.req.generic.CurrencyChainsRequest;
import com.huobi.constant.Options;
import com.huobi.constant.enums.ExchangeEnum;
import com.huobi.exception.SDKException;
import com.huobi.model.generic.CurrencyChain;
import com.huobi.model.generic.MarketStatus;
import com.huobi.model.generic.Symbol;
import com.huobi.service.huobi.HuobiGenericService;

public interface GenericClient {

    String getSystemStatus() throws Exception;

    MarketStatus getMarketStatus() throws Exception;

    List<Symbol> getSymbols() throws Exception;

    List<String> getCurrencys() throws Exception;

    List<CurrencyChain> getCurrencyChains(CurrencyChainsRequest request) throws Exception;

    Long getTimestamp() throws Exception;
}

package com.huobi.service.huobi;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huobi.client.ETFClient;
import com.huobi.client.req.etf.ETFSwapListRequest;
import com.huobi.client.req.etf.ETFSwapRequest;
import com.huobi.constant.enums.EtfSwapDirectionEnum;
import com.huobi.model.etf.ETFConfig;
import com.huobi.model.etf.ETFSwapRecord;
import com.huobi.service.huobi.connection.HuobiRestConnection;
import com.huobi.service.huobi.parser.etf.ETFConfigParser;
import com.huobi.service.huobi.parser.etf.ETFSwapRecordParser;
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
public class HuobiETFService implements ETFClient {

    @Autowired
    private HuobiRestConnection restConnection;


    @Override
    public ETFConfig getConfig(String etfName) throws Exception {

        InputChecker.checker()
                .shouldNotNull(etfName, "etf_name");

        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("etf_name", etfName);

        JSONObject jsonObject = restConnection.executeGetWithSignature(HuobiETFEnum.GET_SWAP_CONFIG_PATH.getCode(),
                builder);
        JSONObject data = jsonObject.getJSONObject("data");
        return new ETFConfigParser().parse(data);
    }

    @Override
    public void etfSwap(ETFSwapRequest request) throws Exception {

        InputChecker.checker()
                .shouldNotNull(request.getDirection(), "direction")
                .shouldNotNull(request.getAmount(), "amount");

        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToPost("etf_name", request.getEtfName())
                .putToPost("amount", request.getAmount());

        String path = null;
        if (request.getDirection() == EtfSwapDirectionEnum.SWAP_IN_ETF) {
            path = HuobiETFEnum.ETF_SWAP_IN_PATH.getCode();
        } else {
            path = HuobiETFEnum.ETF_SWAP_OUT_PATH.getCode();
        }

        restConnection.executePostWithSignature(path, builder);
    }

    @Override
    public List<ETFSwapRecord> getEtfSwapList(ETFSwapListRequest request) throws Exception {

        InputChecker.checker()
                .shouldNotNull(request.getEtfName(), "etf_name")
                .shouldNotNull(request.getOffset(), "offset")
                .shouldNotNull(request.getLimit(), "limit")
                .checkRange(request.getLimit(), 1, 100, "limit");

        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("etf_name", request.getEtfName())
                .putToUrl("offset", request.getOffset())
                .putToUrl("limit", request.getLimit());

        JSONObject jsonObject = restConnection.executeGetWithSignature(HuobiETFEnum.GET_SWAP_LIST_PATH.getCode(),
                builder);
        JSONArray data = jsonObject.getJSONArray("data");
        return new ETFSwapRecordParser().parseArray(data);
    }


    @Getter
    @AllArgsConstructor
    enum HuobiETFEnum {

        GET_SWAP_CONFIG_PATH("", "/etf/swap/config"),

        ETF_SWAP_IN_PATH("", "/etf/swap/in"),

        ETF_SWAP_OUT_PATH("", "/etf/swap/out"),

        GET_SWAP_LIST_PATH("", "/etf/swap/list");

        private String mess;
        private String code;
    }
}

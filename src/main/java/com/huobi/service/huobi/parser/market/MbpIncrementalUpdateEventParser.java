package com.huobi.service.huobi.parser.market;

import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.huobi.constant.HuobiConstants;
import com.huobi.model.market.MbpIncrementalUpdateEvent;
import com.huobi.service.huobi.parser.HuobiModelParser;
import com.huobi.service.huobi.utils.DataUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class MbpIncrementalUpdateEventParser implements HuobiModelParser<MbpIncrementalUpdateEvent> {

    @Autowired
    private HuobiConstants huobiConstants;

    @Override
    public MbpIncrementalUpdateEvent parse(JSONObject json) {

        String topic;
        String action;
        if (json.containsKey("rep")) {
            topic = json.getString("rep");
            action = huobiConstants.getActionRep();
        } else {
            topic = json.getString("ch");
            action = huobiConstants.getActionSub();
        }


        String dataKey = DataUtils.getDataKey(json);
        JSONObject data = json.getJSONObject(dataKey);
        return MbpIncrementalUpdateEvent.builder()
                .action(action)
                .topic(topic)
                .ts(json.getLong("ts"))
                .seqNum(data.getLong("seqNum"))
                .prevSeqNum(data.getLong("prevSeqNum"))
                .bids(new PriceLevelParser().parseArray(data.getJSONArray("bids")))
                .asks(new PriceLevelParser().parseArray(data.getJSONArray("asks")))
                .build();
    }

    @Override
    public MbpIncrementalUpdateEvent parse(JSONArray json) {
        return null;
    }

    @Override
    public List<MbpIncrementalUpdateEvent> parseArray(JSONArray jsonArray) {
        return null;
    }
}

package com.huobi.constant;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "huobi.constants")
@Data
public class HuobiConstants {

    private String apiKey;

    private String secretKey;

    private String opSub = "sub";

    private String opReq = "req";

    private String actionSub = "sub";

    private String actionRep = "rep";


}

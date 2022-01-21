package com.huobi.client;

import java.util.List;

import com.huobi.client.req.account.TransferSubuserRequest;
import com.huobi.client.req.subuser.GetApiKeyListRequest;
import com.huobi.client.req.subuser.GetSubUserAccountListRequest;
import com.huobi.client.req.subuser.GetSubUserDepositRequest;
import com.huobi.client.req.subuser.GetSubUserListRequest;
import com.huobi.client.req.subuser.SubUserApiKeyDeletionRequest;
import com.huobi.client.req.subuser.SubUserApiKeyGenerationRequest;
import com.huobi.client.req.subuser.SubUserApiKeyModificationRequest;
import com.huobi.client.req.subuser.SubUserCreationRequest;
import com.huobi.client.req.subuser.SubUserManagementRequest;
import com.huobi.client.req.subuser.SubUserTradableMarketRequest;
import com.huobi.client.req.subuser.SubUserTransferabilityRequest;
import com.huobi.constant.Options;
import com.huobi.constant.enums.ExchangeEnum;
import com.huobi.exception.SDKException;
import com.huobi.model.account.AccountBalance;
import com.huobi.model.account.SubuserAggregateBalance;
import com.huobi.model.subuser.GetApiKeyListResult;
import com.huobi.model.subuser.GetSubUserAccountListResult;
import com.huobi.model.subuser.GetSubUserDepositResult;
import com.huobi.model.subuser.GetSubUserListResult;
import com.huobi.model.subuser.SubUserApiKeyGenerationResult;
import com.huobi.model.subuser.SubUserApiKeyModificationResult;
import com.huobi.model.subuser.SubUserCreationInfo;
import com.huobi.model.subuser.SubUserManagementResult;
import com.huobi.model.subuser.SubUserState;
import com.huobi.model.subuser.SubUserTradableMarketResult;
import com.huobi.model.subuser.SubUserTransferabilityResult;
import com.huobi.model.wallet.DepositAddress;
import com.huobi.service.huobi.HuobiSubUserService;

public interface SubUserClient {


    List<SubUserCreationInfo> subuserCreation(SubUserCreationRequest request) throws Exception;


    GetSubUserListResult getSubUserList(GetSubUserListRequest request) throws Exception;

    SubUserState getSubuserState(Long subUid) throws Exception;

    SubUserManagementResult subuserManagement(SubUserManagementRequest request) throws Exception;

    GetSubUserAccountListResult getSubuserAccountList(GetSubUserAccountListRequest request) throws Exception;

    SubUserTransferabilityResult subuserTransferability(SubUserTransferabilityRequest request) throws Exception;

    SubUserTradableMarketResult subuserTradableMarket(SubUserTradableMarketRequest request) throws Exception;

    SubUserApiKeyGenerationResult subuserApiKeyGeneration(SubUserApiKeyGenerationRequest request) throws Exception;

    SubUserApiKeyModificationResult subuserApiKeyModification(SubUserApiKeyModificationRequest request) throws Exception;

    void subuserApiKeyDeletion(SubUserApiKeyDeletionRequest request) throws Exception;

    GetApiKeyListResult getApiKeyList(GetApiKeyListRequest request) throws Exception;

    List<DepositAddress> getSubUserDepositAddress(Long subUid, String currency) throws Exception;

    GetSubUserDepositResult getSubUserDeposit(GetSubUserDepositRequest request) throws Exception;

    long transferSubuser(TransferSubuserRequest request) throws Exception;

    List<AccountBalance> getSubuserAccountBalance(Long subuserId) throws Exception;

    List<SubuserAggregateBalance> getSubuserAggregateBalance() throws Exception;

    long getUid() throws Exception;
}

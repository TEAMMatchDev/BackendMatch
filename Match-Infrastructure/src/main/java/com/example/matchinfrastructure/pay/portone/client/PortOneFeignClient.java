package com.example.matchinfrastructure.pay.portone.client;

import com.example.matchinfrastructure.oauth.kakao.dto.KakaoUserAddressDto;
import com.example.matchinfrastructure.oauth.kakao.dto.KakaoUserInfoDto;
import com.example.matchinfrastructure.pay.portone.config.PortOneFeignConfiguration;
import com.example.matchinfrastructure.pay.portone.config.PortOneInfoConfig;
import com.example.matchinfrastructure.pay.portone.config.PortOneInfoErrorDecoder;
import com.example.matchinfrastructure.pay.portone.dto.*;
import com.example.matchinfrastructure.pay.portone.dto.req.PayWithBillKeyReq;
import com.example.matchinfrastructure.pay.portone.dto.req.PortOneAuthReq;
import com.example.matchinfrastructure.pay.portone.dto.req.PortOneBillReq;
import com.example.matchinfrastructure.pay.portone.dto.req.PortOnePrepareReq;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "PortOneFeignClient",
        url = "https://api.iamport.kr",
        configuration = PortOneInfoErrorDecoder.class)
@Component
public interface PortOneFeignClient {
    @PostMapping("/users/getToken")
    public PortOneResponse<PortOneAuth> getAccessToken(@RequestBody PortOneAuthReq portOneAuthReq);

    @PostMapping("/subscribe/customers/{customer_uid}")
    public PortOneResponse<PortOneBillResponse> getBillKey(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("customer_uid") String customer_uid,
            @RequestBody PortOneBillReq portOneBillReq);

    @PostMapping("/subscribe/payments/again")
    public PortOneResponse<PortOneBillPayResponse> payWithBillKey(
            @RequestHeader("Authorization") String accessToken,
            @RequestBody PayWithBillKeyReq payWithBillKeyReq
            );

    @DeleteMapping("/subscribe/customers/{customer_uid}")
    public PortOneResponse<PortOneBillDeleteResponse> deleteBillKey(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("customer_uid") String customer_uid
    );

    @PostMapping("/payments/prepare")
    public PortOneResponse<PortOnePrepareReq> preparePayments(
            @RequestHeader("Authorization") String accessToken,
            @RequestBody PortOnePrepareReq portOnePrepareReq
            );
}

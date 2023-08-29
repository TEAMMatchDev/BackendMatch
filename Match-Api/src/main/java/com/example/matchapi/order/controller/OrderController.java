package com.example.matchapi.order.controller;

import com.example.matchapi.common.aop.CheckIdExist;
import com.example.matchapi.common.aop.CheckOneTimeProject;
import com.example.matchapi.common.aop.CheckRegularProject;
import com.example.matchapi.order.dto.OrderReq;
import com.example.matchapi.order.dto.OrderRes;
import com.example.matchapi.order.service.OrderService;
import com.example.matchapi.user.service.UserService;
import com.example.matchcommon.annotation.ApiErrorCodeExample;
import com.example.matchcommon.exception.errorcode.OtherServerErrorCode;
import com.example.matchcommon.exception.errorcode.RequestErrorCode;
import com.example.matchcommon.properties.NicePayProperties;
import com.example.matchcommon.reponse.CommonResponse;
import com.example.matchdomain.donation.exception.DeleteCardErrorCode;
import com.example.matchdomain.order.exception.RegistrationCardErrorCode;
import com.example.matchdomain.project.exception.ProjectOneTimeErrorCode;
import com.example.matchdomain.project.exception.ProjectRegualrErrorCode;
import com.example.matchdomain.user.entity.User;
import com.example.matchdomain.user.exception.UserAuthErrorCode;
import com.example.matchinfrastructure.pay.nice.dto.NicePaymentAuth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


@RequestMapping("/order")
@RequiredArgsConstructor
@RestController
@Slf4j
@Tag(name = "04-Order💸",description = "NicePayment 결제 API")
public class OrderController {
    private final OrderService orderService;
    private final NicePayProperties nicePayProperties;
    private final UserService userService;


    @PostMapping("/{projectId}")
    @ApiErrorCodeExample(UserAuthErrorCode.class)
    @Operation(summary= "04-00 Order💸 결제 요청용 처음 결제할 때 요청 보내기",description = "결제 요청용 API 입니다")
    @CheckIdExist
    public CommonResponse<String> requestPay(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "프로젝트 ID", example = "1") @PathVariable("projectId") Long projectId){
        String orderId = orderService.saveRequest(user, projectId);
        return CommonResponse.onSuccess(orderId);
    }

    @GetMapping("/serverAuth")
    @Operation(summary= "04-01 Order💸 결제 인증용",description = "결제 요청용 API 입니다")
    public CommonResponse<String> requestPaymentAuth(
            @RequestParam String tid,
            @RequestParam Long amount){
        log.info("04-01 Order 결제 인증용 API 결제 ID: " + tid + " 결제 금액 " + amount);
        orderService.requestPaymentAuth(tid, amount);
        return CommonResponse.onSuccess("성공");
    }

    @Deprecated

    @PostMapping("/test/pay")
    @ApiErrorCodeExample(OtherServerErrorCode.class)
    @Operation(summary= "04-00 Order💸 결제 인증용 API 사용 X 테스트용",description = "결제 인증용 API 입니다 테스트 용")
    public CommonResponse<NicePaymentAuth> requestPayment(@RequestParam String tid,
                                                          @RequestParam Long amount){
        log.info("04-00 Order 결제 인증 테스트용 API 결제 ID: " + tid + " 결제 금액 " +amount);
        return CommonResponse.onSuccess(orderService.authPayment(tid, amount));
    }

    @Deprecated
    @PostMapping("/test/cancel/pay")
    @ApiErrorCodeExample(OtherServerErrorCode.class)
    @Operation(summary= "04-00 Order💸 결제 취소용 API 사용 X 테스트용",description = "결제 인증용 API 입니다 테스트 용")
    public CommonResponse<NicePaymentAuth> cancelPayment(@RequestParam String tid,
                                                         @RequestParam String orderId){
        log.info("04-00 Order 결제 취소 테스트용 API 결제 ID: " + tid + " 주문 번호 " +orderId);
        return CommonResponse.onSuccess(orderService.cancelPayment(tid, orderId));
    }


    @PostMapping("/pay/{projectId}")
    @ApiErrorCodeExample({OtherServerErrorCode.class, UserAuthErrorCode.class, RequestErrorCode.class, ProjectOneTimeErrorCode.class})
    @Operation(summary= "04-01 Order💸 단기 결제 API",description = "결제 API 입니다")
    @CheckRegularProject
    public CommonResponse<String> requestPayment(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "프로젝트 ID", example = "1") @PathVariable("projectId") Long projectId,
            @Valid @RequestBody OrderReq.OrderDetail orderDetail){
        log.info("04-03 Order 결제 인증용 API 결제 ID: " + orderDetail.getTid() + " 결제 금액 " + orderDetail.getAmount() +" 기부 프로젝트 ID : " + projectId);
        return CommonResponse.onSuccess(orderService.requestPayment(user , orderDetail, projectId));
    }

    @PostMapping("/pay/card")
    @ApiErrorCodeExample({UserAuthErrorCode.class, OtherServerErrorCode.class, RegistrationCardErrorCode.class})
    @Operation(summary = "04-02 Order💸 정기 결제용 카드 등록 api",description = "정기 결제를 위한 카드 등록 API 입니다.")
    public CommonResponse<String> registrationCard(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Valid @RequestBody OrderReq.RegistrationCard registrationCard){
        orderService.registrationCard(user, registrationCard);
        return CommonResponse.onSuccess("카드 등록 성공");
    }

    @GetMapping("/pay/card")
    @ApiErrorCodeExample({UserAuthErrorCode.class})
    @Operation(summary = "04-03 Order💸 정기 결제용 카드 조회 api #FRAME 결제 화면 - 단기,정기 결제", description = "정기 결제를 위한 카드 조회 API 입니다..")
    public CommonResponse<List<OrderRes.UserBillCard>> getUserBillCard(@Parameter(hidden = true) @AuthenticationPrincipal User user){
        return CommonResponse.onSuccess(orderService.getUserBillCard(user));
    }

    @DeleteMapping("/pay/card/{cardId}")
    @ApiErrorCodeExample({UserAuthErrorCode.class, DeleteCardErrorCode.class})
    @Operation(summary = "04-04 Order💸 정기,단기 결제용 카드 삭제 api #FRAME 결제 화면 - 단기,정기 결제", description = "정기 결제를 위한 카드 삭제 API 입니다..")
    @CheckIdExist
    public CommonResponse<String> deleteBillCard(@Parameter(hidden = true) @AuthenticationPrincipal User user,
                                                                      @Parameter(description = "카드 ID", example = "1") @PathVariable("cardId") Long cardId){
        orderService.deleteBillCard(cardId);
        return CommonResponse.onSuccess("삭제 성공");
    }

    @PostMapping("/pay/card/{cardId}/{projectId}")
    @ApiErrorCodeExample({UserAuthErrorCode.class, OtherServerErrorCode.class, ProjectRegualrErrorCode.class, DeleteCardErrorCode.class})
    @Operation(summary = "04-05 Order💸 정기 결제 등록 api #FRAME 결제 화면 - 정기 결제",description = "정기 결제 신청하기 API 입니다.")
    @CheckIdExist
    @CheckRegularProject
    public CommonResponse<String> regularDonation(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "카드 id",example = "1") @PathVariable Long cardId,
            @Parameter(description = "프로젝트 id", example = "2") @PathVariable Long projectId,
            @Valid @RequestBody OrderReq.RegularDonation regularDonation){
        orderService.regularDonation(user, regularDonation, cardId, projectId);
        return CommonResponse.onSuccess("정기 결제 등록 성공");
    }

    @PostMapping("/pay/one/card/{cardId}/{projectId}")
    @ApiErrorCodeExample({UserAuthErrorCode.class, OtherServerErrorCode.class, ProjectOneTimeErrorCode.class, DeleteCardErrorCode.class})
    @Operation(summary = "04-06 Order💸 빌키로 단기 결제 api #FRAME 결제 화면 - 단기 결제",description = "단 결제 신청하기 API 입니다.")
    @CheckIdExist
    @CheckOneTimeProject
    public CommonResponse<String> oneTimeDonationCard(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "카드 id",example = "1") @PathVariable Long cardId,
            @Parameter(description = "프로젝트 id", example = "2") @PathVariable Long projectId,
            @Valid @RequestBody OrderReq.OneTimeDonation oneTimeDonation){
        orderService.oneTimeDonationCard(user, oneTimeDonation, cardId, projectId);
        return CommonResponse.onSuccess("단기 결제 성공");
    }

    @PostMapping("/user")
    @ApiErrorCodeExample({UserAuthErrorCode.class})
    @Operation(summary = "04-07 Order💸 후원자 정보조회",description = "후원자 정보조회 API 입니다.")
    public CommonResponse<OrderRes.UserDetail> getUserInfo(
            @Parameter(hidden = true) @AuthenticationPrincipal User user
            ){
        return CommonResponse.onSuccess(userService.getUserInfo(user));
    }

}

package com.example.matchapi.user.controller;

import com.example.matchapi.user.dto.UserRes;
import com.example.matchapi.user.service.UserService;
import com.example.matchcommon.reponse.CommonResponse;
import com.example.matchdomain.user.entity.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Api(tags = "02-User👤")
public class UserController {
    private final UserService userService;
    @ApiOperation(value = "02-01👤 MYPage 조회", notes = "마이페이지 편집을 위한 조회 화면입니다.")
    @GetMapping(value = "/my-page")
    public CommonResponse< UserRes.MyPage> getMyPage(@AuthenticationPrincipal User user){
        return CommonResponse.onSuccess(userService.getMyPage(user));
    }
}

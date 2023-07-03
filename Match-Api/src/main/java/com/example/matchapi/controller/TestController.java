package com.example.matchapi.controller;

import com.example.matchapi.service.UserService;
import com.example.matchcommon.reponse.CommonResponse;
import com.example.matchdomain.user.entity.User;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/test")
@Api(tags = "00-Test✨")
@RequiredArgsConstructor
public class TestController {
    private final UserService userService;

    @Value("${spring.config.activate.on-profile}")
    String profile;

    @GetMapping("")
    public String getUser(){
        System.out.println("hi");
        return "Success";
    }

    @GetMapping("/response")
    public CommonResponse<String> getTest(){
        Optional<User> user = userService.findUser(1L);
        System.out.println(profile);
        return CommonResponse.onSuccess(user.get().getUsername());

    }
}

package com.example.matchapi.admin.keyword.controller;

import com.example.matchapi.admin.keyword.dto.AdminKeywordReq;
import com.example.matchapi.admin.keyword.service.AdminKeywordService;
import com.example.matchapi.keword.dto.KeywordRes;
import com.example.matchapi.keword.service.KeywordService;
import com.example.matchcommon.reponse.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/keywords")
@Tag(name = "ADMIN-07-Keyword 추천 검색 키워드 🔎",description = "검색 키워드 API")
public class AdminKeywordController {
    private final KeywordService keywordService;
    private final AdminKeywordService adminKeywordService;

    @GetMapping("")
    @Operation(summary = "07-01 ADMIN Keyword 추천 조회🔎")
    public CommonResponse<List<KeywordRes.KeywordList>> getKeywordList(){
        return CommonResponse.onSuccess(adminKeywordService.getKeywordList());
    }

    @PostMapping("")
    @Operation(summary = "07-02 ADMIN Keyword 업로드🔎")
    public CommonResponse<List<KeywordRes.KeywordList>> postKeywordList(
            @RequestBody AdminKeywordReq.KeywordUpload keyword
            ){

        return CommonResponse.onSuccess(adminKeywordService.postKeyword(keyword));
    }

    @DeleteMapping("/{keywordId}")
    @Operation(summary = "07-03 ADMIN Keyword 삭제🔎")
    public CommonResponse<String> deleteKeyword(@PathVariable Long keywordId){
        adminKeywordService.deleteKeyword(keywordId);
        return CommonResponse.onSuccess("삭제 성공");
    }

    @PatchMapping("/{keywordId}")
    @Operation(summary = "07-04 ADMIN Keyword 수정🔎")
    public CommonResponse<String> patchKeyword(@PathVariable Long keywordId,
        @RequestParam("keyword") String keyword){
        adminKeywordService.patchKeyword(keywordId,keyword);
        return CommonResponse.onSuccess("수정 성공");
    }
}

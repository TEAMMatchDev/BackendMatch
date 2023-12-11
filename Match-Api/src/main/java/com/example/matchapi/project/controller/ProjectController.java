package com.example.matchapi.project.controller;

import com.example.matchapi.common.aop.CheckIdExist;
import com.example.matchapi.donation.service.DonationService;
import com.example.matchapi.project.dto.ProjectReq;
import com.example.matchapi.project.dto.ProjectRes;
import com.example.matchapi.project.service.CommentService;
import com.example.matchcommon.constants.enums.FILTER;
import com.example.matchapi.project.service.ProjectService;
import com.example.matchcommon.annotation.ApiErrorCodeExample;
import com.example.matchcommon.exception.errorcode.RequestErrorCode;
import com.example.matchdomain.project.entity.enums.ProjectKind;
import com.example.matchdomain.project.entity.enums.ReportReason;
import com.example.matchdomain.project.exception.CommentDeleteErrorCode;
import com.example.matchdomain.project.exception.CommentGetErrorCode;
import com.example.matchdomain.project.exception.ProjectGetErrorCode;
import com.example.matchdomain.project.exception.ProjectOneTimeErrorCode;
import com.example.matchcommon.reponse.CommonResponse;
import com.example.matchcommon.reponse.PageResponse;
import com.example.matchdomain.user.entity.User;
import com.example.matchdomain.user.exception.UserAuthErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "03-Project💻", description = "프로젝트 모아보기 용 API 입니다.")
public class ProjectController {
    private final ProjectService projectService;
    private final DonationService donationService;
    private final CommentService commentService;
    @Operation(summary = "03-01💻 프로젝트 리스트 조회 API. #Web version",description = "프로젝트 리스트 조회 API 입니다.")
    @GetMapping("")
    public CommonResponse<PageResponse<List<ProjectRes.ProjectList>>> getProjectList(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "페이지", example = "0") @RequestParam(required = true, defaultValue = "0") @Min(value = 0) int page,
            @Parameter(description = "페이지 사이즈", example = "10") @RequestParam(required = true, defaultValue = "10") int size) {
        log.info("03-01 프로젝트 리스트 조회");
        return CommonResponse.onSuccess(projectService.getProjectList(user, page, size));
    }

    @Operation(summary = "03-02💻 프로젝트 상세조회 API.  #Web version",description = "프로젝트 상세조회 API 입니다.")
    @GetMapping("/{projectId}")
    @CheckIdExist
    @ApiErrorCodeExample({ProjectOneTimeErrorCode.class})
    public CommonResponse<ProjectRes.ProjectDetail> getProject(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "프로젝트 ID", example = "1") @PathVariable(required = true)Long projectId) {
        log.info("03-02 프로젝트 상세 조회 projectId : "+projectId);
        return CommonResponse.onSuccess(projectService.getProjectDetail(user, projectId));
    }


    @Operation(summary = "03-03💻 프로젝트 검색 조회  #Web version",description = "프로젝트 검색 조회 API 입니다.")
    @GetMapping("/search")
    public CommonResponse<PageResponse<List<ProjectRes.ProjectList>>> searchProjectList(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "페이지", example = "0") @RequestParam(required = true, defaultValue = "0") @Min(value = 0) int page,
            @Parameter(description = "페이지 사이즈", example = "10") @RequestParam(required = true, defaultValue = "10") int size,
            @Parameter(description = "검색어")  @RequestParam("content") String content
            ){
        log.info("03-03 프로젝트 검색 조회 projectId : "+ content);
        return CommonResponse.onSuccess(projectService.searchProjectList(user, content, page, size));
    }

    @Operation(summary = "03-04💻 프로젝트 댓글 조회",description = "프로젝트 댓글 조회 API 입니다.")
    @GetMapping("/comment/{projectId}")
    public CommonResponse<PageResponse<List<ProjectRes.CommentList>>> getProjectComment(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "페이지", example = "0") @RequestParam(required = true, defaultValue = "0") @Min(value = 0) int page,
            @Parameter(description = "페이지 사이즈", example = "10") @RequestParam(required = true, defaultValue = "10") int size,
            @Parameter(description = "프로젝트 id")  @PathVariable("projectId") Long projectId
    ){
        log.info("03-04 프로젝트 댓글 조회 projectId : "+ projectId);
        return CommonResponse.onSuccess(commentService.getProjectComment(user, projectId, page, size));
    }

    @Operation(summary = "03-05💻 프로젝트 리스트 조회 API #FRAME_프로젝트 리스트 조회.",description = "프로젝트 리스트 조회 API 입니다.")
    @GetMapping("/list")
    @ApiErrorCodeExample(UserAuthErrorCode.class)
    public CommonResponse<PageResponse<List<ProjectRes.ProjectLists>>> getProjectLists(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "페이지", example = "0") @RequestParam(required = true, defaultValue = "0") @Min(value = 0) int page,
            @Parameter(description = "페이지 사이즈", example = "10") @RequestParam(required = true, defaultValue = "10") int size,
            @Parameter(description = "후원종류") @RequestParam(required = false)ProjectKind projectKind,
            @Parameter(description = "검색어")  @RequestParam(required = false) String content,
            @Parameter(description = "필터종류 \n최신순 = LATEST \n추천순 RECOMMEND ") @RequestParam(required = false, defaultValue = "LATEST") FILTER filter
            ) {
        log.info("03-05 프로젝트 리스트 조회");
        return CommonResponse.onSuccess(projectService.getProjectLists(user, page, size, projectKind, content, filter));
    }

    @Operation(summary = "03-06💻 프로젝트 관심설정/관심삭제 API #FRAME_프로젝트 리스트 조회.",description = "프로젝트 관심 설정/삭제 API 입니다.")
    @PatchMapping("/{projectId}")
    @CheckIdExist
    @ApiErrorCodeExample({UserAuthErrorCode.class, ProjectGetErrorCode.class})
    public CommonResponse<ProjectRes.ProjectLike> patchProjectLike(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @PathVariable Long projectId
            ){
        return CommonResponse.onSuccess(projectService.patchProjectLike(user, projectId));
    }


    @Operation(summary = "03-07💻 오늘의 후원 조회 #FRAME_홈_오늘의 후원",description = "오늘의 후원 조회 API 입니다.")
    @GetMapping("/today")
    @ApiErrorCodeExample({UserAuthErrorCode.class})
    public CommonResponse<PageResponse<List<ProjectRes.ProjectLists>>> getTodayProjectList(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "페이지", example = "0") @RequestParam(required = true, defaultValue = "0") @Min(value = 0) int page,
            @Parameter(description = "페이지 사이즈", example = "10") @RequestParam(required = true, defaultValue = "10") int size
    ){
        return CommonResponse.onSuccess(projectService.getTodayProjectLists(user, page ,size));
    }


    @Operation(summary = "03-08💻 후원 상세조회 #FRAME_후원 상세조회",description = "후원 상세조회 API 입니다.")
    @GetMapping("/detail/{projectId}")
    @ApiErrorCodeExample({UserAuthErrorCode.class, ProjectGetErrorCode.class})
    public CommonResponse<ProjectRes.ProjectAppDetail> getProjectAppDetail(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "프로젝트 id")  @PathVariable("projectId") Long projectId
    ){
        return CommonResponse.onSuccess(projectService.getProjectAppDetail(user, projectId));
    }

    @Operation(summary = "03-09💻 후원 매치 기록 조회 #FRAME_후원 상세조회",description = "후원 매치 기록조회 API 입니다.")
    @GetMapping("/match/{projectId}")
    @ApiErrorCodeExample({UserAuthErrorCode.class, ProjectGetErrorCode.class})
    public CommonResponse<PageResponse<List<ProjectRes.MatchHistory>>> getMatchHistory(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "프로젝트 id")  @PathVariable("projectId") Long projectId,
            @Parameter(description = "페이지", example = "0") @RequestParam(required = true, defaultValue = "0") @Min(value = 0) int page,
            @Parameter(description = "페이지 사이즈", example = "10") @RequestParam(required = true, defaultValue = "10") int size
    ){
        return CommonResponse.onSuccess(donationService.getMatchHistory(user, projectId, page, size));
    }

    @Operation(summary = "03-10💻 후원 매치응원하기  #FRAME_후원 상세조회",description = "후원 매치 응원하기 POST API 입니다.")
    @CheckIdExist
    @PostMapping("/comment/{projectId}")
    @ApiErrorCodeExample({UserAuthErrorCode.class, ProjectGetErrorCode.class, RequestErrorCode.class})
    public CommonResponse<ProjectRes.CommentList> postComment(@Parameter(hidden = true) @AuthenticationPrincipal User user,
                                              @Parameter(description = "프로젝트 id")  @PathVariable("projectId") Long projectId,
                                              @Valid  @RequestBody ProjectReq.Comment comment){
        return CommonResponse.onSuccess(commentService.postComment(user, projectId, comment));
    }

    @Operation(summary = "03-11💻 후원 응원 신고하기 #FRAME_후원 상세조회", description = "후원 응원 신고하기 기능입니다")
    @PostMapping("/comment/report/{commentId}")
    @ApiErrorCodeExample({UserAuthErrorCode.class, CommentGetErrorCode.class})
    public CommonResponse<String> reportComment(@PathVariable Long commentId,
                                                @RequestParam("reportReason")ReportReason reportReason){
        commentService.reportComment(commentId,reportReason);
        return CommonResponse.onSuccess("신고 성공");
    }

    @Operation(summary = "03-12💻 후원 응원 삭제하기 #FRAME_후원 상세조회", description = "후원 신고하기 기능입니다")
    @DeleteMapping("/comment/{commentId}")
    @ApiErrorCodeExample({UserAuthErrorCode.class, CommentDeleteErrorCode.class})
    public CommonResponse<String> deleteComment(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @PathVariable Long commentId){
        commentService.deleteComment(user, commentId);
        return CommonResponse.onSuccess("삭제 성공");
    }

    @Operation(summary = "03-13 내가 찜한 기부처 모아보기 ", description = "내가 찜한 기부처 모아보기")
    @GetMapping("/like")
    @ApiErrorCodeExample(UserAuthErrorCode.class)
    public CommonResponse<PageResponse<List<ProjectRes.ProjectLists>>> getLikeProjects(
            @AuthenticationPrincipal User user,
            @Parameter(description = "페이지", example = "0") @RequestParam(required = true, defaultValue = "0") @Min(value = 0) int page,
            @Parameter(description = "페이지 사이즈", example = "10") @RequestParam(required = true, defaultValue = "10") int size
    ){
        return CommonResponse.onSuccess(projectService.getLikeProjects(user, page, size));
    }
}

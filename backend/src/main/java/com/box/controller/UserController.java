package com.box.controller;

import com.box.common.Result;
import com.box.common.UserContext;
import com.box.dto.ChangePasswordDTO;
import com.box.dto.RegisterDTO;
import com.box.dto.UserDetailDTO;
import com.box.dto.UserPrivacyDTO;
import com.box.entity.UserPrivacy;
import com.box.service.UserService;
import com.box.vo.TopicUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户模块接口
 */
@RequestMapping("/api/user")
@RequiredArgsConstructor
@RestController
public class UserController {

    private final UserService userService;

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public Result<TopicUserVO> info() {
        return Result.success(userService.getInfo(UserContext.getUid()));
    }

    /**
     * 保存用户隐私设置
     */
    @PostMapping("/save-privacy")
    public Result<Void> savePrivacy(@RequestBody UserPrivacyDTO dto) {
        userService.savePrivacy(UserContext.getUid(), dto);
        return Result.success(null);
    }

    /**
     * 获取当前登录用户详细信息
     */
    @GetMapping("/details")
    public Result<TopicUserVO> details() {
        return Result.success(userService.getDetails(UserContext.getUid()));
    }

    /**
     * 获取当前登录用户隐私设置
     */
    @GetMapping("/privacy")
    public Result<UserPrivacy> privacy() {
        return Result.success(userService.getPrivacy(UserContext.getUid()));
    }

    /**
     * 保存用户详细信息
     */
    @PostMapping("/save-details")
    public Result<Void> saveDetails(@RequestBody UserDetailDTO userDetailDTO) {
        userService.saveDetails(UserContext.getUid(), userDetailDTO);
        return Result.success(null);
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestBody ChangePasswordDTO changePasswordDTO) {
        userService.changePassword(UserContext.getUid(), changePasswordDTO);
        return Result.success(null);
    }

    /**
     * 修改邮箱
     */
    @PostMapping("/modify-email")
    public Result<Void> modifyEmail(@RequestBody RegisterDTO dto) {
        userService.modifyEmail(UserContext.getUid(), dto);
        return Result.success(null);
    }
}

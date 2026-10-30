package com.box.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.box.common.Result;
import com.box.common.UserContext;
import com.box.dto.AdminChangePasswordDTO;
import com.box.dto.AdminStatusDTO;
import com.box.dto.AdminUserSaveDTO;
import com.box.dto.TopicTypeDTO;
import com.box.entity.EmailRecord;
import com.box.entity.Topic;
import com.box.entity.User;
import com.box.service.AdminService;
import com.box.vo.AdminUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class adminController {

    private final AdminService adminService;
    /**
     * 获取论坛列表
     */
    @GetMapping("/forum/list")
    public Result<Page<Topic>> getForumList(@RequestParam Integer page,
                                            @RequestParam Integer size,
                                            @RequestParam(required = false) String keyword) {
        return Result.success(adminService.getForumList(UserContext.get().getRole(), page, size, keyword));
    }

    /**
     * 置顶/取消置顶帖子
     */
    @PostMapping("/forum/top")
    public Result<?> topForum(@RequestBody AdminStatusDTO adminStatusDTO) {
        adminService.topForum(UserContext.getRole(), adminStatusDTO);
        return Result.success("操作成功");
    }

    /**
     * 删除帖子
     */
    @GetMapping("/forum/delete")
    public Result<?> deleteForum(@RequestParam Integer tid) {
        adminService.deleteForum(UserContext.getUid(),UserContext.getRole(), tid);
        return Result.success("操作成功");
    }

    /**
     * 锁定/解锁帖子
     */
    @PostMapping("/forum/locked")
    public Result<Void> lockedForum(@RequestBody AdminStatusDTO adminStatusDTO) {
        adminService.lockedForum(UserContext.getRole(), adminStatusDTO);
        return Result.success(null);
    }

    /**
     * 隐藏/显示帖子
     */
    @PostMapping("/forum/invisible")
    public Result<Void> invisibleForum(@RequestBody AdminStatusDTO adminStatusDTO) {
        adminService.invisibleForum(UserContext.getRole(), adminStatusDTO);
        return Result.success(null);
    }

    /**
     * 修改帖子类型
     */
    @GetMapping("/forum/change-topic-type")
    public Result<?> changeTopicType(@RequestParam Integer tid,
                                     @RequestParam Integer type) {
        adminService.changeTopicType(UserContext.getRole(), tid, type);
        return Result.success("操作成功");
    }

    /**
     * 新增帖子分类
     */
    @PostMapping("/forum/create-type")
    public Result<Void> createType(@RequestBody TopicTypeDTO topicTypeDTO) {
        adminService.createType(UserContext.getRole(), topicTypeDTO);
        return Result.success(null);
    }

    /**
     * 修改帖子分类
     */
    @PostMapping("/forum/update-type")
    public Result<Void> updateType(@RequestBody TopicTypeDTO topicTypeDTO) {
        adminService.updateType(UserContext.getRole(), topicTypeDTO);
        return Result.success(null);
    }

    /**
     * 获取用户列表
     */
    @GetMapping("/user/list")
    public Result<Page<User>> getUserList(@RequestParam Integer page,
                                          @RequestParam Integer size,
                                          @RequestParam(required = false) String keyword) {
        return Result.success(adminService.getUserList(UserContext.getRole(), page, size, keyword));
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/user/detail")
    public Result<AdminUserVO> getUserDetail(@RequestParam Integer id) {
        return Result.success(adminService.getUserDetail(UserContext.getRole(), id));
    }

    /**
     * 保存用户信息
     */
    @PostMapping("/user/save")
    public Result<Void> saveUser(@RequestBody AdminUserSaveDTO dto) {
        adminService.saveUser(UserContext.getRole(), dto);
        return Result.success(null);
    }

    /**
     * 管理员修改用户密码
     */
    @PostMapping("/user/change-password")
    public Result<Void> changePassword(@RequestBody AdminChangePasswordDTO dto) {
        adminService.changePassword(UserContext.getRole(), dto);
        return Result.success(null);
    }

    /**
     * 获取邮件列表
     */
    @GetMapping("/email/list")
    public Result<Page<EmailRecord>> getEmailList(@RequestParam Integer page,
                                                  @RequestParam Integer size) {
        return Result.success(adminService.getEmailList(UserContext.getRole(), page, size));
    }

    /**
     * 重新发送邮件
     */
    @GetMapping("/email/resend")
    public Result<?> resendEmail(@RequestParam Integer id) {
        adminService.resendEmail(UserContext.getRole(), id);
        return Result.success("邮件已重新发送");
    }
}

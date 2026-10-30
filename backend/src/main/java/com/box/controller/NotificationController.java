package com.box.controller;

import com.box.common.Result;
import com.box.common.UserContext;
import com.box.entity.Notification;
import com.box.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知模块接口
 */
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /** 获取通知列表 */
    @GetMapping("list")
    public Result<List<Notification>> list() {
        return Result.success(notificationService.list(UserContext.getUid()));
    }

    /** 删除单条通知 */
    @GetMapping("/delete")
    public Result<Void> delete(@RequestParam Integer id) {
        notificationService.delete(UserContext.getUid(), id);
        return Result.success(null);
    }

    /** 删除全部通知 */
    @GetMapping("/delete-all")
    public Result<Void> deleteAll() {
        notificationService.deleteAll(UserContext.getUid());
        return Result.success(null);
    }
}

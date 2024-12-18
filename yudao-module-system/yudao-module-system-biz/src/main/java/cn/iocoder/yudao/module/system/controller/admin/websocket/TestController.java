package cn.iocoder.yudao.module.system.controller.admin.websocket;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 认证")
@RestController
@RequestMapping("/system/websocket/test")
@Validated
@Slf4j
public class TestController {
    @GetMapping("/gettest1")
    @Operation(summary = "获取登录用户的权限信息")
    public CommonResult<String> getPermissionInfo() {
        // 2. 拼接结果返回
        return success("test success!");
    }
}

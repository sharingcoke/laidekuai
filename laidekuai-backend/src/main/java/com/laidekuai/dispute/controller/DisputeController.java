package com.laidekuai.dispute.controller;

import com.laidekuai.common.dto.Result;
import com.laidekuai.common.util.SecurityUtils;
import com.laidekuai.dispute.dto.DisputeDetailDTO;
import com.laidekuai.dispute.service.DisputeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 争议详情控制器
 */
@RestController
@RequestMapping("/disputes")
@RequiredArgsConstructor
public class DisputeController {

    private final DisputeService disputeService;

    @GetMapping("/{id}")
    public Result<DisputeDetailDTO> getDisputeDetail(@PathVariable("id") Long disputeId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return disputeService.getDisputeDetail(disputeId, userId, SecurityUtils.isAdmin());
    }
}

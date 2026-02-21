package com.laidekuai.dispute.controller;

import com.laidekuai.common.dto.PageResult;
import com.laidekuai.common.dto.Result;
import com.laidekuai.common.util.SecurityUtils;
import com.laidekuai.dispute.dto.DisputeDTO;
import com.laidekuai.dispute.dto.DisputeDetailDTO;
import com.laidekuai.dispute.dto.DisputeResolveRequest;
import com.laidekuai.dispute.service.DisputeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员争议控制器
 */
@RestController
@RequestMapping("/admin/disputes")
@RequiredArgsConstructor
public class AdminDisputeController {

    private final DisputeService disputeService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<DisputeDTO>> listDisputes(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "order_id", required = false) Long orderId,
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size) {
        return disputeService.listAdminDisputes(status, orderId, page, size);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<DisputeDetailDTO> getDisputeDetail(@PathVariable("id") Long disputeId) {
        Long adminId = SecurityUtils.getCurrentUserId();
        return disputeService.getDisputeDetail(disputeId, adminId, true);
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> resolveDispute(@PathVariable("id") Long disputeId,
                                       @Valid @RequestBody DisputeResolveRequest request) {
        Long adminId = SecurityUtils.getCurrentUserId();
        return disputeService.resolveDispute(disputeId, request.getResolution(), request.getAdminNote(), adminId);
    }
}

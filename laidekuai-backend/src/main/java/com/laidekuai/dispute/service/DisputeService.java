package com.laidekuai.dispute.service;

import com.laidekuai.common.dto.PageResult;
import com.laidekuai.common.dto.Result;
import com.laidekuai.dispute.dto.DisputeDTO;
import com.laidekuai.dispute.dto.DisputeDetailDTO;

public interface DisputeService {

    Result<PageResult<DisputeDTO>> listAdminDisputes(String status, Long orderId, Long page, Long size);

    Result<DisputeDetailDTO> getDisputeDetail(Long disputeId, Long userId, boolean isAdmin);

    Result<Void> resolveDispute(Long disputeId, String resolution, String adminNote, Long adminId);
}

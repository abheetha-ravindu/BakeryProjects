package com.example.bakeryshop.billing;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RefundService {
    private final RefundRepository refundRepository;

    public RefundService(RefundRepository refundRepository) {
        this.refundRepository = refundRepository;
    }

    public void submitRefundRequest(RefundRequest request) {
        request.setStatus("PENDING");
        refundRepository.save(request);
    }

    public void updateRefundStatus(Long id, String newStatus) {
        RefundRequest request = refundRepository.findById(id).orElse(null);
        if (request != null) {
            request.setStatus(newStatus);
            refundRepository.save(request);
        }
    }

    public List<RefundRequest> getAllRefundRequest() {
        return refundRepository.findAll();
    }

    public void withdrawRefundRequest(Long id) {
        refundRepository.deleteById(id);
    }
}

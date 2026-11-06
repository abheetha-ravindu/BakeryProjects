package com.example.bakeryshop.billing;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/refund")
public class RefundController {
    private final RefundService refundService;

    public RefundController(RefundService refundService){
        this.refundService = refundService;
    }

    @PostMapping
    public String applyForRefund(@RequestBody  RefundRequest request){
        refundService.submitRefundRequest(request);
        return "Refund request submitted successfully and now PENDING!";
    }

    @PutMapping ("/{id}")
    public String processRefund(@PathVariable Long id,@RequestParam String status){
        refundService.updateRefundStatus(id,status);
        return "Refund request status update to "+status+ "successfully!";
    }

    @GetMapping
    public List<RefundRequest>viewAllRefundRequest(){
        return refundService.getAllRefundRequest();
    }

    @DeleteMapping("/{id}")
    public String cancelRefundRequest(@PathVariable Long id){
        refundService.withdrawRefundRequest(id);
        return "Refund request withdrawn successfully!";
    }
}

package kr.kro.airbob.domain.discountPolicy.controller;

import kr.kro.airbob.domain.discountPolicy.dto.request.DiscountPolicyCreateDto;
import kr.kro.airbob.domain.discountPolicy.dto.request.DiscountPolicyUpdateDto;
import kr.kro.airbob.domain.discountPolicy.dto.response.DiscountPolicyResponseDto;
import kr.kro.airbob.domain.discountPolicy.service.DiscountPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/discount")
public class DiscountPolicyController {

    private final DiscountPolicyService discountpolicyService;

    @GetMapping("")
    public ResponseEntity<List<DiscountPolicyResponseDto>> findValidDiscountPolicies() {
        List<DiscountPolicyResponseDto> discountPolicies = discountpolicyService.findValidDiscountPolicies();
        return ResponseEntity.ok(discountPolicies);
    }

    @PostMapping("")
    public ResponseEntity<Void> createDiscountPolicy(@RequestBody DiscountPolicyCreateDto discountPolicyCreateDto) {
        discountpolicyService.createDiscountPolicy(discountPolicyCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{discountPolicyId}")
    public ResponseEntity<Void> updateDiscountPolicy(@RequestBody DiscountPolicyUpdateDto discountPolicyUpdateDto, @PathVariable Long discountPolicyId){
        discountpolicyService.updateDiscountPolicy(discountPolicyUpdateDto, discountPolicyId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{discountPolicyId}")
    public ResponseEntity<Void> deleteDiscountPolicy(@PathVariable Long discountPolicyId){
        discountpolicyService.deletePolicy(discountPolicyId);
        return ResponseEntity.noContent().build();
    }


}

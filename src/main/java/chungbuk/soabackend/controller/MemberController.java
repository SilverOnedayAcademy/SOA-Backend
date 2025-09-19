package chungbuk.soabackend.controller;

import chungbuk.soabackend.member.Member;
import chungbuk.soabackend.member.MemberCredential;
import chungbuk.soabackend.member.MemberService;
import chungbuk.soabackend.service.PasskeyMemberDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    @Autowired
    private PasskeyMemberDetailsService passkeyMemberDetailsService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity<String> createMember(@RequestBody Member member) {
        memberService.join(member);
        return ResponseEntity.ok("Member created successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<Member> getMember(@PathVariable Long id) {
        Member member = memberService.findMember(id);
        if (member != null) {
            return ResponseEntity.ok(member);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/passkey")
    public ResponseEntity<Map<String, Object>> getMemberPasskey(@PathVariable Long id) {
        System.out.println("🔍 패스키 정보 조회 요청:");
        System.out.println("   Member ID: " + id);

        // Member 찾기
        Member member = memberService.findMember(id);
        if (member == null) {
            System.out.println("   ❌ Member not found");
            return ResponseEntity.notFound().build();
        }

        System.out.println("   ✅ Member found: " + member.getName() + " (" + member.getEmail() + ")");

        // Member의 패스키 credential 찾기 (credentialId로)
        String memberCredentialId = member.getCredentialId();
        if (memberCredentialId == null) {
            System.out.println("   ❌ Member has no passkey credential ID");
            return ResponseEntity.ok(Map.of(
                    "memberId", id,
                    "memberName", member.getName(),
                    "hasPasskey", false,
                    "message", "No passkey registered"));
        }

        // MemberCredential 찾기
        MemberCredential memberCredential = passkeyMemberDetailsService
                .findCredentialByCredentialId(memberCredentialId);
        if (memberCredential == null) {
            System.out.println("   ❌ MemberCredential not found for credentialId: " + memberCredentialId);
            return ResponseEntity.ok(Map.of(
                    "memberId", id,
                    "memberName", member.getName(),
                    "hasPasskey", false,
                    "message", "Passkey credential not found"));
        }

        System.out.println("   ✅ Passkey credential found:");
        System.out.println("   🔑 Credential ID: " + memberCredential.getCredentialId());
        System.out.println("   🏷️ Label: " + memberCredential.getLabel());
        System.out.println("   📊 Sign Count: " + memberCredential.getSignCount());

        Map<String, Object> response = new HashMap<>();
        response.put("memberId", id);
        response.put("memberName", member.getName());
        response.put("memberEmail", member.getEmail());
        response.put("hasPasskey", true);
        response.put("passkeyInfo", Map.of(
                "credentialId", memberCredential.getCredentialId(),
                "label", memberCredential.getLabel(),
                "signCount", memberCredential.getSignCount(),
                "publicKeyLength",
                memberCredential.getPublicKey() != null ? memberCredential.getPublicKey().length : 0));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-email/{email}/passkey")
    public ResponseEntity<Map<String, Object>> getMemberPasskeyByEmail(@PathVariable String email) {
        System.out.println("🔍 이메일로 패스키 정보 조회:");
        System.out.println("   Email: " + email);

        // 이메일로 Member 찾기
        Member member = passkeyMemberDetailsService.findMemberByEmail(email);
        if (member == null) {
            System.out.println("   ❌ Member not found for email: " + email);
            return ResponseEntity.notFound().build();
        }

        // Member ID로 패스키 정보 조회 (위의 메소드 재사용)
        return getMemberPasskey(member.getId());
    }
}
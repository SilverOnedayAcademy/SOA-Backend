package chungbuk.soabackend.controller;

import chungbuk.soabackend.dto.MemberRegistrationDto;
import chungbuk.soabackend.member.Member;
import chungbuk.soabackend.member.MemberRole;
import chungbuk.soabackend.service.PasskeyMemberDetailsService;
import chungbuk.soabackend.service.TempRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Controller
public class AuthController {

    @Autowired
    private PasskeyMemberDetailsService memberDetailsService;

    @Autowired
    private TempRegistrationService tempRegistrationService;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("memberRegistration", new MemberRegistrationDto());
        model.addAttribute("roles", MemberRole.values());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("memberRegistration") MemberRegistrationDto registrationDto,
            BindingResult bindingResult, Model model) {

        System.out.println("📝 회원가입 요청 받음:");
        System.out.println("   이름: " + registrationDto.getName());
        System.out.println("   이메일: " + registrationDto.getEmail());
        System.out.println("   역할: " + registrationDto.getRole());

        // 유효성 검증 실패시
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", MemberRole.values());
            return "register";
        }

        // 이메일 중복 체크
        Member existingMember = memberDetailsService.findMemberByEmail(registrationDto.getEmail());
        if (existingMember != null) {
            model.addAttribute("error", "이미 사용중인 이메일입니다");
            model.addAttribute("roles", MemberRole.values());
            return "register";
        }

        // 임시 데이터 저장
        String sessionId = tempRegistrationService.storeTempRegistration(registrationDto);

        // 패스키 설정 페이지로 리다이렉트
        return "redirect:/setup-passkey?sessionId=" + sessionId;
    }

    @GetMapping("/setup-passkey")
    public String setupPasskey(@RequestParam(required = false) String sessionId, Model model) {
        System.out.println("🔑 패스키 설정 페이지 요청:");
        System.out.println("   세션 ID: " + sessionId);

        if (sessionId != null) {
            // 임시 데이터에서 사용자 정보 가져오기
            MemberRegistrationDto tempData = tempRegistrationService.getTempRegistration(sessionId);
            if (tempData != null) {
                model.addAttribute("username", tempData.getName());
                model.addAttribute("email", tempData.getEmail());
                model.addAttribute("sessionId", sessionId);
                model.addAttribute("required", true);
                System.out.println("   사용자: " + tempData.getName() + " (" + tempData.getEmail() + ")");
            } else {
                System.out.println("   ❌ 임시 데이터를 찾을 수 없음");
                model.addAttribute("error", "세션이 만료되었습니다. 다시 회원가입을 진행해주세요.");
                return "redirect:/register";
            }
        } else {
            // 기존 방식 (sessionId 없이 직접 접근)
            model.addAttribute("required", false);
        }

        return "setup-passkey";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }
}
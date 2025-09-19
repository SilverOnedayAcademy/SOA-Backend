package chungbuk.soabackend.dto;

import chungbuk.soabackend.member.MemberRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MemberRegistrationDto {

    @NotBlank(message = "이름은 필수입니다")
    private String name;

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @NotBlank(message = "전화번호는 필수입니다")
    private String phone;

    @NotNull(message = "역할을 선택해주세요")
    private MemberRole role;

    // Guardian 전용 필드
    private Boolean governmentSupport;

    // Tutor 전용 필드
    private String birthDate;
    private String address;
    private String career;
    private String certification;

    // 생성자
    public MemberRegistrationDto() {
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public MemberRole getRole() {
        return role;
    }

    public void setRole(MemberRole role) {
        this.role = role;
    }

    public Boolean getGovernmentSupport() {
        return governmentSupport;
    }

    public void setGovernmentSupport(Boolean governmentSupport) {
        this.governmentSupport = governmentSupport;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCareer() {
        return career;
    }

    public void setCareer(String career) {
        this.career = career;
    }

    public String getCertification() {
        return certification;
    }

    public void setCertification(String certification) {
        this.certification = certification;
    }
}
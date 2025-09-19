package chungbuk.soabackend.member;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String name;
    private String phone;

    @Enumerated(EnumType.STRING)
    private MemberRole role;

    // Passkey 전용 필드
    private String credentialId;
    private String publicKey;

    // Guardian 전용
    private Boolean governmentSupport;

    // Tutor 전용
    private String birthDate;
    private String address;
    private String career;
    private String certification;
    private Boolean approved;
}
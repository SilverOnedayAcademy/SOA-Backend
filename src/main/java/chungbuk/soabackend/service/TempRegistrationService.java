package chungbuk.soabackend.service;

import chungbuk.soabackend.dto.MemberRegistrationDto;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TempRegistrationService {

    private final ConcurrentHashMap<String, MemberRegistrationDto> tempStorage = new ConcurrentHashMap<>();

    /**
     * 임시 회원가입 데이터를 저장하고 세션 ID를 반환
     */
    public String storeTempRegistration(MemberRegistrationDto registrationDto) {
        String sessionId = UUID.randomUUID().toString();
        tempStorage.put(sessionId, registrationDto);

        System.out.println("📝 임시 회원가입 데이터 저장:");
        System.out.println("   세션 ID: " + sessionId);
        System.out.println("   이름: " + registrationDto.getName());
        System.out.println("   이메일: " + registrationDto.getEmail());
        System.out.println("   역할: " + registrationDto.getRole());

        return sessionId;
    }

    /**
     * 세션 ID로 임시 데이터 조회
     */
    public MemberRegistrationDto getTempRegistration(String sessionId) {
        MemberRegistrationDto data = tempStorage.get(sessionId);

        if (data != null) {
            System.out.println("📋 임시 데이터 조회 성공: " + sessionId);
        } else {
            System.out.println("❌ 임시 데이터 조회 실패: " + sessionId);
        }

        return data;
    }

    /**
     * 회원가입 완료 후 임시 데이터 삭제
     */
    public void removeTempRegistration(String sessionId) {
        MemberRegistrationDto removed = tempStorage.remove(sessionId);

        if (removed != null) {
            System.out.println("🗑️ 임시 데이터 삭제 완료: " + sessionId);
        } else {
            System.out.println("⚠️ 삭제할 임시 데이터 없음: " + sessionId);
        }
    }

    /**
     * 현재 저장된 임시 데이터 개수
     */
    public int getTempDataCount() {
        return tempStorage.size();
    }

    /**
     * 모든 임시 데이터 삭제 (테스트용)
     */
    public void clearAll() {
        int count = tempStorage.size();
        tempStorage.clear();
        System.out.println("🧹 모든 임시 데이터 삭제: " + count + "개");
    }
}
package chungbuk.soabackend.member;

public class MemberCredential {

    private Long id;
    private String credentialId;
    private byte[] publicKey;
    private Long signCount;
    private String label;
    private Member member;

    public MemberCredential() {
    }

    public MemberCredential(String credentialId, byte[] publicKey, Long signCount, String label, Member member) {
        this.credentialId = credentialId;
        this.publicKey = publicKey;
        this.signCount = signCount;
        this.label = label;
        this.member = member;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    public Long getSignCount() {
        return signCount;
    }

    public void setSignCount(Long signCount) {
        this.signCount = signCount;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }
}
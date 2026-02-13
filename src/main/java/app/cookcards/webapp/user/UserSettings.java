package app.cookcards.webapp.user;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "user_settings")
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String uuid;

    @Enumerated(EnumType.STRING)
    @Column(name = "units_mode", nullable = false, length = 16)
    private UnitsMode unitsMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_language", nullable = false, length = 16)
    private TargetLanguage targetLanguage;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @PrePersist
    void onCreate() {
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }
        if (unitsMode == null) {
            unitsMode = UnitsMode.ORIGINAL;
        }
        if (targetLanguage == null) {
            targetLanguage = TargetLanguage.ORIGINAL;
        }
    }

    public Long getId() {
        return id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public UnitsMode getUnitsMode() {
        return unitsMode;
    }

    public void setUnitsMode(UnitsMode unitsMode) {
        this.unitsMode = unitsMode;
    }

    public TargetLanguage getTargetLanguage() {
        return targetLanguage;
    }

    public void setTargetLanguage(TargetLanguage targetLanguage) {
        this.targetLanguage = targetLanguage;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}

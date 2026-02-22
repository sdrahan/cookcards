package app.cookcards.webapp.user;

import app.cookcards.webapp.entity.BaseEntityWithUuid;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_settings")
public class UserSettings extends BaseEntityWithUuid {

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
    protected void applyDefaults() {
        if (unitsMode == null) {
            unitsMode = UnitsMode.ORIGINAL;
        }
        if (targetLanguage == null) {
            targetLanguage = TargetLanguage.ORIGINAL;
        }
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

package app.cookcards.webapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@MappedSuperclass
public class BaseEntityWithUuid extends BaseEntity {
    @NotNull
    @Column(nullable = false, unique = true, length = 36)
    private String uuid;

    @PrePersist
    protected void ensureUuid() {
        if (uuid == null || uuid.isBlank()) {
            uuid = UUID.randomUUID().toString();
        }
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}

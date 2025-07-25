package com.github.kaivu.domain;

import com.github.kaivu.domain.enumeration.ActionStatus;
import com.github.kaivu.domain.supplier.DeletedStatusSupplier;
import com.github.kaivu.domain.type.JsonObjectType;
import io.vertx.core.json.JsonObject;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.Type;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Getter
@Setter
@Cacheable
@NoArgsConstructor
@Table(name = "entity_device")
@FilterDef(
        name = "entitiesDeletedFilter",
        autoEnabled = true,
        defaultCondition = "status <> :statusDeleted",
        parameters = @ParamDef(name = "statusDeleted", type = String.class, resolver = DeletedStatusSupplier.class))
@Filter(name = "entitiesDeletedFilter")
public class EntityDevice extends AbstractAuditingEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Size(max = 500)
    @Column(name = "name", unique = true, length = 500)
    private String name;

    @Size(max = 2000)
    @Column(name = "description", length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ActionStatus status = ActionStatus.ACTIVATED;

    @Column(name = "metadata", columnDefinition = "jsonb")
    @Type(value = JsonObjectType.class)
    private JsonObject metadata = new JsonObject();

    public String getName() {
        return name.toUpperCase();
    }

    public void setName(String name) {
        this.name = name.toLowerCase();
    }
}

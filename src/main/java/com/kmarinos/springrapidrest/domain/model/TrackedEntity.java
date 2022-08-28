package com.kmarinos.springrapidrest.domain.model;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

@MappedSuperclass
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class TrackedEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name
            = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    String id;
    boolean active=true;
}

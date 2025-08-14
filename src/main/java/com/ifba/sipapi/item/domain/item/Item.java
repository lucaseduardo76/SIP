package com.ifba.sipapi.item.domain.item;

import com.ifba.sipapi.Auditable;
import com.ifba.sipapi.item.domain.picture.Picture;
import com.ifba.sipapi.region.domain.RegionModel;
import com.ifba.sipapi.user.domain.User;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@EqualsAndHashCode
@Entity
@Table(name = "item")
public class Item extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String color;

    @Column(nullable = false)
    private LocalDate finding_date;

    @Column(nullable = false)
    private Status status;

    private DayPeriod day_period;

    @Column(nullable = false)
    private Category category;

    private LocalDate date_return;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Picture> pictures;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private RegionModel region;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
}

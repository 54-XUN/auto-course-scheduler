package com.example.scheduling.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "time_slot")
@Getter
@Setter
@NoArgsConstructor
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 星期：1-5（周一至周五） */
    @NotNull
    @Min(1)
    @Max(7)
    private Integer weekDay;

    /** 节次：1-8 */
    @NotNull
    @Min(1)
    @Max(8)
    private Integer section;
}

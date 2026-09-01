package com.example.scheduling.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "course", uniqueConstraints = {
    @jakarta.persistence.UniqueConstraint(columnNames = {"code"})
})
@Getter
@Setter
@NoArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "课程编号不能为空")
    @Size(max = 32, message = "课程编号长度不能超过32")
    @Column(unique = true)
    private String code;

    @NotBlank(message = "课程名称不能为空")
    @Size(max = 64, message = "课程名称长度不能超过64")
    private String name;

    @NotNull(message = "学分不能为空")
    private BigDecimal credit;

    @NotNull(message = "周学时不能为空")
    @Min(value = 1, message = "周学时必须大于0")
    @Max(value = 8, message = "周学时不能超过8")
    private Integer weeklyHours;

    /** 类型：0-必修，1-选修 */
    private Integer type;
}

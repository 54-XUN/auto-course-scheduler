package com.example.scheduling.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "class_info")
@Getter
@Setter
@NoArgsConstructor
public class ClassInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "班级编号不能为空")
    @Size(max = 32, message = "班级编号长度不能超过32")
    @Column(unique = true, nullable = false)
    private String code;

    @NotBlank(message = "班级名称不能为空")
    @Size(max = 64, message = "班级名称长度不能超过64")
    private String name;

    @Size(max = 32, message = "年级长度不能超过32")
    private String grade;

    @NotNull(message = "班级人数不能为空")
    @Min(value = 1, message = "班级人数必须大于0")
    private Integer studentCount;
}

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
@Table(name = "classroom")
@Getter
@Setter
@NoArgsConstructor
public class Classroom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "教室编号不能为空")
    @Size(max = 32, message = "教室编号长度不能超过32")
    @Column(unique = true, nullable = false)
    private String code;

    @NotBlank(message = "教室名称不能为空")
    @Size(max = 64, message = "教室名称长度不能超过64")
    private String name;

    @NotNull(message = "教室容量不能为空")
    @Min(value = 1, message = "教室容量必须大于0")
    private Integer capacity;

    /** 类型：0-普通，1-实验室，2-多媒体 */
    private Integer type;
}

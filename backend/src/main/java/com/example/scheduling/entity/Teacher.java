package com.example.scheduling.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "teacher")
@Getter
@Setter
@NoArgsConstructor
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "教师编号不能为空")
    @Size(max = 32, message = "教师编号长度不能超过32")
    @Column(unique = true, nullable = false)
    private String code;

    @NotBlank(message = "教师姓名不能为空")
    @Size(max = 64, message = "教师姓名长度不能超过64")
    private String name;

    /** 性别：0-女，1-男 */
    private Integer gender;

    @Size(max = 20, message = "电话长度不能超过20")
    private String phone;

    @Size(max = 32, message = "职称长度不能超过32")
    private String title;
}

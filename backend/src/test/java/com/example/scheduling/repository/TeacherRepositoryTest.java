package com.example.scheduling.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.scheduling.entity.Teacher;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/** 数据层测试：H2 内存库验证唯一约束与查询方法 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class TeacherRepositoryTest {

    @Autowired
    private TeacherRepository teacherRepository;

    @Test
    void findByCode_returnsSavedTeacher() {
        Teacher t = teacher("T100");
        teacherRepository.save(t);

        Optional<Teacher> found = teacherRepository.findByCode("T100");
        assertTrue(found.isPresent());
        assertEquals("张老师", found.get().getName());
    }

    @Test
    void uniqueCode_duplicateInsertRejected() {
        teacherRepository.save(teacher("T100"));

        Teacher dup = teacher("T100");
        dup.setName("另一个");
        // 唯一索引在 flush 时触发约束异常
        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> {
            teacherRepository.saveAndFlush(dup);
        });
    }

    @Test
    void existsByCodeAndIdNot_excludesSelf() {
        Teacher saved = teacherRepository.save(teacher("T100"));

        assertTrue(teacherRepository.existsByCodeAndIdNot("T100", saved.getId() + 1));
        assertFalse(teacherRepository.existsByCodeAndIdNot("T100", saved.getId()));
    }

    private Teacher teacher(String code) {
        Teacher t = new Teacher();
        t.setCode(code);
        t.setName("张老师");
        t.setGender(1);
        t.setPhone("13800000000");
        t.setTitle("讲师");
        return t;
    }
}

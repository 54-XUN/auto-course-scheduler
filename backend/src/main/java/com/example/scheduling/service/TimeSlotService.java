package com.example.scheduling.service;

import com.example.scheduling.entity.TimeSlot;
import com.example.scheduling.exception.BusinessException;
import com.example.scheduling.repository.ScheduleRepository;
import com.example.scheduling.repository.TimeSlotRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 时间段为系统预置数据（周一~周五 × 8 节），仅允许查看与修改，禁止删除 */
@Service
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;
    private final ScheduleRepository scheduleRepository;

    public TimeSlotService(TimeSlotRepository timeSlotRepository, ScheduleRepository scheduleRepository) {
        this.timeSlotRepository = timeSlotRepository;
        this.scheduleRepository = scheduleRepository;
    }

    public List<TimeSlot> list() {
        return timeSlotRepository.findAllByOrderByWeekDayAscSectionAsc();
    }

    @Transactional
    public TimeSlot update(Long id, TimeSlot req) {
        TimeSlot slot = timeSlotRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "时间段不存在"));
        if (timeSlotRepository.existsByWeekDayAndSection(req.getWeekDay(), req.getSection())
                && !(slot.getWeekDay().equals(req.getWeekDay()) && slot.getSection().equals(req.getSection()))) {
            throw new BusinessException("该星期与节次组合已存在");
        }
        slot.setWeekDay(req.getWeekDay());
        slot.setSection(req.getSection());
        return timeSlotRepository.save(slot);
    }

    @Transactional
    public TimeSlot create(TimeSlot req) {
        if (timeSlotRepository.existsByWeekDayAndSection(req.getWeekDay(), req.getSection())) {
            throw new BusinessException("该星期与节次组合已存在");
        }
        return timeSlotRepository.save(req);
    }

    @Transactional
    public void delete(Long id) {
        if (!timeSlotRepository.existsById(id)) {
            throw new BusinessException(404, "时间段不存在");
        }
        if (scheduleRepository.existsByTimeSlotId(id)) {
            throw new BusinessException("该时间段已被排课结果引用，请先清除相关排课");
        }
        timeSlotRepository.deleteById(id);
    }
}

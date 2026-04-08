package com.example.demo.repository.auth;

import com.example.demo.domain.user.entity.BusinessHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BusinessHoursRepository extends JpaRepository<BusinessHours, Long> {

    List<BusinessHours> findByUser_IdOrderByDayOfWeekAsc(Long userId);

    @Modifying
    @Query("DELETE FROM BusinessHours b WHERE b.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}

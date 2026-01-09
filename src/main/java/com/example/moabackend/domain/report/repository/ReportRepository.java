package com.example.moabackend.domain.report.repository;

import com.example.moabackend.domain.report.entity.Report;
import com.example.moabackend.domain.report.entity.type.EReportType;
import com.example.moabackend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {
    Optional<Report> findByUserAndTypeAndDate(User user, EReportType type, LocalDate date);

    Optional<Report> findByIdAndUser(Long id, User user);
}

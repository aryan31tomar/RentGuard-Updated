package com.rentguard.report.repository;

import com.rentguard.report.model.Report;
import com.rentguard.report.model.ReportReason;
import com.rentguard.report.model.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByListingId(Long listingId);
    List<Report> findByReporterId(Long reporterId);
    List<Report> findByStatus(ReportStatus status);

    long countByStatus(ReportStatus status);
    long countByListingIdAndStatusIn(Long listingId, List<ReportStatus> statuses);

    @Query("SELECT r.reason, COUNT(r) FROM Report r GROUP BY r.reason")
    List<Object[]> countReportsByReason();
}

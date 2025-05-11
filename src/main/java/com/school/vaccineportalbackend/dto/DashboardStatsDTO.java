package com.school.vaccineportalbackend.dto;

import java.util.Map;
import java.util.List;

public class DashboardStatsDTO {
    public static class GradeWiseStats {
        private Map<String, GradeStats> stats;

        public GradeWiseStats(Map<String, GradeStats> stats) {
            this.stats = stats;
        }

        public Map<String, GradeStats> getStats() {
            return stats;
        }

        public void setStats(Map<String, GradeStats> stats) {
            this.stats = stats;
        }
    }

    public static class GradeStats {
        private long totalStudents;
        private long vaccinatedStudents;
        private double vaccinationRate;

        public GradeStats(long totalStudents, long vaccinatedStudents) {
            this.totalStudents = totalStudents;
            this.vaccinatedStudents = vaccinatedStudents;
            this.vaccinationRate = totalStudents > 0 ? (vaccinatedStudents * 100.0 / totalStudents) : 0;
        }

        public long getTotalStudents() {
            return totalStudents;
        }

        public void setTotalStudents(long totalStudents) {
            this.totalStudents = totalStudents;
        }

        public long getVaccinatedStudents() {
            return vaccinatedStudents;
        }

        public void setVaccinatedStudents(long vaccinatedStudents) {
            this.vaccinatedStudents = vaccinatedStudents;
        }

        public double getVaccinationRate() {
            return vaccinationRate;
        }

        public void setVaccinationRate(double vaccinationRate) {
            this.vaccinationRate = vaccinationRate;
        }
    }

    public static class StatusSummary {
        private long total;
        private long completed;
        private long pending;
        private long overdue;
        private double completionRate;

        public StatusSummary(long total, long completed, long pending, long overdue) {
            this.total = total;
            this.completed = completed;
            this.pending = pending;
            this.overdue = overdue;
            this.completionRate = total > 0 ? (completed * 100.0 / total) : 0;
        }

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }

        public long getCompleted() {
            return completed;
        }

        public void setCompleted(long completed) {
            this.completed = completed;
        }

        public long getPending() {
            return pending;
        }

        public void setPending(long pending) {
            this.pending = pending;
        }

        public long getOverdue() {
            return overdue;
        }

        public void setOverdue(long overdue) {
            this.overdue = overdue;
        }

        public double getCompletionRate() {
            return completionRate;
        }

        public void setCompletionRate(double completionRate) {
            this.completionRate = completionRate;
        }
    }

    public static class UpcomingDrives {
        private List<VaccinationDriveDTO> drives;
        private long totalDrives;
        private long totalDoses;

        public UpcomingDrives(List<VaccinationDriveDTO> drives) {
            this.drives = drives;
            this.totalDrives = drives.size();
            this.totalDoses = drives.stream()
                    .mapToLong(VaccinationDriveDTO::getAvailableDoses)
                    .sum();
        }

        public List<VaccinationDriveDTO> getDrives() {
            return drives;
        }

        public void setDrives(List<VaccinationDriveDTO> drives) {
            this.drives = drives;
        }

        public long getTotalDrives() {
            return totalDrives;
        }

        public void setTotalDrives(long totalDrives) {
            this.totalDrives = totalDrives;
        }

        public long getTotalDoses() {
            return totalDoses;
        }

        public void setTotalDoses(long totalDoses) {
            this.totalDoses = totalDoses;
        }
    }
} 
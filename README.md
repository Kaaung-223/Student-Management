# Student Management System

A desktop administration platform for schools, built with **JavaFX** and **MySQL**. It supports role-based dashboards for administrators, teachers, 
and staff — covering student enrolment, class management, subject curricula, attendance, exams and grades, fees and payments, leave requests, announcements, 
and email-based password recovery with BCrypt-hashed credentials.

---

## Overview

- **Purpose:** Full-featured school management: login, role-based dashboards (ADMIN / TEACHER / STAFF), student and class records, subject curricula,
- teacher assignments, attendance tracking, exam creation and grade entry, fee collection with receipts, leave request workflows, and admin analytics.
- **Entry point:** Login screen; after authentication the user is redirected to the appropriate dashboard based on their role.
- **Architecture:** JavaFX FXML views with controller classes, MySQL persistence via DAOs, plain-Java model classes, and utility services for password hashing (BCrypt),
- email (Jakarta Mail), and password-reset OTPs. Session state is passed from the dashboard into sub-views via `setTeacherInfo(...)` / `setStaffInfo(...)`.

---

## Preview

### Admin

| | |
|---|---|
| **Login** ![Login](screenshots/auth-form.png) | **Admin Dashboard** ![Admin Dashboard](screenshots/admin-dashboard.png) |
| **Financial Statement** ![Financial Statement](screenshots/admin-financial-statement.png) | **Students** ![Students](screenshots/admin-students.png) |
| **Teachers** ![Teachers](screenshots/admin-teachers.png) | **Add Teacher** ![Add Teacher](screenshots/add-teacher.png) |
| **Staff** ![Staff](screenshots/admin-staff.png) | **Add Staff** ![Add Staff](screenshots/add-staff.png) |
| **Class Management** ![Class Management](screenshots/class-management.png) | **Class Fees** ![Class Fees](screenshots/class-fees.png) |
| **Subjects** ![Subjects](screenshots/subjects.png) | **Exams** ![Exams](screenshots/exams.png) |
| **Grades / Results** ![Grades](screenshots/grades-results.png) | **Attendance** ![Attendance](screenshots/attendance.png) |
| **Leave Requests** ![Leave Requests](screenshots/leave-requests.png) | **Announcement Management** ![Announcements](screenshots/announcements.png) |
| **My Profile** ![Profile](screenshots/profile.png) | |

### Teacher

| | |
|---|---|
| **Teacher Dashboard** ![Teacher Dashboard](screenshots/teacher-dashboard.png) | **Class Performance** ![Class Performance](screenshots/teacher-class-performance.png) |
| **My Students** ![My Students](screenshots/teacher-students.png) | **Attendance** ![Attendance](screenshots/teacher-attendance.png) |
| **Exams** ![Exams](screenshots/teacher-exams.png) | **Results & Reports** ![Results](screenshots/teacher-results.png) |
| **My Subjects** ![My Subjects](screenshots/teacher-subjects.png) | **My Classes** ![My Classes](screenshots/teacher-classes.png) |
| **Leave Requests** ![Leave Requests](screenshots/teacher-leave-requests.png) | **Announcements** ![Announcements](screenshots/teacher-announcements.png) |
| **My Profile** ![Profile](screenshots/teacher-profile.png) | |

### Staff

| | |
|---|---|
| **Staff Dashboard** ![Staff Dashboard](screenshots/staff-dashboard.png) | **Students** ![Students](screenshots/staff-students.png) |
| **Add Student** ![Add Student](screenshots/staff-add-student.png) | **Payments** ![Payments](screenshots/staff-payments.png) |
| **Payment Receipt** ![Receipt](screenshots/staff-receipt.png) | **Leave Requests** ![Leave Requests](screenshots/staff-leave-requests.png) |
| **New Leave Request** ![New Leave Request](screenshots/staff-new-leave.png) | **Announcements** ![Announcements](screenshots/staff-announcements.png) |
| **My Profile** ![Profile](screenshots/staff-profile.png) | |

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| **Language** | Java (25) |
| **UI** | JavaFX (FXML, CSS), Ikonli (FontAwesome) |
| **Database** | MySQL (JDBC via `mysql-connector-j`) |
| **Build** | Maven |
| **Auth** | BCrypt (jbcrypt 0.4), password reset via OTP |
| **Email** | Jakarta Mail 2.0 (Gmail SMTP) |
| **Config** | dotenv-java (`.env` for DB and Gmail credentials) |
| **Misc** | JavaFX WebView (receipt printing), JavaFX Charts (Pie / Bar) |

---

## Project Structure
```
Student_Management_System/
├── pom.xml # Maven build (JavaFX + MySQL + BCrypt + Jakarta Mail)
├── mvnw # Maven wrapper (Unix)
├── mvnw.cmd # Maven wrapper (Windows)
├── .gitignore
├── student_management_backup.sql # DB dump for seeding / restoring
│
├── uploads/ # Runtime photo & receipt storage
│ ├── admin_photos/
│ ├── staff_photos/
│ ├── student_photos/
│ ├── teacher_photos/
│ └── Receive/ # Generated receipt files (HTML / printed)
│
├── target/ # Maven build output (ignored)
│
└── src/
└── main/
├── java/
│ ├── module-info.java # JavaFX module descriptor
│ └── com/example/student_management_system/Controller/
│ ├── Main.java # Application.launch() entry point
│ ├── LoginController.java
│ ├── ForgotPasswordController.java
│ ├── ChangePasswordController.java
│ │
│ ├── Admin/ # Admin-side FXML controllers
│ │ ├── AdminDashboardController.java
│ │ ├── AdminStudentsController.java
│ │ ├── AdminTeacherController.java
│ │ ├── AddTeacherDialogController.java
│ │ ├── AdminStaffController.java
│ │ ├── AddStaffDialogController.java
│ │ ├── AdminClassController.java
│ │ ├── AdminClassFeeController.java
│ │ ├── AdminSubjectController.java
│ │ ├── EditSubjectDialogController.java
│ │ ├── AdminExamController.java
│ │ ├── AdminGradeController.java
│ │ ├── AdminAttendanceController.java
│ │ ├── AdminAnnouncementController.java
│ │ └── AdminProfileController.java
│ │
│ ├── Teacher/ # Teacher-side FXML controllers
│ │ ├── TeacherDashboardController.java
│ │ ├── TeacherStudentsController.java
│ │ ├── TeacherAttendanceController.java
│ │ ├── TeacherExamController.java
│ │ ├── TeacherResultsController.java
│ │ ├── TeacherSubjectsController.java
│ │ ├── TeacherClassesController.java
│ │ ├── TeacherLeaveRequestsController.java
│ │ ├── TeacherAnnouncementsController.java
│ │ └── TeacherProfileController.java
│ │
│ ├── Staff/ # Staff-side FXML controllers
│ │ ├── StaffDashboardController.java
│ │ ├── StaffStudentsController.java
│ │ ├── AddStudentDialogController.java
│ │ ├── StaffPaymentsController.java
│ │ ├── StaffLeaveController.java
│ │ ├── AddLeaveRequestDialogController.java
│ │ ├── StaffAnnouncementsController.java
│ │ └── StaffProfileController.java
│ │
│ ├── Model/ # Domain entities (POJOs)
│ │ ├── User.java, Admin.java, Staff.java, Teacher.java
│ │ ├── Student.java, StudentProfile.java, StudentPerformance.java
│ │ ├── Batch.java, BatchFilter.java, ClassData.java, ClassOption.java
│ │ ├── Subject.java, SubjectOption.java, SubjectResult.java
│ │ ├── Exam.java, ExamItem.java, ExamOption.java
│ │ ├── ExamResultRow.java, ExamResultSummary.java, BatchExamRate.java
│ │ ├── StudentGradeRow.java, StudentMarkRow.java, GradeSummary.java
│ │ ├── AttendanceRow.java, AttendanceSummary.java, DateRangeOption.java
│ │ ├── LeaveRequest.java, LeaveRequestRow.java
│ │ ├── PaymentRow.java, PaymentStudent.java
│ │ ├── FeeClassBreakdown.java, FeeFinancialSummary.java
│ │ ├── Announcement.java, AnnouncementRow.java, AnnouncementNotice.java
│ │ ├── StaffInfo.java, StaffStudentDetails.java, StaffAnnouncementRow.java
│ │ ├── TeacherInfo.java, TeacherClass.java, TeacherStudent.java
│ │ ├── StudentAttendanceRow.java, StudentFeeRow.java
│ │ ├── PerformancePeriod.java
│ │ └── ...
│ │
│ ├── DAO/ # Data access layer
│ │ ├── DBConnention.java
│ │ ├── UserDAO.java, UserService.java
│ │ ├── AdminDAO.java, StaffDAO.java, TeacherDAO.java
│ │ ├── StudentDao.java, StudentProfileDAO.java
│ │ ├── StaffStudentCreateDAO.java, StaffStudentDetailsDAO.java
│ │ ├── ClassDAO.java, SubjectDAO.java
│ │ ├── AttendanceDAO.java, TeacherAttendanceDAO.java
│ │ ├── ExamDAO.java, GradeDAO.java
│ │ ├── TeacherExamDAO.java, TeacherResultsDAO.java
│ │ ├── FeeDAO.java, StaffPaymentsDAO.java
│ │ ├── LeaveDAO.java, StaffLeaveDAO.java, StaffLeaveCreateDAO.java
│ │ ├── TeacherLeaveRequestsDAO.java
│ │ ├── AnnouncementDAO.java, StaffAnnouncementsDAO.java, TeacherAnnouncementsDAO.java
│ │ ├── StaffDashboardDAO.java, TeacherDashboardDAO.java
│ │ ├── TeacherClassesDAO.java, TeacherStudentsDAO.java, TeacherSubjectsDAO.java
│ │ ├── StaffProfileDAO.java, TeacherProfileDAO.java
│ │ └── ...
│ │
│ └── Util/ # Utility / security / email helpers
│ ├── PasswordHasher.java
│ ├── AutoHashOnStartup.java
│ ├── HashExistingPasswords.java
│ ├── GenerateHash.java, GenerateAllHashes.java
│ ├── PasswordResetService.java
│ └── EmailService.java
│
└── resources/
├── com/example/student_management_system/
│ ├── View/ # FXML screens
│ │ ├── Login.fxml, ForgotPassword.fxml
│ │ ├── Admin/
│ │ │ ├── AdminDashboard.fxml
│ │ │ ├── AdminStudent.fxml, AdminTeacher.fxml, AdminStaff.fxml
│ │ │ ├── AdminClass.fxml, AdminClassFee.fxml, AdminSubject.fxml
│ │ │ ├── AdminExam.fxml, AdminGrade.fxml, AdminAttendance.fxml
│ │ │ ├── AdminAnnouncement.fxml, AdminProfile.fxml
│ │ │ ├── AdminTeacherDialog.fxml, AdminStaffDialog.fxml
│ │ │ └── EditSubjectDialog.fxml
│ │ ├── Teacher/
│ │ │ ├── TeacherDashboard.fxml
│ │ │ ├── TeacherStudents.fxml, TeacherAttendance.fxml
│ │ │ ├── TeacherExam.fxml, TeacherResult.fxml
│ │ │ ├── TeacherSubjects.fxml, TeacherClasses.fxml
│ │ │ ├── TeacherLeaveRequests.fxml, TeacherAnnouncements.fxml
│ │ │ └── TeacherProfile.fxml
│ │ └── Staff/
│ │ ├── StaffDashboard.fxml
│ │ ├── StaffStudent.fxml, StaffStudentDialog.fxml
│ │ ├── StaffPayment.fxml
│ │ ├── StaffLeave.fxml, StaffLeaveDialog.fxml
│ │ ├── StaffAnnouncement.fxml
│ │ └── StaffProfile.fxml
│ ├── Images/ # Icons, default avatar, etc.
│ └── CSS/ # Stylesheets
│
└── Mail/
└── Properties # Mail / SMTP properties file
```
---

## Features

### Authentication & Security

- **Login** with role-based redirect: **ADMIN** → Admin Dashboard; **TEACHER** → Teacher Dashboard; **STAFF** → Staff Dashboard.
- **BCrypt password hashing** (work factor 12) for all roles, applied automatically to new accounts and password changes.
- **Auto-migration on startup** — `AutoHashOnStartup` converts any legacy plaintext passwords to BCrypt, and the login flow upgrades a plaintext row the first time it succeeds.
- **Login lockout** — 3 failed attempts trigger a 10-second lockout with a live countdown on the SIGN IN button.
- **Forgot password** — 3-step wizard (email → 6-digit OTP → new password). OTPs expire in 30 seconds with a 5-attempt limit and a 300-second verified grace window.
- **OTP email delivery** via Gmail SMTP (Jakarta Mail).

### Admin

- **Dashboard:** Stat cards (students, teachers, classes, subjects), attendance pie chart, exam pass/fail pie chart, and a fee breakdown bar chart with a per-batch filter.
- **Students:** Profile view with attendance counts, grade counts, latest exam result, and performance-period selector (attendance rate + GPA per month).
- **Teachers:** Card view with avatar, subjects taught, class-leader badge, salary, and photo. Full CRUD via dialog with subject checklist and class-leader picker.
- **Staff:** Card view with avatar, login username, salary, and hire date. Full CRUD via dialog.
- **Classes:** CRUD with academic year, room, duration, fees, assigned class teacher, and status.
- **Class Fees:** Financial summary (expected, collected, outstanding, paid/unpaid counts), per-student fee rows, and status filter (Paid / Unpaid / Partial / Overdue / No Fee).
- **Subjects:** Two-panel screen — assign subjects to a class's curriculum on the left, manage the master subject list on the right (add / edit / delete).
- **Exams:** Per-batch exam CRUD with date, total marks, and grade-count metadata.
- **Grades:** Pass/fail summary, GPA stats, per-exam rates table, and per-student results with badge coloring.
- **Attendance:** Aggregate view with filters (batch, student, period type: Overall / Month / 6 Months / Year), per-student attendance rows, extra-absent-day detection, and a leave-request table with status badges.
- **Announcements:** CRUD with target audience (TEACHER / STAFF / ALL) and date.
- **Profile:** Update name, username, and photo; change password with live rules.

### Teacher

- **Dashboard:** Stat cards (students, classes, subjects, pending leave), attendance pie (filter by period and class), exam pass/fail pie (filter by exam), and a per-class performance bar chart.
- **My Students:** Roster with profile card (attendance, grades, leave counts, latest exam) and a performance-period selector.
- **Attendance:** Date-based marking per batch, with a status ComboBox and remarks field, live summary counters, "mark all present", and batch save.
- **Exams:** Create exams for a batch, then enter per-student marks for a selected subject.
- **Results:** Per-student result summary with GPA and pass/fail, plus a subject breakdown for the selected student.
- **My Subjects:** Subjects taught with class usage and average score statistics.
- **My Classes:** Classes where the teacher is leader or subject teacher, with role badges and per-class counts.
- **Leave Requests:** Read-only view with filters and status counters.
- **Announcements:** List + detail view; auto-marks as read on refresh.
- **Profile:** Update name, change password with live rules, HiDPI-aware circular photo.

### Staff

- **Dashboard:** Stat cards (students, classes, collected this month, pending leaves), paid/unpaid fee pie chart, and payment-methods pie chart (last 90 days).
- **Students:** Roster with full profile card (attendance, grades, fees), status toggle (ACTIVE / INACTIVE), and an Add Student dialog with photo upload, validation, and uniqueness checks.
- **Payments:** Student picker with filters, batch price card, fee summary, payment form (amount, method, period, receipt no., note), payment history, and downloadable HTML receipts.
- **Leave Requests:** List with filters, counters, approve/reject actions, and an Add Leave Request dialog with overlap detection.
- **Announcements:** List + detail view; auto-marks as read on refresh.
- **Profile:** Update name, change password with live rules, display salary.

### UI / UX

- **Role-specific dashboards** with a shared sidebar layout, active-button highlighting, and toast-based notifications.
- **Toast notifications** (top-right) for success and error states, chained on login to show a welcome message followed by any unread announcements.
- **Consistent design language** across all screens: rounded cards, soft shadows, badge pills, and color-coded status indicators.
- **HiDPI-aware avatars** — images are loaded at 3× display size, center-cropped to a square, and clipped to a circle for crisp rendering on Retina screens.
- **Debounced search** — 250 ms debounce on all text filters to avoid excessive queries.
- **Photo uploads** — teacher, staff, and student photos are copied into per-role folders under `uploads/` with timestamped filenames to avoid collisions.

---

## Configuration

Create a `.env` file in the project root (do not commit secrets):

### Database Connection
Open `src/main/java/com/example/student_management_system/Controller/DAO/DBConnention.java` and edit the following constants:

java
private static final String HOST = "localhost";
private static final String PORT = "3306";
private static final String DATABASE = "student_management";
private static final String USER = "root";
private static final String PASSWORD = "your_mysql_password";

# Gmail SMTP (for password-reset OTP)
GMAIL_ACCOUNT=your.email@gmail.com
APP_PASSWORD=your_16_char_app_password

## Running the Application

- **Recommended:** `mvn clean javafx:run` or `./mvnw clean javafx:run` (uses `Main` as the main class in `pom.xml`).
- **Alternative:** Run `Main` (or `LoginController`) from the IDE; ensure VM options for JavaFX if needed.
- **Database:** Import `student_management_backup.sql` into a MySQL instance named `student_management` before first launch.
- **First launch:** `AutoHashOnStartup.run()` runs once at startup and migrates any plaintext passwords in the `users` table to BCrypt.

---

## Summary

The **Student Management System** is a JavaFX + MySQL desktop app that covers login, role-based dashboards (Admin, Teacher, Staff), students, teachers, 
staff, classes, subjects, exams, grades, attendance, class fees, leave requests, announcements, and profiles. Credentials are secured with BCrypt, 
with email-based OTP password reset. The project is modular (`module-info.java`) with a consistent set of DAOs, models, and utilities used across all three roles.

---

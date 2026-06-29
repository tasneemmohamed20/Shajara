# Moodle staging endpoints implemented

The network layer now exposes the staging Postman functions through `RetrofitApiService`, grouped behind repositories/data sources for easier ViewModel use.

## AuthRepository
- `login/token.php`
- `local_getuserdetailsapi_login`
- `core_auth_request_password_reset`
- `auth_email_get_signup_settings`
- `core_webservice_get_site_info`

## UserRepository
- `core_user_get_users_by_field`
- `core_user_get_user_preferences`
- `local_preferencesapi_update_preference`
- `core_user_update_picture`
- draft file upload through `webservice/upload.php`

## CoursesRepository
- `core_enrol_get_users_courses`
- `core_course_get_courses`
- `core_course_search_courses`
- `core_course_get_contents`
- `local_courseapi_get_activities`
- `core_course_get_course_module`
- `core_course_get_course_module_by_instance`
- `core_completion_get_course_completion_status`
- `core_completion_update_activity_completion_status_manually`
- `mod_resource_get_resources_by_courses`
- `mod_quiz_get_quizzes_by_courses`
- `core_grades_get_gradeitems`
- `gradereport_overview_get_course_grades`
- `gradereport_user_get_grades_table`

## AssignmentsRepository
- `mod_assign_get_assignments`
- `mod_assign_get_submission_status`
- `mod_assign_save_submission`
- `mod_assign_submit_for_grading`
- file upload through Moodle draft upload endpoint

## NotificationsRepository
- `message_popup_get_unread_popup_notification_count`
- `core_message_mark_notification_read`
- `core_calendar_get_action_events_by_timesort`
- `core_calendar_get_calendar_events`

## CertificatesRepository
- `local_certificateapi_get_issued_certificates`
- `local_thirdpartyapi_view_certificate`

## Leaderboard
- `local_leaderboardapi_get_ranking`

DTO/domain models were added in `domain/models/MoodleExtraModels.kt`; the older screen models were kept so the current ViewModels continue to compile with minimal UI changes.

package com.example.moodlegovapp.data.network

import com.example.moodlegovapp.data.service.DataStoreManager
import com.example.moodlegovapp.domain.models.ActivityDate
import com.example.moodlegovapp.domain.models.AssignmentSubmission
import com.example.moodlegovapp.domain.models.AuthToken
import com.example.moodlegovapp.domain.models.Badge
import com.example.moodlegovapp.domain.models.Certificate
import com.example.moodlegovapp.domain.models.Course
import com.example.moodlegovapp.domain.models.CourseSection
import com.example.moodlegovapp.domain.models.CourseModule

import com.example.moodlegovapp.domain.models.LeaderboardData
import com.example.moodlegovapp.domain.models.LeaderboardEntry
import com.example.moodlegovapp.domain.models.Notification
import com.example.moodlegovapp.domain.models.PerformanceOverview
import com.example.moodlegovapp.domain.models.SubmissionSaveResponse
import com.example.moodlegovapp.domain.models.TrainingEvent
import com.example.moodlegovapp.domain.models.TrainingStats
import com.example.moodlegovapp.domain.models.Performance
import com.example.moodlegovapp.domain.models.Settings
import com.example.moodlegovapp.domain.models.UserBadge
import com.example.moodlegovapp.domain.models.UserCertificate
import com.example.moodlegovapp.domain.models.UserProfile
import com.example.moodlegovapp.domain.models.UserResponse
import com.example.moodlegovapp.domain.models.toUserProfile
import com.google.gson.JsonElement
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RealApiService(
    private val retrofit: RetrofitApiService,
    private val dataStoreManager: DataStoreManager
) : ApiServiceProtocol {

    private fun userId() = dataStoreManager.userIdState.value?.toIntOrNull() ?: 101

    // ── Safe call helper ──────────────────────
    private suspend inline fun <T> safeCall(
        crossinline call: suspend () -> retrofit2.Response<T>
    ): AppResult<T> {
        return NetworkCallHandler.executeCall { call() }
    }

    private fun JsonElement.arrayAt(vararg keys: String): List<JsonElement> {
        if (isJsonArray) return asJsonArray.toList()
        if (!isJsonObject) return emptyList()
        val obj = asJsonObject
        for (key in keys) {
            val element = obj.get(key)
            if (element != null && element.isJsonArray) return element.asJsonArray.toList()
        }
        return emptyList()
    }

    private fun JsonElement.stringAt(key: String, fallback: String = ""): String {
        return if (isJsonObject && asJsonObject.has(key) && !asJsonObject.get(key).isJsonNull) {
            asJsonObject.get(key).asString
        } else fallback
    }

    private fun JsonElement.intAt(key: String, fallback: Int = 0): Int {
        return if (isJsonObject && asJsonObject.has(key) && !asJsonObject.get(key).isJsonNull) {
            runCatching { asJsonObject.get(key).asInt }.getOrDefault(fallback)
        } else fallback
    }


    private fun govModuleToCourseModule(a: com.example.moodlegovapp.domain.models.GovCourseActivityDto): CourseModule {
        val id = a.cmid ?: a.activityId ?: a.assignId ?: 0
        return CourseModule(
            id = id,
            url = null,
            name = a.activityTitle ?: "Activity",
            instance = a.assignId ?: id,
            contextId = 0,
            description = null,
            visible = 1,
            userVisible = true,
            visibleOnCoursePage = 1,
            modIcon = "",
            modName = a.modName ?: a.activityType ?: "activity",
            purpose = a.activityType ?: a.modName ?: "activity",
            branded = false,
            modPlural = a.activityType ?: a.modName ?: "activity",
            availability = null,
            indent = 0,
            onclick = "",
            afterLink = null,
            activityBadge = null,
            customData = "",
            noViewLink = false,
            canDisplay = true,
            completion = if ((a.activityStatus ?: "").contains("complete", true)) 1 else 0,
            completionData = null,
            downloadContent = 0,
            dates = listOfNotNull(a.activityDueDate?.let { ActivityDate("Due", it, "due") }),
            groupMode = 0,
            contents = emptyList(),
            contentsInfo = null
        )
    }

    private fun govOverviewToSections(dto: com.example.moodlegovapp.domain.models.GovCourseOverviewDto): List<CourseSection> {
        return dto.modules.mapIndexed { index, module ->
            CourseSection(
                id = module.moduleId ?: index + 1,
                name = module.moduleTitle ?: "Module ${index + 1}",
                visible = if (module.moduleLocked == 1) 0 else 1,
                summary = "",
                summaryFormat = 1,
                section = index + 1,
                hiddenByNumSections = 0,
                userVisible = module.moduleLocked != 1,
                component = null,
                itemId = null,
                modules = module.activities.map { govModuleToCourseModule(it) }
            )
        }
    }

    private fun govResourcesToMoodle(dto: com.example.moodlegovapp.domain.models.GovCourseResourcesDto, courseId: Int): List<com.example.moodlegovapp.domain.models.MoodleResource> {
        var idx = 1
        return dto.resourceGroups.flatMap { group ->
            group.files.map { f ->
                val fileUrl = f.url.orEmpty()
                com.example.moodlegovapp.domain.models.MoodleResource(
                    id = idx,
                    coursemodule = idx,
                    course = courseId,
                    name = f.name ?: "Resource",
                    section = 0,
                    visible = true,
                    contentFiles = listOf(
                        com.example.moodlegovapp.domain.models.MoodleResourceFile(
                            filename = f.name ?: "Resource",
                            filepath = "/",
                            filesize = ((f.sizeMb ?: 0.0) * 1024 * 1024).toLong(),
                            fileurl = fileUrl,
                            timemodified = 0L,
                            mimetype = when ((f.type ?: "").lowercase()) {
                                "pdf" -> "application/pdf"
                                "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                                else -> f.type ?: "application/octet-stream"
                            },
                            isexternalfile = false,
                            icon = ""
                        )
                    )
                ).also { idx++ }
            }
        }
    }

    private fun govTaskToAssignment(t: com.example.moodlegovapp.domain.models.GovTaskDto): com.example.moodlegovapp.domain.models.MoodleAssignment {
        return com.example.moodlegovapp.domain.models.MoodleAssignment(
            id = t.assignId ?: t.id ?: 0,
            cmid = t.cmid ?: 0,
            course = 0,
            name = t.title ?: "Task",
            grade = t.gradePercent ?: 0,
            intro = t.courseName ?: "",
            dueDate = t.dueDate ?: 0L
        )
    }

    private fun formatDateTime(timestampSeconds: Long): Pair<String, String> {
        if (timestampSeconds <= 0L) return "" to ""
        val date = Date(timestampSeconds * 1000)
        val dateText = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
        val timeText = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)
        return dateText to timeText
    }

    private fun govCourseToCourse(c: com.example.moodlegovapp.domain.models.GovCourseCardDto): Course {
        val courseId = c.courseId ?: c.id ?: 0
        val title = c.courseTitle ?: c.courseName ?: "Course"
        val progress = c.courseProgress ?: c.progress ?: 0
        return Course(
            id = courseId,
            shortName = title,
            fullName = title,
            displayName = title,
            courseImage = c.courseImage,
            progress = progress,
            completed = progress >= 100 || c.status.equals("completed", ignoreCase = true) || c.courseStatus.equals("completed", ignoreCase = true),
            hidden = false,
            showCompletionConditions = true
        )
    }

    private fun govEventToTrainingEvent(e: com.example.moodlegovapp.domain.models.GovCalendarEventDto): TrainingEvent {
        val ts = e.eventDatetime ?: e.timestart ?: e.time ?: 0L
        val (dateText, timeText) = formatDateTime(ts)
        return TrainingEvent(
            id = e.eventId ?: e.id ?: 0,
            title = e.eventTitle ?: e.title ?: e.name ?: "Upcoming event",
            type = e.eventType ?: e.type ?: "event",
            description = e.description ?: "",
            date = if (dateText.isNotBlank()) "$dateText · $timeText" else "",
            location = e.location ?: "",
            instructorName = e.eventInstructor ?: "",
            rawTimeStart = ts,
            courseId = e.courseId ?: 0,
            moduleName = e.courseName ?: e.course ?: ""
        )
    }

    private fun govProfileToUserProfile(dto: com.example.moodlegovapp.domain.models.GovProfileDto): UserProfile {
        val taskRate = (dto.taskCompletionRate ?: dto.assignmentCompletionRate ?: 0.0).toInt()
        val badges = dto.badges.mapIndexed { idx, b ->
            UserBadge(idx.toString(), b.badgeName ?: "Badge", b.badgeIcon ?: "", "")
        }
        val certificates = dto.certificates.mapIndexed { idx, c ->
            UserCertificate(
                id = idx.toString(),
                courseName = c.certificateTitle ?: "Certificate",
                instructorName = "N/A",
                status = c.certificateStatus ?: "pending",
                approvalStatus = c.certificateStatus ?: "pending",
                completedAtFormatted = (c.certificateDate ?: 0L).takeIf { it > 0 }?.let { formatDateTime(it).first } ?: "N/A",
                viewUrl = c.certificateDownloadUrl,
                downloadUrl = c.certificateDownloadUrl,
                isAvailable = !c.certificateDownloadUrl.isNullOrBlank(),
                pendingMessage = if (c.certificateDownloadUrl.isNullOrBlank()) "Pending approval" else null
            )
        }
        return UserProfile(
            id = userId(),
            fullName = dto.fullName ?: "Student User",
            email = dto.email ?: "",
            profileImageUrl = dto.avatarUrl ?: "",
            role = dto.userDesignation ?: "Student",
            department = dto.academyId ?: "",
            institution = "",
            batch = dto.batchNumber ?: "N/A",
            rank = (dto.cohortRank ?: 0).toString(),
            rankNumber = dto.cohortRank ?: 0,
            level = dto.userLevel ?: 1,
            totalXP = dto.totalXp ?: 0,
            xpToNextLevel = dto.xpToNextLevel ?: 0,
            xpProgressPercent = 0,
            performance = Performance(
                overallProgress = dto.overallProgress ?: 0,
                overallProgressLabel = "${dto.overallProgress ?: 0}%",
                averageGrade = dto.averageGrade ?: 0,
                averageGradeLabel = "${dto.averageGrade ?: 0}%",
                taskCompletion = taskRate,
                taskCompletionLabel = "$taskRate%"
            ),
            badges = badges,
            certificates = certificates,
            settings = Settings(dto.userLocale ?: "en",
                (dto.notifAssignments != false) || (dto.notifCourses != false) || (dto.notifAnnouncements != false) || (dto.notifCertificates != false))
        )
    }


    // ── AUTH ──────────────────────────────────
    override suspend fun login(
        username: String,
        password: String
    ): AppResult<AuthToken> {

        return when (
            val result = safeCall {
                retrofit.loginToken(
                    username = username,
                    password = password,
                    service = "govlms_mobile"
                )
            }
        ) {
            is AppResult.Success -> {
                dataStoreManager.save(
                    DataStoreManager.KEY_USERNAME,
                    username
                )

                AppResult.Success(result.data)
            }

            is AppResult.Failure -> result
            is AppResult.Loading -> AppResult.Loading
        }
    }

    override suspend fun requestPasswordReset(email: String): AppResult<com.example.moodlegovapp.domain.models.PasswordResetResult> =
        safeCall { retrofit.requestPasswordReset(email) }

    override suspend fun getSignupSettings(): AppResult<com.example.moodlegovapp.domain.models.SignupSettings> =
        safeCall { retrofit.getSignupSettings() }

    override suspend fun getSiteInfo(): AppResult<com.example.moodlegovapp.domain.models.SiteInfo> =
        safeCall { retrofit.getSiteInfo() }

    // ── USER ──────────────────────────────────
    override suspend fun getUserProfile(): AppResult<UserProfile> {
        return when (val result = safeCall { retrofit.getGovUserProfile() }) {
            is AppResult.Success -> AppResult.Success(govProfileToUserProfile(result.data))
            is AppResult.Failure -> {
                val username = dataStoreManager.get<String>(DataStoreManager.Companion.KEY_USERNAME) ?: "test.student1"
                when (val fallback = safeCall { retrofit.getUserByField(value = username) }) {
                    is AppResult.Success -> {
                        val studentUser = fallback.data.firstOrNull()
                        if (studentUser != null) {
                            studentUser.id?.let { dataStoreManager.save(DataStoreManager.Companion.KEY_USER_ID, it.toString()) }
                            AppResult.Success(studentUser.toUserProfile())
                        } else AppResult.Failure(AppError.DecodingError)
                    }
                    is AppResult.Failure -> fallback
                    is AppResult.Loading -> AppResult.Loading
                }
            }
            is AppResult.Loading -> AppResult.Loading
        }
    }

    override suspend fun getPerformanceOverview(): AppResult<PerformanceOverview> {
        return when (val r = safeCall { retrofit.getGovDashboard("all") }) {
            is AppResult.Success -> AppResult.Success(
                PerformanceOverview(
                    overallProgress = r.data.overallProgress ?: 0,
                    averageGrade = r.data.averageGrade ?: 0,
                    taskCompletion = (r.data.assignmentCompletionRate ?: 0.0).toInt()
                )
            )
            is AppResult.Failure -> safeCall { retrofit.getPerformanceOverview(userId()) }
            is AppResult.Loading -> AppResult.Loading
        }
    }

    override suspend fun getUserPreferences(userId: Int?): AppResult<List<com.example.moodlegovapp.domain.models.UserPreference>> {
        val finalUserId = userId ?: this.userId()
        return when (val result = safeCall { retrofit.getUserPreferences(finalUserId) }) {
            is AppResult.Success -> AppResult.Success(result.data.preferences)
            is AppResult.Failure -> result
            is AppResult.Loading -> AppResult.Loading
        }
    }

    override suspend fun updatePreference(type: String, value: String): AppResult<com.example.moodlegovapp.domain.models.PreferenceUpdateResult> =
        safeCall { retrofit.updatePreference(type, value) }

    override suspend fun updateUserPicture(draftItemId: Long, delete: Boolean): AppResult<com.example.moodlegovapp.domain.models.UserPictureUpdateResult> =
        safeCall { retrofit.updateUserPicture(draftItemId, if (delete) 1 else 0) }

    // ── COURSES ───────────────────────────────
    override suspend fun getEnrolledCourses(): AppResult<List<Course>> {
        return when (val r = safeCall { retrofit.getGovDashboard("all") }) {
            is AppResult.Success -> AppResult.Success((r.data.inProgressCourses + r.data.completedCourses).map { govCourseToCourse(it) })
            is AppResult.Failure -> {
                val cachedId = dataStoreManager.userIdState.value?.toIntOrNull() ?: userId()
                safeCall { retrofit.getEnrolledCourses(cachedId) }
            }
            is AppResult.Loading -> AppResult.Loading
        }
    }

    override suspend fun getAllCourses(): AppResult<List<Course>> =
        safeCall { retrofit.getAllCourses() }

    override suspend fun getCourseContents(courseId: Int): AppResult<List<CourseSection>> {
        return when (val result = safeCall { retrofit.getGovCourseOverview(courseId) }) {
            is AppResult.Success -> AppResult.Success(govOverviewToSections(result.data))
            is AppResult.Failure -> result
            is AppResult.Loading -> AppResult.Loading
        }
    }



    override suspend fun getCourseModules(courseId: Int): AppResult<List<CourseModule>> {
        return when (val result = getCourseContents(courseId)) {
            is AppResult.Success -> AppResult.Success(result.data.flatMap { it.modules })
            is AppResult.Failure -> result
            is AppResult.Loading -> AppResult.Loading
        }
    }


    override suspend fun getCourseModule(cmid: Int): AppResult<com.example.moodlegovapp.domain.models.MoodleCourseModuleDetail> {
        return when (val result = safeCall { retrofit.getCourseModule(cmid) }) {
            is AppResult.Success -> result.data.cm?.let { AppResult.Success(it) } ?: AppResult.Failure(AppError.NotFound)
            is AppResult.Failure -> result
            is AppResult.Loading -> AppResult.Loading
        }
    }

    override suspend fun getCourseModuleByInstance(module: String, instance: Int): AppResult<com.example.moodlegovapp.domain.models.MoodleCourseModuleDetail> {
        return when (val result = safeCall { retrofit.getCourseModuleByInstance(module, instance) }) {
            is AppResult.Success -> result.data.cm?.let { AppResult.Success(it) } ?: AppResult.Failure(AppError.NotFound)
            is AppResult.Failure -> result
            is AppResult.Loading -> AppResult.Loading
        }
    }

    override suspend fun getCourseActivities(courseId: Int): AppResult<List<com.example.moodlegovapp.domain.models.MoodleActivity>> {
        return when (val result = safeCall { retrofit.getCourseActivities(courseId) }) {
            is AppResult.Success -> AppResult.Success(result.data.activities)
            is AppResult.Failure -> result
            is AppResult.Loading -> AppResult.Loading
        }
    }

    override suspend fun getCourseCompletionStatus(courseId: Int, userId: Int?): AppResult<com.example.moodlegovapp.domain.models.CourseCompletionStatus> {
        return when (val result = safeCall { retrofit.getCourseCompletionStatus(courseId, userId ?: this.userId()) }) {
            is AppResult.Success -> AppResult.Success(result.data.completionstatus)
            is AppResult.Failure -> result
            is AppResult.Loading -> AppResult.Loading
        }
    }

    override suspend fun getGradeItems(courseId: Int): AppResult<List<com.example.moodlegovapp.domain.models.GradeItem>> {
        return when (val result = safeCall { retrofit.getGradeItems(courseId) }) {
            is AppResult.Success -> AppResult.Success(result.data.gradeItems)
            is AppResult.Failure -> result
            is AppResult.Loading -> AppResult.Loading
        }
    }

    override suspend fun getOverviewCourseGrades(userId: Int?): AppResult<List<com.example.moodlegovapp.domain.models.CourseGrade>> {
        return when (val result = safeCall { retrofit.getOverviewCourseGrades(userId ?: this.userId()) }) {
            is AppResult.Success -> AppResult.Success(result.data.grades)
            is AppResult.Failure -> result
            is AppResult.Loading -> AppResult.Loading
        }
    }

    override suspend fun getUserGradesTable(courseId: Int, userId: Int?): AppResult<List<com.example.moodlegovapp.domain.models.GradeTable>> {
        return when (val result = safeCall { retrofit.getUserGradesTable(courseId, userId ?: this.userId()) }) {
            is AppResult.Success -> AppResult.Success(result.data.tables)
            is AppResult.Failure -> result
            is AppResult.Loading -> AppResult.Loading
        }
    }

    override suspend fun getQuizzes(courseId: Int): AppResult<List<com.example.moodlegovapp.domain.models.MoodleQuiz>> {
        return when (val result = safeCall { retrofit.getQuizzesByCourses(courseId) }) {
            is AppResult.Success -> AppResult.Success(result.data.courses.flatMap { it.quizzes })
            is AppResult.Failure -> result
            is AppResult.Loading -> AppResult.Loading
        }
    }

    override suspend fun getAssignments(courseId: Int): AppResult<List<com.example.moodlegovapp.domain.models.MoodleAssignment>> {
        return when (val r = safeCall { retrofit.getGovTasks() }) {
            is AppResult.Success -> AppResult.Success(r.data.tasks.map { govTaskToAssignment(it) })
            is AppResult.Failure -> r
            is AppResult.Loading -> AppResult.Loading
        }
    }

    override suspend fun getAssignmentDetail(assignmentId: Int): AppResult<com.example.moodlegovapp.domain.models.MoodleAssignment> {
        return when (val r = safeCall { retrofit.getGovTasks() }) {
            is AppResult.Success -> r.data.tasks.firstOrNull { (it.assignId ?: it.id ?: 0) == assignmentId }
                ?.let { AppResult.Success(govTaskToAssignment(it)) }
                ?: AppResult.Failure(AppError.NotFound)
            is AppResult.Failure -> r
            is AppResult.Loading -> AppResult.Loading
        }
    }

    override suspend fun submitAssignment(submission: AssignmentSubmission): AppResult<Unit> {
        return when (val result = safeCall<SubmissionSaveResponse> {
            retrofit.saveSubmission(submission.assignmentId, userId(), submission)
        }) {
            is AppResult.Success -> AppResult.Success(Unit)
            is AppResult.Failure -> result  // propagate
            is AppResult.Loading -> AppResult.Loading
        }
    }

// ── COURSE RESOURCES ──────────────────────────────────────────────────────────

    override suspend fun getCourseResources(courseId: Int): AppResult<List<com.example.moodlegovapp.domain.models.MoodleResource>> {
        return when (val r = safeCall { retrofit.getGovCourseResources(courseId) }) {
            is AppResult.Success -> AppResult.Success(govResourcesToMoodle(r.data, courseId))
            is AppResult.Failure -> r
            is AppResult.Loading -> AppResult.Loading
        }
    }
    // ── NOTIFICATIONS / CALENDAR EVENTS ───────
    override suspend fun getNotifications(): AppResult<List<Notification>> {
        return when (val result = safeCall { retrofit.getGovNotifications(null) }) {
            is AppResult.Success -> {
                val mapped = result.data.notifications.map { n ->
                    val time = n.time ?: 0L
                    val (dateText, timeText) = formatDateTime(time)
                    Notification(
                        id = n.id ?: 0,
                        title = n.title ?: "Notification",
                        body = n.body ?: "",
                        notificationType = n.type ?: "notification",
                        shortBody = n.body ?: "",
                        type = n.type ?: "notification",
                        read = n.isRead ?: false,
                        createdAt = time.toString(),
                        createdAtFormatted = if (dateText.isNotBlank()) "$dateText · $timeText" else "",
                        deepLink = "",
                        iconType = n.type ?: "notification",
                        courseName = "",
                        sessionDate = dateText,
                        sessionTime = timeText,
                        location = ""
                    )
                }
                AppResult.Success(mapped)
            }
            is AppResult.Failure -> result
            is AppResult.Loading -> AppResult.Loading
        }
    }

    override suspend fun markNotificationRead(notificationId: Int): AppResult<Unit> {
        return when (val result = safeCall { retrofit.markAllGovNotificationsRead() }) {
            is AppResult.Success -> AppResult.Success(Unit)
            is AppResult.Failure -> result
            is AppResult.Loading -> AppResult.Loading
        }
    }

    override suspend fun getUnreadNotificationCount(): AppResult<Int> {
        return when (val result = safeCall { retrofit.getGovNotifications(1) }) {
            is AppResult.Success -> AppResult.Success(result.data.totalUnread ?: result.data.notifications.size)
            is AppResult.Failure -> result
            is AppResult.Loading -> AppResult.Loading
        }
    }

    override suspend fun getActionEventsByTimesort(from: Long, to: Long, limit: Int): AppResult<List<TrainingEvent>> {
        return when (val result = safeCall { retrofit.getGovCalendarEvents(limit = limit, timeStart = from, timeEnd = to) }) {
            is AppResult.Success -> AppResult.Success(result.data.events.map { govEventToTrainingEvent(it) }.sortedBy { it.rawTimeStart })
            is AppResult.Failure -> result
            is AppResult.Loading -> AppResult.Loading
        }
    }

    // ── CERTIFICATES ──────────────────────────
    override suspend fun getCertificates(): AppResult<List<Certificate>> {
        return when (val result = safeCall { retrofit.getGovUserProfile() }) {
            is AppResult.Success -> {
                val mapped = result.data.certificates.mapIndexed { index, item ->
                    Certificate(
                        id = index,
                        courseTitle = item.certificateTitle ?: "Certificate",
                        instructorName = "N/A",
                        completionDate = (item.certificateDate ?: 0L).toString(),
                        grade = 0,
                        certificateUrl = item.certificateDownloadUrl ?: "",
                        canDownload = !item.certificateDownloadUrl.isNullOrBlank()
                    )
                }
                AppResult.Success(mapped)
            }
            is AppResult.Failure -> AppResult.Success(emptyList())
            is AppResult.Loading -> AppResult.Loading
        }
    }

    override suspend fun getCertificateDownloadUrl(certificateId: Int): AppResult<String> {
        return AppResult.Success("${NetworkConfig.BASE_URL}/webservice/rest/server.php?moodlewsrestformat=json&wstoken=${NetworkConfig.WS_TOKEN}&wsfunction=local_thirdpartyapi_view_certificate&cmid=$certificateId")
    }

    override suspend fun viewCertificate(cmid: Int): AppResult<com.example.moodlegovapp.domain.models.CertificateViewResponse> =
        safeCall { retrofit.viewCertificate(cmid) }

    // ── LEADERBOARD ───────────────────────────
    override suspend fun getLeaderboard(courseId: Int): AppResult<LeaderboardData> {
        return when (val result = safeCall { retrofit.getGovDashboard("all") }) {
            is AppResult.Success -> {
                val currentUserId = userId()
                val entries = result.data.leaderboard.map { item ->
                    val uid = item.leaderboardUserId ?: item.userid ?: 0
                    LeaderboardEntry(
                        rank = item.leaderboardRank ?: item.rank ?: 0,
                        userId = uid,
                        fullName = item.leaderboardUserName ?: item.fullname ?: "Student",
                        profileImageUrl = item.leaderboardUserAvatar ?: item.avatar ?: "",
                        xp = item.leaderboardXp ?: item.xp ?: item.points ?: 0,
                        level = 1,
                        course = null,
                        isCurrentUser = uid != 0 && uid == currentUserId,
                        xpThisWeek = null
                    )
                }
                AppResult.Success(LeaderboardData(entries.firstOrNull { it.isCurrentUser }?.rank ?: (result.data.myRank ?: 0), entries.size, entries))
            }
            is AppResult.Failure -> AppResult.Success(LeaderboardData())
            is AppResult.Loading -> AppResult.Loading
        }
    }

    // ── BADGES ────────────────────────────────
    override suspend fun getBadges(): AppResult<List<Badge>> {
        return when (val r = safeCall { retrofit.getGovDashboard("all") }) {
            is AppResult.Success -> AppResult.Success((r.data.userBadges + r.data.badges).mapIndexed { idx, b ->
                Badge(id = idx, title = b.badgeName ?: "Badge", iconUrl = b.badgeIcon ?: "", isEarned = true)
            })
            is AppResult.Failure -> AppResult.Success(emptyList())
            is AppResult.Loading -> AppResult.Loading
        }
    }

    // ── EVENTS ────────────────────────────────
    override suspend fun getUpcomingEvents(): AppResult<List<TrainingEvent>> {
        return when (val result = safeCall { retrofit.getGovCalendarEvents(limit = 50) }) {
            is AppResult.Success -> AppResult.Success(result.data.events.map { govEventToTrainingEvent(it) }.sortedBy { it.rawTimeStart })
            is AppResult.Failure -> AppResult.Success(emptyList())
            is AppResult.Loading -> AppResult.Loading
        }
    }

    // ── STATS ─────────────────────────────────
    override suspend fun getTrainingStats(): AppResult<TrainingStats> {
        return safeCall { retrofit.getTrainingStats(userId()) }
    }

    // ── SEARCH ────────────────────────────────
    override suspend fun searchCourses(query: String): AppResult<List<Course>> {
        if (query.isBlank()) return getEnrolledCourses()
        return when (val result = safeCall { retrofit.searchCoursesRemote(query) }) {
            is AppResult.Success -> AppResult.Success(result.data.courses)
            is AppResult.Failure -> {
                when (val local = getEnrolledCourses()) {
                    is AppResult.Success -> AppResult.Success(local.data.filter { it.fullName?.contains(query, ignoreCase = true) == true || it.shortName?.contains(query, ignoreCase = true) == true })
                    is AppResult.Failure -> result
                    is AppResult.Loading -> AppResult.Loading
                }
            }
            is AppResult.Loading -> AppResult.Loading
        }
    }

    // ── ACTIVITY COMPLETION ───────────────────
    override suspend fun updateActivityCompletion(activityId: Int, completed: Boolean): AppResult<Unit> {
        return when (val result = safeCall { retrofit.updateActivityCompletionRemote(activityId, if (completed) 1 else 0) }) {
            is AppResult.Success -> AppResult.Success(Unit)
            is AppResult.Failure -> result
            is AppResult.Loading -> AppResult.Loading
        }
    }
}

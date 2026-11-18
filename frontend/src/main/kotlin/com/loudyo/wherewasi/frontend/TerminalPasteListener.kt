package com.loudyo.wherewasi.frontend

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.platform.project.projectId
import com.loudyo.wherewasi.shared.TerminalPasteApi
import fleet.rpc.client.durable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Subscribes to the backend's paste requests for as long as the project is open on this frontend. */
@Service(Service.Level.PROJECT)
internal class TerminalPasteListener(private val project: Project, private val scope: CoroutineScope) {

    fun start() {
        scope.launch {
            // `durable` re-subscribes if the connection to the backend drops and comes back.
            durable {
                TerminalPasteApi.getInstance().pasteRequests(project.projectId()).collect { text ->
                    withContext(Dispatchers.EDT) { paste(text) }
                }
            }
        }
    }

    private fun paste(text: String) {
        if (project.isDisposed || TerminalSender.sendToActiveTerminal(project, text)) return
        NotificationGroupManager.getInstance()
            .getNotificationGroup(NOTIFICATION_GROUP_ID)
            .createNotification("No active Terminal tab found. Open a terminal tab and try again.", NotificationType.WARNING)
            .notify(project)
    }

    private companion object {
        const val NOTIFICATION_GROUP_ID = "WhereWasI"
    }
}

internal class TerminalPasteListenerStarter : ProjectActivity {
    override suspend fun execute(project: Project) {
        project.service<TerminalPasteListener>().start()
    }
}

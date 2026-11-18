package com.loudyo.wherewasi.backend

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.platform.project.ProjectId
import com.intellij.platform.project.findProjectOrNull
import com.intellij.platform.rpc.backend.RemoteApiProvider
import com.loudyo.wherewasi.shared.TerminalPasteApi
import fleet.rpc.remoteApiDescriptor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.emptyFlow

/** Per-project stream of texts to paste; the frontend collects it through [TerminalPasteApi]. */
@Service(Service.Level.PROJECT)
internal class TerminalPasteRequests {
    private val requests = MutableSharedFlow<String>(extraBufferCapacity = 16)

    val flow: Flow<String> = requests.asSharedFlow()

    fun send(text: String): Boolean {
        if (!hasListeners) logger<TerminalPasteRequests>().warn("No frontend is subscribed; the reference won't reach the Terminal")
        return requests.tryEmit(text)
    }

    /** No frontend is listening, e.g. the client hasn't connected its subscription yet. */
    val hasListeners: Boolean get() = requests.subscriptionCount.value > 0

    companion object {
        fun getInstance(project: Project): TerminalPasteRequests = project.service()
    }
}

internal class TerminalPasteApiImpl : TerminalPasteApi {
    override suspend fun pasteRequests(projectId: ProjectId): Flow<String> {
        val project = projectId.findProjectOrNull() ?: return emptyFlow()
        logger<TerminalPasteApiImpl>().info("Frontend subscribed to terminal paste requests for ${project.name}")
        return TerminalPasteRequests.getInstance(project).flow
    }
}

internal class TerminalPasteApiProvider : RemoteApiProvider {
    override fun RemoteApiProvider.Sink.remoteApis() {
        remoteApi(remoteApiDescriptor<TerminalPasteApi>()) {
            TerminalPasteApiImpl()
        }
    }
}

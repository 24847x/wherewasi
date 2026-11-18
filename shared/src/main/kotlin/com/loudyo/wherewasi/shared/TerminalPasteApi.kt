package com.loudyo.wherewasi.shared

import com.intellij.platform.project.ProjectId
import com.intellij.platform.rpc.RemoteApiProviderService
import fleet.rpc.RemoteApi
import fleet.rpc.Rpc
import fleet.rpc.remoteApiDescriptor
import kotlinx.coroutines.flow.Flow

/**
 * Frontend ↔ backend API. The action runs on the backend, where the editors, VFS and project paths are.
 * The Terminal UI ([com.intellij.terminal.frontend.view.TerminalView]) is on the frontend, so the frontend
 * subscribes to [pasteRequests] and pastes each text it receives. In a regular (monolithic) IDE both ends
 * run in the same process.
 */
@Rpc
interface TerminalPasteApi : RemoteApi<Unit> {

    /** Texts the backend action wants pasted into the active Terminal tab of [projectId]. */
    suspend fun pasteRequests(projectId: ProjectId): Flow<String>

    companion object {
        suspend fun getInstance(): TerminalPasteApi =
            RemoteApiProviderService.resolve(remoteApiDescriptor<TerminalPasteApi>())
    }
}

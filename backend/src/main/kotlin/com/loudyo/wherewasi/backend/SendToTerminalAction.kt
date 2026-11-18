package com.loudyo.wherewasi.backend

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile

class SendToTerminalAction : AnAction() {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR)
        val editorFile = editorFile(e)
        val refs = if (editor != null && editorFile != null) {
            listOf(fileReference(project, editorFile, selectedLines(editor)))
        } else {
            // Project view (or another file tree): one reference per selected file/folder.
            selectedFiles(e).map { fileReference(project, it, lines = null) }.takeIf { it.isNotEmpty() } ?: return
        }
        // The Terminal UI is on the frontend (JetBrains Client in remote development), which subscribes to these.
        TerminalPasteRequests.getInstance(project).send(formatReferences(refs))
    }

    /**
     * A file or folder to mention; [lines] is the 1-based selected line range, if any.
     */
    private data class Reference(
        val path: String,
        val isDirectory: Boolean,
        val lines: IntRange?,
    )

    private fun editorFile(e: AnActionEvent): VirtualFile? =
        e.getData(CommonDataKeys.VIRTUAL_FILE)?.takeIf { it.isInLocalFileSystem }

    private fun selectedFiles(e: AnActionEvent): List<VirtualFile> =
        e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY).orEmpty().filter { it.isInLocalFileSystem }

    /** Path relative to the project, falling back to the name for files outside it. */
    private fun fileReference(project: Project, file: VirtualFile, lines: IntRange?): Reference {
        val path = project.guessProjectDir()?.let { VfsUtilCore.getRelativePath(file, it) }?.takeIf { it.isNotEmpty() }
            ?: file.name
        return Reference(path, file.isDirectory, lines)
    }

    private fun selectedLines(editor: Editor): IntRange? {
        val selection = editor.selectionModel
        val doc = editor.document
        if (!selection.hasSelection()) {
            val line = doc.getLineNumber(editor.caretModel.primaryCaret.offset) + 1
            return line..line
        }

        val startLine = doc.getLineNumber(selection.selectionStart) + 1
        // A selection ending at column 0 doesn't include that line.
        val endOffset = selection.selectionEnd
        var endLine = doc.getLineNumber(endOffset) + 1
        if (endLine > startLine && endOffset == doc.getLineStartOffset(endLine - 1)) endLine--
        return startLine..endLine
    }

    /** Terminal reference format: `@src/File.kt#L42-58`, `@src/File.kt#L42`, or `@src/folder/`. */
    private fun formatReferences(refs: List<Reference>): String =
        refs.joinToString(separator = " ", postfix = " ") { ref ->
            val lines = ref.lines
            when {
                ref.isDirectory -> "@${ref.path}/"
                lines == null -> "@${ref.path}"
                lines.first == lines.last -> "@${ref.path}#L${lines.first}"
                else -> "@${ref.path}#L${lines.first}-${lines.last}"
            }
        }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null && (
            (e.getData(CommonDataKeys.EDITOR) != null && editorFile(e) != null) ||
                selectedFiles(e).isNotEmpty()
            )
    }
}

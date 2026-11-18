package com.loudyo.wherewasi.frontend

import com.intellij.ide.DataManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.terminal.frontend.view.TerminalView
import com.intellij.util.ui.UIUtil
import javax.swing.JComponent

internal object TerminalSender {

    fun sendToActiveTerminal(project: Project, text: String): Boolean {
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow(TERMINAL_TOOL_WINDOW_ID)
            ?: return false
        val content = toolWindow.contentManager.selectedContent ?: return false
        val view = findTerminalView(content.component, content.preferredFocusableComponent) ?: return false

        toolWindow.activate({
            view.createSendTextBuilder().useBracketedPasteMode().send(text)
            view.preferredFocusableComponent.requestFocusInWindow()
        }, true)
        return true
    }

    private fun findTerminalView(root: JComponent, preferredFocused: JComponent?): TerminalView? {
        val candidates = sequence {
            listOfNotNull(preferredFocused, root).forEach { yield(it) }
            UIUtil.findComponentsOfType(root, JComponent::class.java).forEach { yield(it) }
        }
        return candidates.firstNotNullOfOrNull { component ->
            DataManager.getInstance().getDataContext(component).getData(TerminalView.DATA_KEY)
        }
    }

    private const val TERMINAL_TOOL_WINDOW_ID = "Terminal"
}

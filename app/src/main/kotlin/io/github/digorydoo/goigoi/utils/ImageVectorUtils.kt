package io.github.digorydoo.goigoi.utils

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorGroup
import androidx.compose.ui.graphics.vector.VectorNode
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.graphics.vector.toPath

fun ImageVector.toPath(): Path {
    val path = Path()

    fun traverse(node: VectorNode) {
        when (node) {
            is VectorPath -> {
                val subPath = Path()
                node.pathData.toPath(subPath)
                path.addPath(subPath)
            }
            is VectorGroup -> {
                for (child in node) {
                    traverse(child)
                }
            }
        }
    }

    traverse(root)
    return path
}

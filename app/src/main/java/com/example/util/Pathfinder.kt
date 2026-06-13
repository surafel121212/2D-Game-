package com.example.util

import java.util.PriorityQueue
import kotlin.math.abs

data class GridPoint(val x: Int, val y: Int)

class Pathfinder(
    private val width: Int,
    private val height: Int,
    private val isWalkable: (x: Int, y: Int) -> Boolean
) {
    private data class Node(
        val point: GridPoint,
        val g: Int,
        val h: Int,
        val parent: Node? = null
    ) : Comparable<Node> {
        val f: Int get() = g + h
        override fun compareTo(other: Node): Int = this.f.compareTo(other.f)
    }

    private fun heuristic(a: GridPoint, b: GridPoint): Int {
        return abs(a.x - b.x) + abs(a.y - b.y)
    }

    fun findPath(start: GridPoint, end: GridPoint): List<GridPoint> {
        // Bounds checking
        if (start.x !in 0 until width || start.y !in 0 until height) return emptyList()
        if (end.x !in 0 until width || end.y !in 0 until height) return emptyList()
        if (!isWalkable(end.x, end.y)) return emptyList()

        val openSet = PriorityQueue<Node>()
        val closedSet = mutableSetOf<GridPoint>()

        openSet.add(Node(start, 0, heuristic(start, end)))

        while (openSet.isNotEmpty()) {
            val current = openSet.poll() ?: break

            if (current.point == end) {
                // Reconstruct path
                val path = mutableListOf<GridPoint>()
                var node: Node? = current
                while (node != null) {
                    path.add(0, node.point)
                    node = node.parent
                }
                return path
            }

            closedSet.add(current.point)

            // 4-directional or 8-directional layout (let's do 4-directional for perfect maze grid flow)
            val neighbors = listOf(
                GridPoint(current.point.x + 1, current.point.y),
                GridPoint(current.point.x - 1, current.point.y),
                GridPoint(current.point.x, current.point.y + 1),
                GridPoint(current.point.x, current.point.y - 1)
            )

            for (neighbor in neighbors) {
                if (neighbor.x !in 0 until width || neighbor.y !in 0 until height) continue
                if (!isWalkable(neighbor.x, neighbor.y)) continue
                if (neighbor in closedSet) continue

                val newG = current.g + 1
                val newH = heuristic(neighbor, end)

                // Check if neighbor is already in open list with a lower cost
                val existing = openSet.find { it.point == neighbor }
                if (existing == null || existing.g > newG) {
                    if (existing != null) {
                        openSet.remove(existing)
                    }
                    openSet.add(Node(neighbor, newG, newH, current))
                }
            }
        }

        return emptyList()
    }
}

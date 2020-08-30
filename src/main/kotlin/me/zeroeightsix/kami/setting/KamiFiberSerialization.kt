package me.zeroeightsix.kami.setting

import io.github.fablabsmc.fablabs.api.fiber.v1.exception.ValueDeserializationException
import io.github.fablabsmc.fablabs.api.fiber.v1.serialization.ValueSerializer
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.*
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

/**
 * A copy of [io.github.fablabsmc.fablabs.api.fiber.v1.serialization.FiberSerialization] that fails softly if an exception occurs while deserialising a value
 */
object KamiFiberSerialization {
    @Throws(IOException::class)
    fun <A, T> serialize(tree: ConfigTree, out: OutputStream?, ctx: ValueSerializer<A, T>) {
        val target = ctx.newTarget()
        for (node in tree.items) {
            serializeNode(node, target, ctx)
        }
        ctx.writeTarget(target, out)
    }

    @Throws(IOException::class, ValueDeserializationException::class)
    fun <A, T> deserialize(tree: ConfigTree, `in`: InputStream?, ctx: ValueSerializer<A, T>) {
        val target = ctx.readTarget(`in`)
        val itr = ctx.elements(target)
        while (itr.hasNext()) {
            val entry = itr.next()
            val node = tree.lookup(entry.key)
            val elem = entry.value
            if (node != null) {
                deserializeNode(node, elem, ctx)
            }
        }
    }

    fun <A, T> serializeNode(node: ConfigNode, target: T, ctx: ValueSerializer<A, T>) {
        val name = Objects.requireNonNull(node.name)
        val comment: String?
        comment = if (node is Commentable) {
            (node as Commentable).comment
        } else {
            null
        }
        if (node is ConfigBranch) {
            val branch = node
            if (!branch.isSerializedSeparately) {
                val subTarget = ctx.newTarget()
                for (subNode in branch.items) {
                    serializeNode(subNode, subTarget, ctx)
                }
                ctx.addSubElement(name, subTarget, target, comment)
            }
        } else if (node is ConfigLeaf<*>) {
            ctx.addElement(name, serializeValue(node, ctx), target, comment)
        }
    }

    private fun <T, A> serializeValue(leaf: ConfigLeaf<T>, ctx: ValueSerializer<A, *>): A {
        return leaf.configType.serializeValue(leaf.value, ctx)
    }

    @Throws(ValueDeserializationException::class)
    fun <A, T> deserializeNode(node: ConfigNode?, elem: A, ctx: ValueSerializer<A, T>) {
        if (node is ConfigBranch) {
            val itr = ctx.subElements(elem)
            while (itr.hasNext()) {
                val entry = itr.next()
                val subNode = node.lookup(entry.key)
                val subElem = entry.value
                if (subNode != null) {
                    deserializeNode(subNode, subElem, ctx)
                }
            }
        } else if (node is ConfigLeaf<*>) {
            deserializeValue(node, elem, ctx)
        }
    }

    @Throws(ValueDeserializationException::class)
    private fun <T, A> deserializeValue(leaf: ConfigLeaf<T>, elem: A, ctx: ValueSerializer<A, *>) {
        try {
            leaf.value = leaf.configType.deserializeValue(elem, ctx)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

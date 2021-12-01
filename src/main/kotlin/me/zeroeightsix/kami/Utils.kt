package me.zeroeightsix.kami

import imgui.ImGui
import imgui.ImGui.popStyleVar
import imgui.ImGui.pushStyleVar
import imgui.flag.ImGuiStyleVar
import imgui.internal.ImGui.popItemFlag
import imgui.internal.ImGui.pushItemFlag
import imgui.internal.flag.ImGuiItemFlags
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigBranch
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigLeaf
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigNode
import me.zeroeightsix.kami.mixin.client.`IMatrixStack$Entry`
import me.zeroeightsix.kami.mixin.extend.a00
import me.zeroeightsix.kami.mixin.extend.a01
import me.zeroeightsix.kami.mixin.extend.a02
import me.zeroeightsix.kami.mixin.extend.a03
import me.zeroeightsix.kami.mixin.extend.a10
import me.zeroeightsix.kami.mixin.extend.a11
import me.zeroeightsix.kami.mixin.extend.a12
import me.zeroeightsix.kami.mixin.extend.a13
import me.zeroeightsix.kami.mixin.extend.a20
import me.zeroeightsix.kami.mixin.extend.a21
import me.zeroeightsix.kami.mixin.extend.a22
import me.zeroeightsix.kami.mixin.extend.a23
import me.zeroeightsix.kami.mixin.extend.a30
import me.zeroeightsix.kami.mixin.extend.a31
import me.zeroeightsix.kami.mixin.extend.a32
import me.zeroeightsix.kami.mixin.extend.a33
import me.zeroeightsix.kami.mixin.extend.getStack
import me.zeroeightsix.kami.util.Friends
import me.zeroeightsix.kami.util.minus
import me.zeroeightsix.kami.util.plus
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.AbstractFurnaceScreen
import net.minecraft.client.gui.screen.ingame.BookEditScreen
import net.minecraft.client.gui.screen.ingame.CraftingScreen
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen
import net.minecraft.client.gui.screen.ingame.SignEditScreen
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.predicate.item.ItemPredicate
import net.minecraft.util.math.Matrix3f
import net.minecraft.util.math.Matrix4f
import net.minecraft.util.math.Quaternion
import net.minecraft.util.math.Vec3d
import java.util.Optional
import java.util.stream.Stream
import kotlin.reflect.KMutableProperty0

val mc: MinecraftClient = MinecraftClient.getInstance()

// / Quality of life and primitive extensions

fun <T> tryOrNull(block: () -> T): T? = try {
    block()
} catch (e: Exception) {
    null
}

val <T> Optional<T>.kotlin
    get() = if (this.isPresent) this.get() else null

/**
 * If `true`, compute a value. Else, return `null`.
 *
 * Use only where the return value is used & makes sense. This method isn't supposed to be a cool if statement.
 */
infix fun <T> Boolean.then(block: () -> T): T? {
    if (this) return block()
    return null
}

fun Boolean.conditionalWrap(before: () -> Unit, during: () -> Unit, after: () -> Unit) {
    if (this) before()
    during()
    if (this) after()
}

fun wrapDisabled(conditional: Boolean, during: () -> Unit) {
    conditional.conditionalWrap(
        {
            pushItemFlag(ImGuiItemFlags.Disabled, true)
            pushStyleVar(ImGuiStyleVar.Alpha, ImGui.getStyle().alpha * 0.5f)
        },
        during,
        {
            popItemFlag()
            popStyleVar()
        }
    )
}

fun String.splitFirst(char: Char): Pair<String, String> {
    val index = this.indexOf(char)
    return Pair(this.substring(0, index), this.substring(index + 1))
}

inline fun <T> KMutableProperty0<T>.tempSet(value: T, block: () -> Unit) {
    val old = get()
    set(value)
    block()
    set(old)
}

@ExperimentalUnsignedTypes
val Long.unsignedInt
    get() = toUInt().toInt()

private class UnreachableError : Error()

fun unreachable(): Nothing = throw UnreachableError()

fun PlayerEntity.isFriend() = Friends.isFriend(this.gameProfile.name)

// / Collection/iterable utilities

fun <T> Iterator<T>.forEachRemainingIndexed(startAt: Int = 0, action: (Int, T) -> Unit) {
    var index = startAt
    while (hasNext()) {
        action(index, next())
        index++
    }
}

/**
 * Returns a resettable, infinitely cycling iterator over this iterable.
 * Please note this will throw an exception if the underlying collection is empty.
 */
fun <T> Iterable<T>.cyclingIterator(): CyclingIterator<T> = CyclingIterator(this)

class CyclingIterator<T>(private val iterable: Iterable<T>) : Iterator<T> {
    var internalIterator = iterable.iterator()

    override fun hasNext(): Boolean = true

    override fun next(): T {
        if (!internalIterator.hasNext()) reset()
        return internalIterator.next()
    }

    fun reset() {
        internalIterator = iterable.iterator()
    }
}

fun ConfigNode.flattenedStream(): Stream<ConfigLeaf<*>> {
    return when (this) {
        is ConfigBranch -> {
            items.stream().flatMap { it.flattenedStream() }
        }
        is ConfigLeaf<*> -> {
            Stream.of(this)
        }
        else -> Stream.empty()
    }
}

/**
 * Returns the sum of all values produced by [selector] function applied to each element in the collection.
 */
inline fun <T> Iterable<T>.sumByFloat(selector: (T) -> Float): Float {
    var sum = 0f
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

// / Math

inline fun MatrixStack.matrix(block: () -> Unit) {
    push()
    block()
    pop()
}

fun MatrixStack.push(entry: MatrixStack.Entry) = getStack().addLast(entry)

fun createIdentityMatrixStackEntry(): MatrixStack.Entry {
    val model = Matrix4f()
    model.loadIdentity()
    val normal = Matrix3f()
    normal.loadIdentity()
    return `IMatrixStack$Entry`.create(model, normal)
}

operator fun Vec3d.times(factor: Double): Vec3d = multiply(factor)

fun noBobbingCamera(matrixStack: MatrixStack, block: () -> Unit) {
    with(matrixStack) {
        val entry = createIdentityMatrixStackEntry()
        entry.model.multiply(mc.gameRenderer.getBasicProjectionMatrix(mc.gameRenderer.camera, mc.tickDelta, false))
        push(entry)
        mc.gameRenderer.loadProjectionMatrix(entry.model)

        block()

        pop()
        mc.gameRenderer.loadProjectionMatrix(peek().model)
    }
}

fun Matrix4f.multiplyMatrix(q: Quaternion) = Quaternion(
    a00 * q.x + a01 * q.y + a02 * q.z + a03 * q.w,
    a10 * q.x + a11 * q.y + a12 * q.z + a13 * q.w,
    a20 * q.x + a21 * q.y + a22 * q.z + a23 * q.w,
    a30 * q.x + a31 * q.y + a32 * q.z + a33 * q.w
)

fun Quaternion.toScreen(): Quaternion {
    val newW = 1.0f / w * 0.5f
    return Quaternion(
        x * newW + 0.5f,
        y * newW + 0.5f,
        z * newW + 0.5f,
        newW
    )
}

val Entity.prevPos
    get() = Vec3d(prevX, prevY, prevZ)

fun Entity.getInterpolatedPos(tickDelta: Float = mc.tickDelta): Vec3d {
    val prev = prevPos
    return prev + (pos - prev) * tickDelta.toDouble()
}

// / Minecraft

/**
 * Whether or not this [Element] is actively consuming keypresses.
 *
 * In effect, only `true` if `this` is a [TextFieldWidget] and [TextFieldWidget.isActive]
 */
val Element.expectingInput: Boolean
    get() = this is TextFieldWidget && this.isActive

/**
 * Whether or not this [RecipeBookWidget] is open and the player is not in spectator
 */
val RecipeBookWidget.expectingInput: Boolean
    // This check 'should' also cover whether or not the search field is active,
    // but, said field is private and is not worth an accessor.
    get() = this.isOpen && !(mc.player?.isSpectator == true)

/**
 * Whether or not this [Screen] is expecting input.
 *
 * This returns true if
 * * The screen is an sign editing screen, or,
 * * The screen is a book editing screen, or,
 * * The screen is the creative inventory screen, or,
 * * The screen is a furnace or crafting screen with a recipe book that is expecting input, or,
 * * The focused element of this screen is expecting input
 */
val Screen.expectingInput: Boolean
    get() = this is SignEditScreen ||
        this is BookEditScreen ||
        this is CreativeInventoryScreen ||
        (this is AbstractFurnaceScreen<*> && this.recipeBook.expectingInput) ||
        (this is CraftingScreen && this.recipeBookWidget.expectingInput) ||
        this.focused?.expectingInput == true

data class Colour(val a: Float, val r: Float, val g: Float, val b: Float) {
    fun asInts() = intArrayOf((a * 255).toInt(), (r * 255).toInt(), (g * 255).toInt(), (b * 255).toInt())

    fun asFloatRGBA() = floatArrayOf(r, g, b, a)

    fun asARGB(): Int {
        val integers = asInts()
        return (integers[0] shl 24) or (integers[1] shl 16) or (integers[2] shl 8) or integers[3]
    }

    companion object {
        fun fromARGB(argb: Int): Colour {
            val a = ((argb shr 24) and 0xFF) / 255f
            val r = ((argb shr 16) and 0xFF) / 255f
            val g = ((argb shr 8) and 0xFF) / 255f
            val b = (argb and 0xFF) / 255f
            return Colour(a, r, g, b)
        }

        fun fromFloatRGBA(rgba: FloatArray): Colour {
            return Colour(rgba[3], rgba[0], rgba[1], rgba[2])
        }

        val WHITE = Colour(1f, 1f, 1f, 1f)
        val TRANSPARENT = Colour(0f, 1f, 1f, 1f)
    }
}

inline fun VertexConsumer.vertex(vec: Vec3d) = this.vertex(vec.x, vec.y, vec.z)

fun VertexConsumer.colour(colour: Colour) = this.color(colour.r, colour.g, colour.b, colour.a)

fun <K, V> MutableMap<K, V>.put(p: Pair<K, V>) {
    put(p.first, p.second)
}

fun String.replaceAll(chars: Iterable<Char>, replacement: String): String {
    var s = this
    for (c in chars) {
        s = s.replace("$c", replacement)
    }
    return s
}

operator fun Iterable<ItemPredicate>.contains(i: ItemStack): Boolean {
    for (item in this) {
        if (item.test(i))
            return true
    }
    return false
}

operator fun Iterable<ItemPredicate>.contains(i: Item) = i.defaultStack in this

// just a kotliny shortcut
// unfortunately can't add static extensions
fun itemPredicate(applyFun: ItemPredicate.Builder.() -> Unit) =
    ItemPredicate.Builder.create().apply(applyFun).build()

// i dont condone python but this is really cool
operator fun String.times(factor: Int) = this.repeat(factor)

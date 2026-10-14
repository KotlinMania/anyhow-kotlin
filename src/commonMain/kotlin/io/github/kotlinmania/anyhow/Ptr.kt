// port-lint: source ptr.rs
package io.github.kotlinmania.anyhow

internal data class Own<T>(
    internal val ptr: T,
) {
    internal companion object
}

internal fun <T> Own<T>.clone(): Own<T> = Own(ptr)

internal fun <T> Own.Companion.new(ptr: T): Own<T> = Own(ptr)

internal inline fun <reified U> Own<*>.cast(): Own<U> = Own(ptr as U)

internal fun <T> Own<T>.boxed(): T = ptr

internal fun <T> Own<T>.byRef(): Ref<T> = Ref.fromRaw(ptr)

internal fun <T> Own<T>.byMut(): Mut<T> = Mut.fromRaw(ptr)

internal data class Ref<T>(
    internal val ptr: T,
) {
    internal companion object
}

internal fun <T> Ref<T>.clone(): Ref<T> = Ref(ptr)

internal fun <T> Ref.Companion.new(ptr: T): Ref<T> = Ref(ptr)

internal fun <T> Ref.Companion.fromRaw(ptr: T): Ref<T> = Ref(ptr)

internal inline fun <reified U> Ref<*>.cast(): Ref<U> = Ref(ptr as U)

internal fun <T> Ref<T>.byMut(): Mut<T> = Mut.fromRaw(ptr)

internal fun <T> Ref<T>.asPtr(): T = ptr

internal fun <T> Ref<T>.deref(): T = ptr

internal data class Mut<T>(
    internal val ptr: T,
) {
    internal companion object
}

internal fun <T> Mut<T>.clone(): Mut<T> = Mut(ptr)

internal fun <T> Mut.Companion.fromRaw(ptr: T): Mut<T> = Mut(ptr)

internal inline fun <reified U> Mut<*>.cast(): Mut<U> = Mut(ptr as U)

internal fun <T> Mut<T>.byRef(): Ref<T> = Ref.fromRaw(ptr)

internal fun <T> Mut<T>.extend(): Mut<T> = Mut(ptr)

internal fun <T> Mut<T>.derefMut(): T = ptr

internal fun <T> Mut<T>.read(): T = ptr

// Force explicit type arguments on all calls of `.cast<U>()`.
internal interface CastTo<T> {
    interface Target<T>
}

// port-lint: source ptr.rs
package io.github.kotlinmania.anyhow

internal data class Own<T>(
    internal val ptr: T,
) {
    internal companion object {
        internal fun <T> new(ptr: T): Own<T> = Own(ptr)
    }

    internal inline fun <reified U> cast(): Own<U> = Own(ptr as U)

    internal fun boxed(): T = ptr

    internal fun clone(): Own<T> = Own(ptr)

    internal fun byRef(): Ref<T> = Ref.fromRaw(ptr)

    internal fun byMut(): Mut<T> = Mut.fromRaw(ptr)
}

internal data class Ref<T>(
    internal val ptr: T,
) {
    internal companion object {
        internal fun <T> new(ptr: T): Ref<T> = Ref(ptr)

        internal fun <T> fromRaw(ptr: T): Ref<T> = Ref(ptr)
    }

    internal inline fun <reified U> cast(): Ref<U> = Ref(ptr as U)

    internal fun byMut(): Mut<T> = Mut.fromRaw(ptr)

    internal fun asPtr(): T = ptr

    internal fun deref(): T = ptr

    internal fun clone(): Ref<T> = Ref(ptr)
}

internal data class Mut<T>(
    internal val ptr: T,
) {
    internal companion object {
        internal fun <T> fromRaw(ptr: T): Mut<T> = Mut(ptr)
    }

    internal inline fun <reified U> cast(): Mut<U> = Mut(ptr as U)

    internal fun byRef(): Ref<T> = Ref.fromRaw(ptr)

    internal fun extend(): Mut<T> = Mut(ptr)

    internal fun derefMut(): T = ptr

    internal fun read(): T = ptr

    internal fun clone(): Mut<T> = Mut(ptr)
}

internal interface CastTo

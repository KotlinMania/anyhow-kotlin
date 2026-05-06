// port-lint: source src/ptr.rs
package io.github.kotlinmania.anyhow

public data class Own<T>(
    public val ptr: T,
) {
    public companion object {
        public fun <T> new(ptr: T): Own<T> = Own(ptr)
    }

    public inline fun <reified U> cast(): Own<U> = Own(ptr as U)

    public fun boxed(): T = ptr

    public fun byRef(): Ref<T> = Ref.fromRaw(ptr)

    public fun byMut(): Mut<T> = Mut.fromRaw(ptr)
}

public data class Ref<T>(
    public val ptr: T,
) {
    public companion object {
        public fun <T> new(ptr: T): Ref<T> = Ref(ptr)

        public fun <T> fromRaw(ptr: T): Ref<T> = Ref(ptr)
    }

    public inline fun <reified U> cast(): Ref<U> = Ref(ptr as U)

    public fun byMut(): Mut<T> = Mut.fromRaw(ptr)

    public fun asPtr(): T = ptr

    public fun deref(): T = ptr
}

public data class Mut<T>(
    public val ptr: T,
) {
    public companion object {
        public fun <T> fromRaw(ptr: T): Mut<T> = Mut(ptr)
    }

    public inline fun <reified U> cast(): Mut<U> = Mut(ptr as U)

    public fun byRef(): Ref<T> = Ref.fromRaw(ptr)

    public fun extend(): Mut<T> = Mut(ptr)

    public fun derefMut(): T = ptr

    public fun read(): T = ptr
}

// Force turbofish on all calls of `.cast::<U>()`.
public interface CastTo

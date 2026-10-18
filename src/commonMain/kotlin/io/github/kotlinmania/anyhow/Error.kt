@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

// port-lint: source error.rs

package io.github.kotlinmania.anyhow

import kotlin.Result

import kotlin.native.HiddenFromObjC
import kotlin.reflect.KClass

internal typealias Target = StdError

internal class ErrorVTable(
    internal val objectDrop: (Own<ErrorImpl>) -> Unit = ::objectDrop,
    internal val objectRef: (Ref<ErrorImpl>) -> StdError,
    internal val objectBoxed: (Own<ErrorImpl>) -> StdError,
    internal val objectReallocateBoxed: (Own<ErrorImpl>) -> StdError,
    internal val objectDowncast: (Ref<ErrorImpl>, KClass<*>) -> Any?,
    internal val objectDropRest: (Own<ErrorImpl>, KClass<*>) -> Unit,
    internal val objectBacktrace: (Ref<ErrorImpl>) -> Backtrace?,
)

internal class ErrorImpl(
    internal val vtable: ErrorVTable,
    internal val backtrace: Backtrace?,
    internal val `object`: StdError,
) : StdError {
    override fun source(): StdError? = source(this)

    override fun toString(): String = fmt(StringBuilder()).toString()

    internal companion object
}

internal fun ErrorImpl.erase(): Ref<ErrorImpl> = Ref.new(this)

internal fun source(errorImpl: ErrorImpl): StdError? = ErrorImpl.error(errorImpl.erase()).source()

internal fun ErrorImpl.provide(request: Nightly.Request) {
    ErrorImpl.provide(erase(), request)
}

internal fun ErrorImpl.fmt(
    formatter: Appendable,
    debug: Boolean,
): Appendable =
    if (debug) {
        formatter.append(debug(erase(), alternate = false))
    } else {
        fmt(formatter)
    }

internal fun ErrorImpl.fmt(formatter: Appendable): Appendable = formatter.append(ErrorImpl.error(erase()).toString())

internal fun ErrorImpl.Companion.error(thisRef: Ref<ErrorImpl>): StdError = vtable(thisRef.ptr).objectRef(thisRef)

internal fun ErrorImpl.Companion.errorMut(thisRef: Mut<ErrorImpl>): StdError = vtable(thisRef.ptr).objectRef(thisRef.byRef())

internal fun ErrorImpl.Companion.backtrace(thisRef: Ref<ErrorImpl>): Backtrace {
    val direct = thisRef.ptr.backtrace
    if (direct != null) return direct

    val err = error(thisRef)
    val provided =
        if (err is Throwable) {
            Nightly.requestRefBacktrace(err as Throwable)
        } else {
            Nightly.requestRefBacktrace(err)
        }
    return provided ?: vtable(thisRef.ptr).objectBacktrace(thisRef) ?: Backtrace.capture()
}

internal fun ErrorImpl.Companion.provide(
    thisRef: Ref<ErrorImpl>,
    request: Nightly.Request,
) {
    val direct = thisRef.ptr.backtrace
    if (direct != null) {
        Nightly.provideRefBacktrace(request, direct)
    }
    Nightly.provide(error(thisRef), request)
}

internal fun ErrorImpl.Companion.chain(thisRef: Ref<ErrorImpl>): Chain = Chain.new(error(thisRef))

internal class ContextError<C, E>(
    internal val context: C,
    internal val error: E,
) : StdError where E : StdError {
    override fun source(): StdError? = error

    override fun toString(): String = context.toString()
}

private fun vtable(p: ErrorImpl): ErrorVTable = p.vtable

internal class ThrowableStdError(
    internal val value: Throwable,
) : StdError {
    override fun source(): StdError? {
        val cause = value.cause ?: return null
        return throwableAsStdError(cause)
    }

    override fun toString(): String = value.toString()
}

internal fun throwableAsStdError(throwable: Throwable): StdError =
    if (throwable is Error) {
        throwable
    } else {
        ThrowableStdError(throwable)
    }

internal fun errorSource(error: Error): StdError? {
    val inner = error.inner.byRef()
    val stdError = vtable(inner.ptr).objectRef(inner)
    return stdError.source()
}

internal fun errorImplError(thisRef: Ref<ErrorImpl>): StdError = ErrorImpl.error(thisRef)

internal fun errorImplBacktrace(thisRef: Ref<ErrorImpl>): Backtrace = ErrorImpl.backtrace(thisRef)

internal fun errorImplChain(thisRef: Ref<ErrorImpl>): Chain = ErrorImpl.chain(thisRef)

/**
 * Wrap the error value with additional context.
 *
 * For attaching context to a [Result] as it is propagated, the [Context] extension functions
 * may be more convenient than this function.
 */
@HiddenFromObjC
public fun Error.context(context: Any): Error {
    val error: ContextError<Any, Error> = ContextError(context, this)

    val vtable =
        ErrorVTable(
            objectRef = { e -> contextChainObjectRef(e) },
            objectBoxed = { e -> contextChainObjectBoxed(e) },
            objectReallocateBoxed = { e -> contextChainObjectReallocateBoxed(e) },
            objectDowncast = { e, target -> contextChainDowncast(e, target) },
            objectDropRest = { e, target -> contextChainDropRest(e, target) },
            objectBacktrace = { e -> contextBacktrace(e) },
        )

    val backtrace = null
    return construct(error, vtable, backtrace)
}

/**
 * Returns true if `E` is the type held by this error object.
 *
 * For errors with context, this method returns true if `E` matches the type of the context `C`
 * **or** the type of the error on which the context has been attached.
 */
@HiddenFromObjC
public inline fun <reified E> Error.`is`(): Boolean where E : Any = isType(E::class)

internal inline fun <reified E : Any> Error.is_(): Boolean = isType(E::class)

@HiddenFromObjC
public fun Error.isType(type: KClass<*>): Boolean = downcastRef(type) != null

/**
 * Attempt to downcast the error object to a concrete type.
 */
@HiddenFromObjC
public inline fun <reified E : Any> Error.downcast(): Result<E> {
    val value = downcastRef<E>()
    return if (value != null) Result.success(value) else Result.failure(this)
}

/**
 * Downcast this error object by reference.
 */
@HiddenFromObjC
public inline fun <reified E : Any> Error.downcastRef(): E? {
    val addr = downcastRef(E::class) ?: return null
    return addr as? E
}

/**
 * Downcast this error object by mutable reference.
 */
@HiddenFromObjC
public inline fun <reified E : Any> Error.downcastMut(): E? = downcastRef()

@HiddenFromObjC
public fun Error.downcastRef(type: KClass<*>): Any? = vtable(inner.ptr).objectDowncast(inner.byRef(), type)

/**
 * An iterator of the chain of source errors contained by this [Error].
 *
 * This iterator will visit every error in the cause chain of this error object, beginning with
 * the error that this error object was created from.
 */
@HiddenFromObjC
public fun Error.chain(): Chain = ErrorImpl.chain(inner.byRef())

/**
 * The lowest level cause of this error — this error's cause's cause's cause etc.
 *
 * The root cause is the last error in the iterator produced by [chain].
 */
@HiddenFromObjC
public fun Error.rootCause(): StdError {
    var last: StdError? = null
    for (cause in chain()) {
        last = cause
    }
    return last ?: this
}

/**
 * Get the backtrace for this [Error].
 */
@HiddenFromObjC
public fun Error.backtrace(): Backtrace = ErrorImpl.backtrace(inner.byRef())

@HiddenFromObjC
public fun Error.displayString(alternate: Boolean = false): String = display(inner.byRef(), alternate = alternate)

@HiddenFromObjC
public fun Error.debugString(alternate: Boolean = false): String = debug(inner.byRef(), alternate = alternate)

/**
 * Convert to a standard error object.
 */
@HiddenFromObjC
public fun Error.intoBoxedDynError(): StdError {
    val outer = this
    return vtable(outer.inner.ptr).objectBoxed(outer.inner)
}

/**
 * Convert to a standard error object without preserving the collected backtrace.
 */
@HiddenFromObjC
public fun Error.reallocateIntoBoxedDynErrorWithoutBacktrace(): StdError {
    val outer = this
    return vtable(outer.inner.ptr).objectReallocateBoxed(outer.inner)
}

internal fun Error.provide(request: Nightly.Request) {
    ErrorImpl.provide(inner.byRef(), request)
}

@HiddenFromObjC
public fun Error.thiserrorProvide(request: Nightly.Request) {
    provide(request)
}

@HiddenFromObjC
public fun Error.Companion.from(error: StdError): Error {
    val backtrace = backtraceIfAbsent(error) ?: backtrace()
    return constructFromStd(error, backtrace)
}

@HiddenFromObjC
public fun Error.deref(): Target = ErrorImpl.error(inner.byRef())

@HiddenFromObjC
public fun Error.derefMut(): Target = ErrorImpl.errorMut(inner.byMut())

internal fun Error.fmt(formatter: Appendable): Appendable = formatter.append(display(inner.byRef(), alternate = false))

internal fun Error.fmt(
    formatter: Appendable,
    debug: Boolean,
): Appendable =
    if (debug) {
        formatter.append(debug(inner.byRef(), alternate = false))
    } else {
        fmt(formatter)
    }

internal fun Error.drop() {
    vtable(inner.ptr).objectDrop(inner)
}

internal fun from(
    error: Error,
    send: Boolean,
    sync: Boolean,
): StdError = if (send && sync) error.intoBoxedDynError() else error.intoBoxedDynError()

internal fun from(
    error: Error,
    send: Boolean,
): StdError = if (send) error.intoBoxedDynError() else error.intoBoxedDynError()

internal fun from(error: Error): StdError = error.intoBoxedDynError()

@HiddenFromObjC
public fun Error.asRef(): StdError = deref()

internal fun Error.asRef(sendSync: Boolean): StdError = if (sendSync) deref() else deref()

/**
 * Create a new error object from any error type implementing [StdError].
 */
@HiddenFromObjC
public fun Error.Companion.new(error: StdError): Error {
    val backtrace = backtraceIfAbsent(error) ?: backtrace()
    return constructFromStd(error, backtrace)
}

/**
 * Create a new error object from a printable error message.
 */
@HiddenFromObjC
public fun Error.Companion.msg(message: Any): Error = constructFromAdhoc(message, backtrace())

/**
 * Construct an error object from a type-erased standard error.
 */
@HiddenFromObjC
public fun Error.Companion.fromBoxed(boxedError: StdError): Error {
    val backtrace = backtraceIfAbsent(boxedError) ?: backtrace()
    return constructFromBoxed(boxedError, backtrace)
}

internal fun Error.Companion.constructFromStd(
    error: StdError,
    backtrace: Backtrace?,
): Error {
    val vtable =
        ErrorVTable(
            objectDrop = { e -> objectDrop(e) },
            objectRef = { e -> objectRef(e) },
            objectBoxed = { e -> objectBoxed(e) },
            objectReallocateBoxed = { e -> objectReallocateBoxed(e) },
            objectDowncast = { e, target -> objectDowncast(e, target) },
            objectDropRest = { e, target -> objectDropFront(e, target) },
            objectBacktrace = { e -> noBacktrace(e) },
        )

    return construct(error, vtable, backtrace)
}

internal fun Error.Companion.constructFromAdhoc(
    message: Any,
    backtrace: Backtrace?,
): Error {
    val error: MessageError<Any> = MessageError(message)

    val vtable =
        ErrorVTable(
            objectDrop = { e -> objectDrop(e) },
            objectRef = { e -> objectRef(e) },
            objectBoxed = { e -> objectBoxed(e) },
            objectReallocateBoxed = { e -> objectReallocateBoxed(e) },
            objectDowncast = { e, target -> objectDowncastMessage(e, target) },
            objectDropRest = { e, target -> objectDropFront(e, target) },
            objectBacktrace = { e -> noBacktrace(e) },
        )

    return construct(error, vtable, backtrace)
}

internal fun Error.Companion.constructFromDisplay(
    message: Any,
    backtrace: Backtrace?,
): Error {
    val error: DisplayError<Any> = DisplayError(message)

    val vtable =
        ErrorVTable(
            objectDrop = { e -> objectDrop(e) },
            objectRef = { e -> objectRef(e) },
            objectBoxed = { e -> objectBoxed(e) },
            objectReallocateBoxed = { e -> objectReallocateBoxed(e) },
            objectDowncast = { e, target -> objectDowncastDisplay(e, target) },
            objectDropRest = { e, target -> objectDropFront(e, target) },
            objectBacktrace = { e -> noBacktrace(e) },
        )

    return construct(error, vtable, backtrace)
}

internal fun Error.Companion.constructFromContext(
    context: Any,
    error: StdError,
    backtrace: Backtrace?,
): Error {
    val ctx: ContextError<Any, StdError> = ContextError(context, error)

    val vtable =
        ErrorVTable(
            objectDrop = { e -> objectDrop(e) },
            objectRef = { e -> objectRef(e) },
            objectBoxed = { e -> objectBoxed(e) },
            objectReallocateBoxed = { e -> objectReallocateBoxed(e) },
            objectDowncast = { e, target -> contextDowncast(e, target) },
            objectDropRest = { e, target -> contextDropRest(e, target) },
            objectBacktrace = { e -> noBacktrace(e) },
        )

    return construct(ctx, vtable, backtrace)
}

internal fun Error.Companion.constructFromBoxed(
    error: StdError,
    backtrace: Backtrace?,
): Error {
    val boxed = BoxedError(error)

    val vtable =
        ErrorVTable(
            objectDrop = { e -> objectDrop(e) },
            objectRef = { e -> objectRef(e) },
            objectBoxed = { e -> objectBoxed(e) },
            objectReallocateBoxed = { e -> objectReallocateBoxed(e) },
            objectDowncast = { e, target -> objectDowncastBoxed(e, target) },
            objectDropRest = { e, target -> objectDropFront(e, target) },
            objectBacktrace = { e -> noBacktrace(e) },
        )

    return construct(boxed, vtable, backtrace)
}

private fun construct(
    error: StdError,
    vtable: ErrorVTable,
    backtrace: Backtrace?,
): Error {
    val inner = Own.new(ErrorImpl(vtable = vtable, backtrace = backtrace, `object` = error))
    return Error(inner)
}

private fun objectDrop(e: Own<ErrorImpl>) {
    e.boxed()
}

private fun objectDropFront(
    e: Own<ErrorImpl>,
    target: KClass<*>,
) {
    val ignored = target
    e.boxed()
}

private fun objectRef(e: Ref<ErrorImpl>): StdError = e.ptr.`object`

private fun objectBoxed(e: Own<ErrorImpl>): StdError = e.ptr.`object`

private fun objectReallocateBoxed(e: Own<ErrorImpl>): StdError = e.ptr.`object`

private fun objectDowncast(
    e: Ref<ErrorImpl>,
    target: KClass<*>,
): Any? {
    val value = e.ptr.`object`
    return if (target.isInstance(value)) value else null
}

private fun noBacktrace(e: Ref<ErrorImpl>): Backtrace? {
    val ignored = e
    return null
}

private fun objectDowncastMessage(
    e: Ref<ErrorImpl>,
    target: KClass<*>,
): Any? {
    val obj = e.ptr.`object` as MessageError<*>
    val value = obj.value
    return if (target.isInstance(value)) value else null
}

private fun objectDowncastDisplay(
    e: Ref<ErrorImpl>,
    target: KClass<*>,
): Any? {
    val obj = e.ptr.`object` as DisplayError<*>
    val value = obj.value
    return if (target.isInstance(value)) value else null
}

private fun contextDowncast(
    e: Ref<ErrorImpl>,
    target: KClass<*>,
): Any? {
    val ctx = e.ptr.`object` as ContextError<*, *>
    val context = ctx.context
    if (target.isInstance(context)) return context

    val error = ctx.error
    return if (target.isInstance(error)) error else null
}

private fun contextDropRest(
    e: Own<ErrorImpl>,
    target: KClass<*>,
) {
    val ignored = target
    e.boxed()
}

private fun objectDowncastBoxed(
    e: Ref<ErrorImpl>,
    target: KClass<*>,
): Any? {
    val boxed = e.ptr.`object` as BoxedError
    val value = boxed.value
    return if (target.isInstance(value)) value else null
}

private fun contextChainObjectRef(e: Ref<ErrorImpl>): StdError = e.ptr.`object`

private fun contextChainObjectBoxed(e: Own<ErrorImpl>): StdError = e.ptr.`object`

private fun contextChainObjectReallocateBoxed(e: Own<ErrorImpl>): StdError = e.ptr.`object`

private fun contextChainDowncast(
    e: Ref<ErrorImpl>,
    target: KClass<*>,
): Any? {
    val ctx = e.ptr.`object` as ContextError<*, *>
    val context = ctx.context
    if (target.isInstance(context)) return context

    val source = ctx.error as Error
    return vtable(source.inner.ptr).objectDowncast(source.inner.byRef(), target)
}

private fun contextChainDropRest(
    e: Own<ErrorImpl>,
    target: KClass<*>,
) {
    val ignored = target
    e.boxed()
}

private fun contextBacktrace(e: Ref<ErrorImpl>): Backtrace? {
    val ctx = e.ptr.`object` as ContextError<*, *>
    val source = ctx.error as Error
    return ErrorImpl.backtrace(source.inner.byRef())
}

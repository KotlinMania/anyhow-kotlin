@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

// port-lint: source error.rs

package io.github.kotlinmania.anyhow

import kotlin.native.HiddenFromObjC
import kotlin.reflect.KClass

internal class ErrorVTable(
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
)

internal class ContextError<C, E>(
    internal val context: C,
    internal val error: E,
) : StdError where E : StdError {
    override fun source(): StdError? = error

    override fun toString(): String = context.toString()
}

private fun vtable(p: ErrorImpl): ErrorVTable = p.vtable

internal fun ErrorImpl.erase(): Ref<ErrorImpl> = Ref.new(this)

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

internal fun errorImplError(thisRef: Ref<ErrorImpl>): StdError = vtable(thisRef.ptr).objectRef(thisRef)

internal fun errorImplBacktrace(thisRef: Ref<ErrorImpl>): Backtrace {
    val direct = thisRef.ptr.backtrace
    if (direct != null) return direct

    val throwable = errorImplError(thisRef) as? Throwable
    if (throwable == null) return Backtrace.capture()

    return Nightly.requestRefBacktrace(throwable) ?: Backtrace.capture()
}

internal fun errorImplChain(thisRef: Ref<ErrorImpl>): Chain = chainNew(errorImplError(thisRef))

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
            objectBacktrace = { e -> contextChainBacktrace(e) },
        )

    val backtrace = null
    return construct(error, vtable, backtrace)
}

@HiddenFromObjC
public inline fun <reified E> Error.`is`(): Boolean where E : Any = isType(E::class)

@HiddenFromObjC
public fun Error.isType(type: KClass<*>): Boolean = downcastRef(type) != null

@HiddenFromObjC
public inline fun <reified E : Any> Error.downcast(): Result<E> {
    val value = downcastRef<E>()
    return if (value != null) Result.success(value) else Result.failure(this)
}

@HiddenFromObjC
public inline fun <reified E : Any> Error.downcastRef(): E? {
    val addr = downcastRef(E::class) ?: return null
    return addr as? E
}

@HiddenFromObjC
public inline fun <reified E : Any> Error.downcastMut(): E? = downcastRef()

@HiddenFromObjC
public fun Error.downcastRef(type: KClass<*>): Any? = vtable(inner.ptr).objectDowncast(inner.byRef(), type)

@HiddenFromObjC
public fun Error.chain(): Chain = errorImplChain(inner.byRef())

@HiddenFromObjC
public fun Error.rootCause(): StdError {
    var last: StdError? = null
    for (cause in chain()) {
        last = cause
    }
    return last ?: this
}

@HiddenFromObjC
public fun Error.backtrace(): Backtrace = errorImplBacktrace(inner.byRef())

@HiddenFromObjC
public fun Error.debugString(alternate: Boolean = false): String = debug(inner.byRef(), alternate = alternate)

@HiddenFromObjC
public fun Error.intoBoxedDynError(): StdError {
    val outer = this
    return vtable(outer.inner.ptr).objectBoxed(outer.inner)
}

@HiddenFromObjC
public fun Error.reallocateIntoBoxedDynErrorWithoutBacktrace(): StdError {
    val outer = this
    return vtable(outer.inner.ptr).objectReallocateBoxed(outer.inner)
}

@HiddenFromObjC
public fun Error.Companion.new(error: StdError): Error {
    val backtrace = backtraceIfAbsent(error) ?: backtrace()
    return constructFromStd(error, backtrace)
}

@HiddenFromObjC
public fun Error.Companion.msg(message: Any): Error = constructFromAdhoc(message, backtrace())

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
            objectRef = { e -> objectRef(e) },
            objectBoxed = { e -> objectBoxed(e) },
            objectReallocateBoxed = { e -> objectReallocateBoxed(e) },
            objectDowncast = { e, target -> objectDowncast(e, target) },
            objectDropRest = { e, target -> objectDropFront(e, target) },
            objectBacktrace = { _ -> null },
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
            objectRef = { e -> objectRefMessage(e) },
            objectBoxed = { e -> objectBoxed(e) },
            objectReallocateBoxed = { e -> objectReallocateBoxed(e) },
            objectDowncast = { e, target -> objectDowncastMessage(e, target) },
            objectDropRest = { e, target -> objectDropFrontMessage(e, target) },
            objectBacktrace = { _ -> null },
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
            objectRef = { e -> objectRefDisplay(e) },
            objectBoxed = { e -> objectBoxed(e) },
            objectReallocateBoxed = { e -> objectReallocateBoxed(e) },
            objectDowncast = { e, target -> objectDowncastDisplay(e, target) },
            objectDropRest = { e, target -> objectDropFrontDisplay(e, target) },
            objectBacktrace = { _ -> null },
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
            objectRef = { e -> objectRefContext(e) },
            objectBoxed = { e -> objectBoxed(e) },
            objectReallocateBoxed = { e -> objectReallocateBoxed(e) },
            objectDowncast = { e, target -> contextDowncast(e, target) },
            objectDropRest = { e, target -> contextDropRest(e, target) },
            objectBacktrace = { _ -> null },
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
            objectRef = { e -> objectRefBoxed(e) },
            objectBoxed = { e -> objectBoxed(e) },
            objectReallocateBoxed = { e -> objectReallocateBoxed(e) },
            objectDowncast = { e, target -> objectDowncastBoxed(e, target) },
            objectDropRest = { e, target -> objectDropFrontBoxed(e, target) },
            objectBacktrace = { _ -> null },
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

private fun objectRef(e: Ref<ErrorImpl>): StdError = e.ptr.`object`

private fun objectRefMessage(e: Ref<ErrorImpl>): StdError = (e.ptr.`object` as MessageError<*>)

private fun objectRefDisplay(e: Ref<ErrorImpl>): StdError = (e.ptr.`object` as DisplayError<*>)

private fun objectRefContext(e: Ref<ErrorImpl>): StdError = (e.ptr.`object` as ContextError<*, *>)

private fun objectRefBoxed(e: Ref<ErrorImpl>): StdError = (e.ptr.`object` as BoxedError)

private fun objectBoxed(e: Own<ErrorImpl>): StdError = e.ptr.`object`

private fun objectReallocateBoxed(e: Own<ErrorImpl>): StdError = e.ptr.`object`

private fun objectDowncast(
    e: Ref<ErrorImpl>,
    target: KClass<*>,
): Any? {
    val value = e.ptr.`object`
    return if (target.isInstance(value)) value else null
}

private fun objectDropFront(
    e: Own<ErrorImpl>,
    target: KClass<*>,
) {
    val targetVal = target
    val eVal = e
}

private fun objectDowncastMessage(
    e: Ref<ErrorImpl>,
    target: KClass<*>,
): Any? {
    val obj = e.ptr.`object` as MessageError<*>
    val value = obj.value
    return if (target.isInstance(value)) value else null
}

private fun objectDropFrontMessage(
    e: Own<ErrorImpl>,
    target: KClass<*>,
) {
    val targetVal = target
    val eVal = e
}

private fun objectDowncastDisplay(
    e: Ref<ErrorImpl>,
    target: KClass<*>,
): Any? {
    val obj = e.ptr.`object` as DisplayError<*>
    val value = obj.value
    return if (target.isInstance(value)) value else null
}

private fun objectDropFrontDisplay(
    e: Own<ErrorImpl>,
    target: KClass<*>,
) {
    val targetVal = target
    val eVal = e
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
    val targetVal = target
    val eVal = e
}

private fun objectDowncastBoxed(
    e: Ref<ErrorImpl>,
    target: KClass<*>,
): Any? {
    val boxed = e.ptr.`object` as BoxedError
    val value = boxed.value
    return if (target.isInstance(value)) value else null
}

private fun objectDropFrontBoxed(
    e: Own<ErrorImpl>,
    target: KClass<*>,
) {
    val targetVal = target
    val eVal = e
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
    val targetVal = target
    val eVal = e
}

private fun contextChainBacktrace(e: Ref<ErrorImpl>): Backtrace? {
    val ctx = e.ptr.`object` as ContextError<*, *>
    val source = ctx.error as Error
    return source.backtrace()
}

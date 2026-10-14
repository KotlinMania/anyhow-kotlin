@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

// port-lint: source macros.rs

package io.github.kotlinmania.anyhow

import kotlin.native.HiddenFromObjC

/**
 * Construct an ad-hoc error from a string or existing non-`anyhow` error value.
 *
 * This evaluates to an [Error]. It can take either just a string or any custom type which
 * provides a string representation.
 *
 * If called with a single argument whose type implements [StdError], then that error's
 * [StdError.source] is preserved as the source of the resulting [Error].
 *
 * # Example
 *
 * ```kotlin
 * fun lookup(key: String): Result<Unit> {
 *     if (key.length != 16) {
 *         return Result.failure(anyhow("key length must be 16 characters, got $key"))
 *     }
 *     return Ok(Unit)
 * }
 * ```
 */
@HiddenFromObjC
public fun anyhow(message: String): Error = Error.msg(message)

@HiddenFromObjC
public fun anyhow(error: Any): Error =
    when (error) {
        is ErrorConvertible -> Trait.new(error)
        is StdError -> Boxed.new(error)
        else -> Adhoc.new(error)
    }

/**
 * Return early with an error.
 *
 * This helper is equivalent to `Result.failure(anyhow(args))`.
 *
 * The surrounding function's or closure's return value is required to be `Result<T>`.
 *
 * # Example
 *
 * ```kotlin
 * if (!hasPermission(user, resource)) {
 *     return bail("permission denied for accessing $resource")
 * }
 * ```
 */
@HiddenFromObjC
public fun bail(message: String): Result<Nothing> = Result.failure(anyhow(message))

@HiddenFromObjC
public fun bail(error: Any): Result<Nothing> = Result.failure(anyhow(error))

/**
 * Return early with an error if a condition is not satisfied.
 *
 * This helper is equivalent to `if (!cond) return Result.failure(anyhow(args)) else Ok(Unit)`.
 *
 * Analogously to `check`, [ensure] takes a condition and produces a failure if the condition
 * fails. Unlike `check`, [ensure] returns a [Result] containing an [Error] rather than throwing.
 *
 * # Example
 *
 * ```kotlin
 * ensure(user == 0, "only user 0 is allowed").getOrThrow()
 * ```
 */
@HiddenFromObjC
public fun ensure(
    cond: Boolean,
    message: String,
): Result<Unit> = if (cond) Ok(Unit) else Result.failure(anyhow(message))

@HiddenFromObjC
public fun ensure(
    cond: Boolean,
    error: Any,
): Result<Unit> = if (cond) Ok(Unit) else Result.failure(anyhow(error))

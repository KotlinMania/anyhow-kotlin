@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

// port-lint: source macros.rs

package io.github.kotlinmania.anyhow

import kotlin.native.HiddenFromObjC

/** Construct an ad-hoc error from a string or existing non-anyhow error value. */
@HiddenFromObjC
public fun anyhow(message: String): Error = Error.msg(message)

@HiddenFromObjC
public fun anyhow(error: Any): Error =
    when (error) {
        is ErrorConvertible -> Trait.new(error)
        is StdError -> Boxed.new(error)
        else -> Adhoc.new(error)
    }

/** Return early with an error. */
@HiddenFromObjC
public fun bail(message: String): Result<Nothing> = Result.failure(anyhow(message))

@HiddenFromObjC
public fun bail(error: Any): Result<Nothing> = Result.failure(anyhow(error))

/** Return early with an error if a condition is not satisfied. */
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

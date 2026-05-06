// port-lint: source src/macros.rs
package io.github.kotlinmania.anyhow

/**
 * Construct an ad-hoc error from a string or existing non-anyhow error value.
 *
 * This is a Kotlin translation of the upstream `anyhow!(...)` macro.
 *
 * Kotlin has no macro system, so this port provides functions that build an [Error] using the same
 * construction rules as the Rust macro:
 *
 * - If called with a string, constructs a message error.
 * - If called with an existing error value, preserves the source chain when possible.
 */
public fun anyhow(message: String): Error = Error.msg(message)

public fun anyhow(error: Any): Error {
    return when (error) {
        is ErrorConvertible -> Trait.new(error)
        is StdError -> Boxed.new(error)
        else -> Adhoc.new(error)
    }
}

/**
 * Return early with an error.
 *
 * This is a Kotlin translation of the upstream `bail!(...)` macro.
 *
 * In Kotlin, use this helper to produce a failing [Result] which you can return from the current
 * function.
 */
public fun bail(message: String): Result<Nothing> = Result.failure(anyhow(message))

public fun bail(error: Any): Result<Nothing> = Result.failure(anyhow(error))

/**
 * Return early with an error if a condition is not satisfied.
 *
 * This is a Kotlin translation of the upstream `ensure!(...)` macro.
 */
public fun ensure(cond: Boolean, message: String): Result<Unit> {
    return if (cond) Ok(Unit) else Result.failure(anyhow(message))
}

public fun ensure(cond: Boolean, error: Any): Result<Unit> {
    return if (cond) Ok(Unit) else Result.failure(anyhow(error))
}


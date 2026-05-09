// port-lint: source macros.rs
package io.github.kotlinmania.anyhow

/** Construct an ad-hoc error from a string or existing non-anyhow error value. */
public fun anyhow(message: String): Error = Error.msg(message)

public fun anyhow(error: Any): Error {
    return when (error) {
        is ErrorConvertible -> Trait.new(error)
        is StdError -> Boxed.new(error)
        else -> Adhoc.new(error)
    }
}

/** Return early with an error. */
public fun bail(message: String): Result<Nothing> = Result.failure(anyhow(message))

public fun bail(error: Any): Result<Nothing> = Result.failure(anyhow(error))

/** Return early with an error if a condition is not satisfied. */
public fun ensure(cond: Boolean, message: String): Result<Unit> {
    return if (cond) Ok(Unit) else Result.failure(anyhow(message))
}

public fun ensure(cond: Boolean, error: Any): Result<Unit> {
    return if (cond) Ok(Unit) else Result.failure(anyhow(error))
}

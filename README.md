# anyhow-kotlin in Kotlin

[![GitHub link](https://img.shields.io/badge/GitHub-KotlinMania%2Fanyhow--kotlin-blue.svg)](https://github.com/KotlinMania/anyhow-kotlin)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kotlinmania/anyhow-kotlin)](https://central.sonatype.com/artifact/io.github.kotlinmania/anyhow-kotlin)
[![Build status](https://img.shields.io/github/actions/workflow/status/KotlinMania/anyhow-kotlin/ci.yml?branch=main)](https://github.com/KotlinMania/anyhow-kotlin/actions)

This is a Kotlin Multiplatform line-by-line transliteration port of [`dtolnay/anyhow`](https://github.com/dtolnay/anyhow).

**Original Project:** This port is based on [`dtolnay/anyhow`](https://github.com/dtolnay/anyhow). All design credit and project intent belong to the upstream authors; this repository is a faithful port to Kotlin Multiplatform with no behavioural changes intended.

### Porting status

This is an **in-progress port**. The goal is feature parity with the upstream Rust crate while providing a native Kotlin Multiplatform API. Every Kotlin file carries a `// port-lint: source <path>` header naming its upstream Rust counterpart so the AST-distance tool can track provenance.

---

## Upstream README — `dtolnay/anyhow`

> The text below is reproduced and lightly edited from [`https://github.com/dtolnay/anyhow`](https://github.com/dtolnay/anyhow). It is the upstream project's own description and remains under the upstream authors' authorship; links have been rewritten to absolute upstream URLs so they continue to resolve from this repository.

## Anyhow&ensp;¯\\\_(°ペ)\_/¯


[<img alt="github" src="https://img.shields.io/badge/github-dtolnay/anyhow-8da0cb?style=for-the-badge&labelColor=555555&logo=github" height="20">](https://github.com/dtolnay/anyhow)
[<img alt="crates.io" src="https://img.shields.io/crates/v/anyhow.svg?style=for-the-badge&color=fc8d62&logo=rust" height="20">](https://crates.io/crates/anyhow)
[<img alt="docs.rs" src="https://img.shields.io/badge/docs.rs-anyhow-66c2a5?style=for-the-badge&labelColor=555555&logo=docs.rs" height="20">](https://docs.rs/anyhow)
[<img alt="build status" src="https://img.shields.io/github/actions/workflow/status/dtolnay/anyhow/ci.yml?branch=master&style=for-the-badge" height="20">](https://github.com/dtolnay/anyhow/actions?query=branch%3Amaster)

This library provides [`anyhow::Error`][Error], a trait object based error type
for easy idiomatic error handling in Rust applications.

[Error]: https://docs.rs/anyhow/1.0/anyhow/struct.Error.html

```toml
[dependencies]
anyhow = "1.0"
```

<br>

## Details

- Use `Result<T, anyhow::Error>`, or equivalently `anyhow::Result<T>`, as the
  return type of any fallible function.

  Within the function, use `?` to easily propagate any error that implements the
  [`std::error::Error`] trait.

  ```rust
  use anyhow::Result;

  fn get_cluster_info() -> Result<ClusterMap> {
      let config = std::fs::read_to_string("cluster.json")?;
      let map: ClusterMap = serde_json::from_str(&config)?;
      Ok(map)
  }
  ```

  [`std::error::Error`]: https://doc.rust-lang.org/std/error/trait.Error.html

- Attach context to help the person troubleshooting the error understand where
  things went wrong. A low-level error like "No such file or directory" can be
  annoying to debug without more context about what higher level step the
  application was in the middle of.

  ```rust
  use anyhow::{Context, Result};

  fn main() -> Result<()> {
      ...
      it.detach().context("Failed to detach the important thing")?;

      let content = std::fs::read(path)
          .with_context(|| format!("Failed to read instrs from {}", path))?;
      ...
  }
  ```

  ```console
  Error: Failed to read instrs from ./path/to/instrs.json

  Caused by:
      No such file or directory (os error 2)
  ```

- Downcasting is supported and can be by value, by shared reference, or by
  mutable reference as needed.

  ```rust
  // If the error was caused by redaction, then return a
  // tombstone instead of the content.
  match root_cause.downcast_ref::<DataStoreError>() {
      Some(DataStoreError::Censored(_)) => Ok(Poll::Ready(REDACTED_CONTENT)),
      None => Err(error),
  }
  ```

- If using Rust &ge; 1.65, a backtrace is captured and printed with the error if
  the underlying error type does not already provide its own. In order to see
  backtraces, they must be enabled through the environment variables described
  in [`std::backtrace`]:

  - If you want panics and errors to both have backtraces, set
    `RUST_BACKTRACE=1`;
  - If you want only errors to have backtraces, set `RUST_LIB_BACKTRACE=1`;
  - If you want only panics to have backtraces, set `RUST_BACKTRACE=1` and
    `RUST_LIB_BACKTRACE=0`.

  [`std::backtrace`]: https://doc.rust-lang.org/std/backtrace/index.html#environment-variables

- Anyhow works with any error type that has an impl of `std::error::Error`,
  including ones defined in your crate. We do not bundle a `derive(Error)` macro
  but you can write the impls yourself or use a standalone macro like
  [thiserror].

  ```rust
  use thiserror::Error;

  #[derive(Error, Debug)]
  pub enum FormatError {
      #[error("Invalid header (expected {expected:?}, got {found:?})")]
      InvalidHeader {
          expected: String,
          found: String,
      },
      #[error("Missing attribute: {0}")]
      MissingAttribute(String),
  }
  ```

- One-off error messages can be constructed using the `anyhow!` macro, which
  supports string interpolation and produces an `anyhow::Error`.

  ```rust
  return Err(anyhow!("Missing attribute: {}", missing));
  ```

  A `bail!` macro is provided as a shorthand for the same early return.

  ```rust
  bail!("Missing attribute: {}", missing);
  ```

<br>

## No-std support

In no_std mode, almost all of the same API is available and works the same way.
To depend on Anyhow in no_std mode, disable our default enabled "std" feature in
Cargo.toml. A global allocator is required.

```toml
[dependencies]
anyhow = { version = "1.0", default-features = false }
```

With versions of Rust older than 1.81, no_std mode may require an additional
`.map_err(Error::msg)` when working with a non-Anyhow error type inside a
function that returns Anyhow's error type, as the trait that `?`-based error
conversions are defined by is only available in std in those old versions.

<br>

## Comparison to failure

The `anyhow::Error` type works something like `failure::Error`, but unlike
failure ours is built around the standard library's `std::error::Error` trait
rather than a separate trait `failure::Fail`. The standard library has adopted
the necessary improvements for this to be possible as part of [RFC 2504].

[RFC 2504]: https://github.com/rust-lang/rfcs/blob/master/text/2504-fix-error.md

<br>

## Comparison to thiserror

Use Anyhow if you don't care what error type your functions return, you just
want it to be easy. This is common in application code. Use [thiserror] if you
are a library that wants to design your own dedicated error type(s) so that on
failures the caller gets exactly the information that you choose.

[thiserror]: https://github.com/dtolnay/thiserror

<br>

#### License

<sup>
Licensed under either of <a href="LICENSE-APACHE">Apache License, Version
2.0</a> or <a href="LICENSE-MIT">MIT license</a> at your option.
</sup>

<br>

<sub>
Unless you explicitly state otherwise, any contribution intentionally submitted
for inclusion in this crate by you, as defined in the Apache-2.0 license, shall
be dual licensed as above, without any additional terms or conditions.
</sub>

---

## About this Kotlin port

### Installation

```kotlin
dependencies {
    implementation("io.github.kotlinmania:anyhow-kotlin:0.1.0-SNAPSHOT")
}
```

### Building

```bash
./gradlew build
./gradlew test
```

### Targets

- macOS arm64
- Linux x64
- Windows mingw-x64
- iOS arm64 / simulator-arm64 (Swift export + XCFramework)
- JS (browser + Node.js)
- Wasm-JS (browser + Node.js)
- Android (API 24+)

### Porting guidelines

See [AGENTS.md](AGENTS.md) and [CLAUDE.md](CLAUDE.md) for translator discipline, port-lint header convention, and Rust → Kotlin idiom mapping.

### License

This Kotlin port is distributed under the same MIT license as the upstream [`dtolnay/anyhow`](https://github.com/dtolnay/anyhow). See [LICENSE](LICENSE) (and any sibling `LICENSE-*` / `NOTICE` files mirrored from upstream) for the full text.

Original work copyrighted by the anyhow authors.  
Kotlin port: Copyright (c) 2026 Sydney Renee and The Solace Project.

### Acknowledgments

Thanks to the [`dtolnay/anyhow`](https://github.com/dtolnay/anyhow) maintainers and contributors for the original Rust implementation. This port reproduces their work in Kotlin Multiplatform; bug reports about upstream design or behavior should go to the upstream repository.

# port-lint Proposed Changes

**Generated:** 2026-05-22
**Source:** tmp/anyhow
**Target:** src

These are review proposals only. They are emitted when a Rust -> Kotlin pair matches only after fallback normalization, so the existing `port-lint` header is not an exact provenance match.

| Target file | Current header | Proposed header | Source path | Reason |
|-------------|----------------|-----------------|-------------|--------|
| `commonMain/kotlin/io/github/kotlinmania/anyhow/Error.kt` | `// port-lint: source error.rs` | `// port-lint: source error.rs` | `error.rs` | `port-lint provenance header matched only after fallback normalization: 'error.rs' vs expected 'error.rs'` |
| `commonMain/kotlin/io/github/kotlinmania/anyhow/Ptr.kt` | `// port-lint: source ptr.rs` | `// port-lint: source ptr.rs` | `ptr.rs` | `port-lint provenance header matched only after fallback normalization: 'ptr.rs' vs expected 'ptr.rs'` |
| `commonMain/kotlin/io/github/kotlinmania/anyhow/Backtrace.kt` | `// port-lint: source backtrace.rs` | `// port-lint: source backtrace.rs` | `backtrace.rs` | `port-lint provenance header matched only after fallback normalization: 'backtrace.rs' vs expected 'backtrace.rs'` |
| `commonMain/kotlin/io/github/kotlinmania/anyhow/Chain.kt` | `// port-lint: source chain.rs` | `// port-lint: source chain.rs` | `chain.rs` | `port-lint provenance header matched only after fallback normalization: 'chain.rs' vs expected 'chain.rs'` |
| `commonMain/kotlin/io/github/kotlinmania/anyhow/Fmt.kt` | `// port-lint: source fmt.rs` | `// port-lint: source fmt.rs` | `fmt.rs` | `port-lint provenance header matched only after fallback normalization: 'fmt.rs' vs expected 'fmt.rs'` |
| `commonTest/kotlin/io/github/kotlinmania/anyhow/FmtTest.kt` | `// port-lint: source fmt.rs` | `// port-lint: source fmt.rs` | `fmt.rs` | `port-lint provenance header matched only after fallback normalization: 'fmt.rs' vs expected 'fmt.rs'` |
| `commonMain/kotlin/io/github/kotlinmania/anyhow/Context.kt` | `// port-lint: source context.rs` | `// port-lint: source context.rs` | `context.rs` | `port-lint provenance header matched only after fallback normalization: 'context.rs' vs expected 'context.rs'` |
| `commonMain/kotlin/io/github/kotlinmania/anyhow/Ensure.kt` | `// port-lint: source ensure.rs` | `// port-lint: source ensure.rs` | `ensure.rs` | `port-lint provenance header matched only after fallback normalization: 'ensure.rs' vs expected 'ensure.rs'` |
| `commonMain/kotlin/io/github/kotlinmania/anyhow/Nightly.kt` | `// port-lint: source nightly.rs` | `// port-lint: source nightly.rs` | `nightly.rs` | `port-lint provenance header matched only after fallback normalization: 'nightly.rs' vs expected 'nightly.rs'` |
| `commonMain/kotlin/io/github/kotlinmania/anyhow/Lib.kt` | `// port-lint: source lib.rs` | `// port-lint: source lib.rs` | `lib.rs` | `port-lint provenance header matched only after fallback normalization: 'lib.rs' vs expected 'lib.rs'` |
| `commonMain/kotlin/io/github/kotlinmania/anyhow/Wrapper.kt` | `// port-lint: source wrapper.rs` | `// port-lint: source wrapper.rs` | `wrapper.rs` | `port-lint provenance header matched only after fallback normalization: 'wrapper.rs' vs expected 'wrapper.rs'` |
| `commonMain/kotlin/io/github/kotlinmania/anyhow/Kind.kt` | `// port-lint: source kind.rs` | `// port-lint: source kind.rs` | `kind.rs` | `port-lint provenance header matched only after fallback normalization: 'kind.rs' vs expected 'kind.rs'` |
| `commonMain/kotlin/io/github/kotlinmania/anyhow/Macros.kt` | `// port-lint: source macros.rs` | `// port-lint: source macros.rs` | `macros.rs` | `port-lint provenance header matched only after fallback normalization: 'macros.rs' vs expected 'macros.rs'` |

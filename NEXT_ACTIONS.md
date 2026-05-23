# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 13/36 (36.1%)
- **Function parity:** 79/219 matched (target 154) — 36.1%
- **Class/type parity:** 32/63 matched (target 48) — 50.8%
- **Combined symbol parity:** 111/282 matched (target 202) — 39.4%
- **Average inline-code cosine:** 0.32 (function body across 13 matched files)
- **Average documentation cosine:** 0.11 (doc text across 13 matched files)
- **Cheat-zeroed Files:** 2
- **Critical Issues:** 11 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. error

- **Target:** `anyhow.Error [PROVENANCE-FALLBACK]`
- **Similarity:** 0.31
- **Dependents:** 8
- **Priority Score:** 8154807.0
- **Functions:** 30/44 matched (target 56)
- **Missing functions:** `is`, `provide`, `thiserror_provide`, `from`, `deref`, `deref_mut`, `fmt`, `drop`, `object_drop`, `no_backtrace`, `context_backtrace`, `error`, `error_mut`, `as_ref`
- **Types:** 3/4 matched
- **Missing types:** `Target`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `error.rs` vs expected `error.rs`
- **Proposed provenance header:** `// port-lint: source error.rs` (current: `// port-lint: source error.rs`)
- **Lint issues:** 1

### 2. ptr

- **Target:** `anyhow.Ptr [PROVENANCE-FALLBACK]`
- **Similarity:** 0.37
- **Dependents:** 3
- **Priority Score:** 3011706.2
- **Functions:** 12/12 matched (target 20)
- **Missing functions:** _none_
- **Types:** 4/5 matched (target 4)
- **Missing types:** `Target`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `ptr.rs` vs expected `ptr.rs`
- **Proposed provenance header:** `// port-lint: source ptr.rs` (current: `// port-lint: source ptr.rs`)
- **Lint issues:** 1

### 3. backtrace

- **Target:** `anyhow.Backtrace [PROVENANCE-FALLBACK]`
- **Similarity:** 0.08
- **Dependents:** 2
- **Priority Score:** 2151909.2
- **Functions:** 2/11 matched (target 6)
- **Missing functions:** `fmt`, `enabled`, `create`, `new`, `force`, `resolve`, `output_filename`, `_assert_send_sync`, `assert`
- **Types:** 2/8 matched (target 2)
- **Missing types:** `Inner`, `Capture`, `BacktraceFrame`, `BacktraceSymbol`, `BytesOrWide`, `LazilyResolvedCapture`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `backtrace.rs` vs expected `backtrace.rs`
- **Proposed provenance header:** `// port-lint: source backtrace.rs` (current: `// port-lint: source backtrace.rs`)
- **Lint issues:** 1

### 4. chain

- **Target:** `anyhow.Chain [PROVENANCE-FALLBACK]`
- **Similarity:** 0.42
- **Dependents:** 2
- **Priority Score:** 2010905.9
- **Functions:** 6/6 matched (target 13)
- **Missing functions:** _none_
- **Types:** 2/3 matched (target 4)
- **Missing types:** `Item`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `chain.rs` vs expected `chain.rs`
- **Proposed provenance header:** `// port-lint: source chain.rs` (current: `// port-lint: source chain.rs`)
- **Lint issues:** 1

### 5. fmt

- **Target:** `anyhow.Fmt [PROVENANCE-FALLBACK]`
- **Similarity:** 0.73
- **Dependents:** 1
- **Priority Score:** 1000702.7
- **Functions:** 6/6 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 3/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `fmt.rs` vs expected `fmt.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `fmt.rs` vs expected `fmt.rs`
- **Proposed provenance header:** `// port-lint: source fmt.rs` (current: `// port-lint: source fmt.rs`)
- **Proposed provenance header:** `// port-lint: source fmt.rs` (current: `// port-lint: source fmt.rs`)
- **Lint issues:** 2

### 6. context

- **Target:** `anyhow.Context [PROVENANCE-FALLBACK]`
- **Similarity:** 0.22
- **Dependents:** 0
- **Priority Score:** 31007.8
- **Functions:** 4/7 matched (target 10)
- **Missing functions:** `fmt`, `source`, `provide`
- **Types:** 3/3 matched (target 8)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `context.rs` vs expected `context.rs`
- **Proposed provenance header:** `// port-lint: source context.rs` (current: `// port-lint: source context.rs`)
- **Lint issues:** 1

### 7. ensure

- **Target:** `anyhow.Ensure [PROVENANCE-FALLBACK]`
- **Similarity:** 0.37
- **Dependents:** 0
- **Priority Score:** 30806.3
- **Functions:** 2/5 matched (target 8)
- **Missing functions:** `new`, `as_str`, `write_str`
- **Types:** 3/3 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `ensure.rs` vs expected `ensure.rs`
- **Proposed provenance header:** `// port-lint: source ensure.rs` (current: `// port-lint: source ensure.rs`)
- **Lint issues:** 1

### 8. nightly

- **Target:** `anyhow.Nightly [PROVENANCE-FALLBACK]`
- **Similarity:** 0.35
- **Dependents:** 0
- **Priority Score:** 30606.5
- **Functions:** 3/5 matched (target 4)
- **Missing functions:** `fmt`, `request_ref`
- **Types:** 0/1 matched (target 2)
- **Missing types:** `MyError`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `nightly.rs` vs expected `nightly.rs`
- **Proposed provenance header:** `// port-lint: source nightly.rs` (current: `// port-lint: source nightly.rs`)
- **Lint issues:** 1

### 9. lib

- **Target:** `anyhow.Lib [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 21110.0
- **Functions:** 5/5 matched (target 7)
- **Missing functions:** _none_
- **Types:** 4/6 matched (target 5)
- **Missing types:** `Chain`, `Bool`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `lib.rs` vs expected `lib.rs`
- **Proposed provenance header:** `// port-lint: source lib.rs` (current: `// port-lint: source lib.rs`)
- **Lint issues:** 1

### 10. wrapper

- **Target:** `anyhow.Wrapper [PROVENANCE-FALLBACK]`
- **Similarity:** 0.09
- **Dependents:** 0
- **Priority Score:** 20609.1
- **Functions:** 1/3 matched (target 4)
- **Missing functions:** `fmt`, `provide`
- **Types:** 3/3 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `wrapper.rs` vs expected `wrapper.rs`
- **Proposed provenance header:** `// port-lint: source wrapper.rs` (current: `// port-lint: source wrapper.rs`)
- **Lint issues:** 1

### 11. tests.test_source

- **Target:** `anyhow.TestSource`
- **Similarity:** 0.47
- **Dependents:** 0
- **Priority Score:** 10805.3
- **Functions:** 6/7 matched (target 8)
- **Missing functions:** `fmt`
- **Types:** 1/1 matched (target 4)
- **Missing types:** _none_
- **Tests:** 5/5 matched

### 12. kind

- **Target:** `anyhow.Kind [PROVENANCE-FALLBACK]`
- **Similarity:** 0.81
- **Dependents:** 0
- **Priority Score:** 801.9
- **Functions:** 2/2 matched (target 6)
- **Missing functions:** _none_
- **Types:** 6/6 matched (target 7)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `kind.rs` vs expected `kind.rs`
- **Proposed provenance header:** `// port-lint: source kind.rs` (current: `// port-lint: source kind.rs`)
- **Lint issues:** 1

### 13. macros

- **Target:** `anyhow.Macros [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched (target 6)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `macros.rs` vs expected `macros.rs`
- **Proposed provenance header:** `// port-lint: source macros.rs` (current: `// port-lint: source macros.rs`)
- **Lint issues:** 1

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

## Reexport / Wiring Modules

These files match `reexport_modules` patterns in `.ast_distance_config.json`. They are filtered out of
normal priority and missing-file ladders because they are wiring
modules, not direct logic ports. Consult them for call-site routing;
do not treat them as the next implementation target by default.

### Missing

| Source | Expected target | Deps | Source path | Expected path |
|--------|-----------------|------|-------------|---------------|
| `common.mod` | `tests.common.Mod` | 0 | `tests/common/mod.rs` | `tests/common/Mod.kt` |
| `drop.mod` | `tests.drop.Mod` | 0 | `tests/drop/mod.rs` | `tests/drop/Mod.kt` |


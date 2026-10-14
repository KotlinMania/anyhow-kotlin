# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 12/12 (100.0%)
- **Function parity:** 85/149 matched (target 149) — 57.0%
- **Class/type parity:** 31/43 matched (target 44) — 72.1%
- **Combined symbol parity:** 116/192 matched (target 193) — 60.4%
- **Average inline-code cosine:** 0.12 (function body across 12 matched files)
- **Average documentation cosine:** 0.11 (doc text across 12 matched files)
- **Cheat-zeroed Files:** 1
- **Critical Issues:** 12 files with <0.60 function similarity
- **Needs Review:** 0 files with 0.60-0.84 function similarity
- **Excellent:** 0 files with >=0.85 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. error

- **Target:** `anyhow.Error`
- **Similarity:** 0.10
- **Dependents:** 3
- **Priority Score:** 3265909.0
- **Functions:** 30/55 matched (target 57)
- **Missing functions:** `is`, `provide`, `thiserror_provide`, `from`, `deref`, `deref_mut`, `fmt`, `fmt`, `drop`, `object_drop`, `no_backtrace`, `context_backtrace`, `error`, `error_mut`, `backtrace`, `provide`, `chain`, `provide`, `fmt`, `fmt`, `from`, `from`, `from`, `as_ref`, `as_ref`
- **Types:** 3/4 matched
- **Missing types:** `Target`

### 2. backtrace

- **Target:** `anyhow.Backtrace`
- **Similarity:** 0.01
- **Dependents:** 2
- **Priority Score:** 2192309.8
- **Functions:** 2/15 matched (target 6)
- **Missing functions:** `fmt`, `fmt`, `fmt`, `fmt`, `enabled`, `create`, `fmt`, `new`, `force`, `resolve`, `output_filename`, `_assert_send_sync`, `assert`
- **Types:** 2/8 matched (target 2)
- **Missing types:** `Inner`, `Capture`, `BacktraceFrame`, `BacktraceSymbol`, `BytesOrWide`, `LazilyResolvedCapture`

### 3. ptr

- **Target:** `anyhow.Ptr`
- **Similarity:** 0.10
- **Dependents:** 2
- **Priority Score:** 2012409.0
- **Functions:** 19/19 matched (target 20)
- **Missing functions:** _none_
- **Types:** 4/5 matched (target 4)
- **Missing types:** `Target`

### 4. chain

- **Target:** `anyhow.Chain`
- **Similarity:** 0.08
- **Dependents:** 2
- **Priority Score:** 2010909.1
- **Functions:** 6/6 matched (target 15)
- **Missing functions:** _none_
- **Types:** 2/3 matched (target 4)
- **Missing types:** `Item`

### 5. fmt

- **Target:** `anyhow.Fmt`
- **Similarity:** 0.29
- **Dependents:** 1
- **Priority Score:** 1000707.1
- **Functions:** 6/6 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 3/3 matched

### 6. context

- **Target:** `anyhow.Context`
- **Similarity:** 0.06
- **Dependents:** 0
- **Priority Score:** 101709.4
- **Functions:** 4/14 matched (target 10)
- **Missing functions:** `ext_context`, `context`, `with_context`, `fmt`, `fmt`, `source`, `provide`, `source`, `provide`, `fmt`
- **Types:** 3/3 matched (target 8)
- **Missing types:** _none_

### 7. wrapper

- **Target:** `anyhow.Wrapper`
- **Similarity:** 0.02
- **Dependents:** 0
- **Priority Score:** 71109.8
- **Functions:** 1/8 matched (target 4)
- **Missing functions:** `fmt`, `fmt`, `fmt`, `fmt`, `fmt`, `fmt`, `provide`
- **Types:** 3/3 matched
- **Missing types:** _none_

### 8. nightly

- **Target:** `anyhow.Nightly`
- **Similarity:** 0.16
- **Dependents:** 0
- **Priority Score:** 50808.4
- **Functions:** 3/7 matched (target 4)
- **Missing functions:** `fmt`, `fmt`, `request_ref`, `provide`
- **Types:** 0/1 matched (target 2)
- **Missing types:** `MyError`

### 9. lib

- **Target:** `anyhow.Lib`
- **Similarity:** 0.25
- **Dependents:** 0
- **Priority Score:** 41307.5
- **Functions:** 5/7 matched
- **Missing functions:** `not`, `not`
- **Types:** 4/6 matched (target 5)
- **Missing types:** `Chain`, `Bool`

### 10. ensure

- **Target:** `anyhow.Ensure`
- **Similarity:** 0.11
- **Dependents:** 0
- **Priority Score:** 30908.9
- **Functions:** 3/6 matched (target 8)
- **Missing functions:** `new`, `as_str`, `write_str`
- **Types:** 3/3 matched
- **Missing types:** _none_

### 11. kind

- **Target:** `anyhow.Kind`
- **Similarity:** 0.29
- **Dependents:** 0
- **Priority Score:** 1207.1
- **Functions:** 6/6 matched
- **Missing functions:** _none_
- **Types:** 6/6 matched (target 7)
- **Missing types:** _none_

### 12. macros

- **Target:** `anyhow.Macros [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched (target 6)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present


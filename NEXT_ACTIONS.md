# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 12/12 (100.0%)
- **Function parity:** 149/149 matched (target 213) — 100.0%
- **Class/type parity:** 43/43 matched (target 60) — 100.0%
- **Combined symbol parity:** 192/192 matched (target 273) — 100.0%
- **Average inline-code cosine:** 0.21 (function body across 12 matched files)
- **Average documentation cosine:** 0.20 (doc text across 12 matched files)
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
- **Similarity:** 0.26
- **Dependents:** 3
- **Priority Score:** 3005907.5
- **Functions:** 55/55 matched (target 77)
- **Missing functions:** _none_
- **Types:** 4/4 matched (target 5)
- **Missing types:** _none_

### 2. ptr

- **Target:** `anyhow.Ptr`
- **Similarity:** 0.10
- **Dependents:** 2
- **Priority Score:** 2002409.0
- **Functions:** 19/19 matched (target 20)
- **Missing functions:** _none_
- **Types:** 5/5 matched
- **Missing types:** _none_

### 3. backtrace

- **Target:** `anyhow.Backtrace`
- **Similarity:** 0.13
- **Dependents:** 2
- **Priority Score:** 2002308.8
- **Functions:** 15/15 matched (target 19)
- **Missing functions:** _none_
- **Types:** 8/8 matched (target 13)
- **Missing types:** _none_

### 4. chain

- **Target:** `anyhow.Chain`
- **Similarity:** 0.25
- **Dependents:** 2
- **Priority Score:** 2000907.5
- **Functions:** 6/6 matched (target 9)
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 5)
- **Missing types:** _none_

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
- **Similarity:** 0.21
- **Dependents:** 0
- **Priority Score:** 1707.9
- **Functions:** 14/14 matched (target 23)
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 7)
- **Missing types:** _none_

### 7. lib

- **Target:** `anyhow.Lib`
- **Similarity:** 0.35
- **Dependents:** 0
- **Priority Score:** 1306.5
- **Functions:** 7/7 matched (target 11)
- **Missing functions:** _none_
- **Types:** 6/6 matched (target 7)
- **Missing types:** _none_

### 8. kind

- **Target:** `anyhow.Kind`
- **Similarity:** 0.29
- **Dependents:** 0
- **Priority Score:** 1207.1
- **Functions:** 6/6 matched
- **Missing functions:** _none_
- **Types:** 6/6 matched (target 7)
- **Missing types:** _none_

### 9. wrapper

- **Target:** `anyhow.Wrapper`
- **Similarity:** 0.12
- **Dependents:** 0
- **Priority Score:** 1108.8
- **Functions:** 8/8 matched (target 12)
- **Missing functions:** _none_
- **Types:** 3/3 matched
- **Missing types:** _none_

### 10. ensure

- **Target:** `anyhow.Ensure`
- **Similarity:** 0.23
- **Dependents:** 0
- **Priority Score:** 907.7
- **Functions:** 6/6 matched (target 11)
- **Missing functions:** _none_
- **Types:** 3/3 matched
- **Missing types:** _none_

### 11. nightly

- **Target:** `anyhow.Nightly`
- **Similarity:** 0.31
- **Dependents:** 0
- **Priority Score:** 806.9
- **Functions:** 7/7 matched (target 13)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 3)
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


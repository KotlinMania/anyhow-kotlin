# Reverse-call source experiment

This directory preserves hand-written Swift code and six tests for a concrete MainActor-owned request. It is excluded from the root build and does not replace the Kotlin compiler, exported runtime, or Anyhow public API. The library Swift failure remains unresolved until a compatible published upstream repair passes the real library tests.

The request keeps its typed argument, operation and result on MainActor. A checked Sendable control ticket schedules zero-argument, Void-returning actor-isolated work, cancels outside the mutex, and cleans up typed results on their owner. No Sendable assertion on KotlinBase, preconcurrency import, pointer conversion, warning downgrade or generated-source patch is used. Ready notification and later cancellation are distinct state transitions. A running operation retains its state until actual completion; waiting for the operation is not a guarantee that an independently scheduled cleanup task ran.

## Current evidence and limits

Source `ReverseCall.swift` and a separate client importing actual Integration.Payload and KotlinRuntime.KotlinBase passed real Swift 6 object compilation with warnings-as-errors, zero diagnostics (session95637). That did not execute a test or link a complete library. The six lifecycle tests were unexecuted when this source preservation commit was prepared. They retain the real Payload alias and cover identity, cancellation before dispatch, failed readiness notification, cancellation during suspension, cancellation after readiness/repeated discard, and eventual frame release.

The existing exporter accepts an already opaque @Sendable @isolated(any) (Payload) async throws -> Payload. This code does not prove a compatible conversion of that interface. It requires the complete request/conversion code to be generated inside the actual owning lexical actor scope. MainActor is the concrete implementation here; other actors and arbitrary opaque callback registration remain unresolved. Do not silently replace the original API, restrict models to Sendable, replace the original22 tests, or interpret this source experiment as an official toolchain repair.

## Reproduce the isolated tests

Use the same coherent Integration Swift package, generated headers, runtime resources and libIntegration.a built from a reviewed exporter revision. They must expose the real KotlinBase-derived Payload(label:) and its label property. Record their hashes and the exact producer revision. No generated source or binary dependency is vendored here; no machine-specific path is selected by default.

Set SWIFT_REVERSE_INTEGRATION_PACKAGE to the verified generated Package.swift directory and SWIFT_REVERSE_NATIVE_PRODUCTS to the matching libIntegration.a directory. Then run from this directory:

```sh
swift test --verbose -Xswiftc -swift-version -Xswiftc 6 -Xswiftc -warnings-as-errors -Xswiftc -v > swift-tests.log 2>&1
```

Require process exit0, every one of the six named tests passing exactly once, and no skipped tests. Inspect actual swift-frontend product/test commands: every module must use language mode6 and warnings-as-errors. Classify Package.swift configuration commands separately; a test-target flag alone does not prove dependency compilation. Preserve failures and raw logs. The current evidence does not establish foreign completion exactly once, all cancellation/error interleavings, arbitrary actor compatibility, or the original22 reverse bridge behaviors.

The temporary local package selection above is solely for the isolated compiler experiment. Adoption in Anyhow requires an explicitly authorized immutable published coordinate, independently fetched artifact hashes, coherent Gradle plugin/Native/exporter ABI and runtime resources, producer behavior gates, and real Anyhow exact-head CI. An experimental JAR cannot be published or selected as an official org.jetbrains.kotlin replacement.

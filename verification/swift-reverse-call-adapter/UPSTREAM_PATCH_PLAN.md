# Kotlin upstream patch route

The existing Anyhow CI failure comes from Kotlin2.4.20 Swift export source. Its proposed source changes are based on JetBrains/kotlin revision `890ac1d94fdb80eb85f0eeb5be5e4352df987b2f`. They have not been submitted upstream. There is no full Kotlin source Git checkout in this repair; the current inputs are the existing artifact-local official files with pinned Git blobs. No complete upstream build or compatible immutable published repair exists yet.

The seven source diffs are preserved separately in the workspace at `automation-artifacts/2026-10-20-syn-isolated-library-validation/runtime-task-ownership-repair/flow-handoff-candidate-v2/forward-managed-result-source-v2/upstream-source-patch/`. Its provenance.json SHA256 is `e72bdbaee2620e8ff7b0daec1d72cb8b2a9d2edf50768515478ab0f181e1bc5a`. The file and every candidate/patch hash were independently rechecked when this plan was written. No foreign source, generated code, patch payload, JAR or native archive is copied into this directory.

| Upstream source | Base Git blob | Patch SHA256 |
| --- | --- | --- |
| `native/swift/sir-providers/src/org/jetbrains/kotlin/sir/providers/impl/BridgeProvider/TypeBridging.kt` | `e4c2729147cab80d11b3c4e82dd48862177cec7f` | `d9744986b256356658e6bfec33d23c56482c86d76fa5f21c5bc4f153dd2d85e6` |
| `native/swift/swift-export-standalone/resources/swift/KotlinCoroutineSupport.kt` | `2f1b36bcf861b444ceffa74bed066e69a4846ba9` | `1e30db15444df9f701e2f52708ebbf43739556d7e8261891f549d9f2cfb90932` |
| `native/swift/sir-printer/src/org/jetbrains/sir/printer/impl/SirAsSwiftSourcesPrinter.kt` | `c7b5f69050d8cfa2d67031b826ad98d0caa0631c` | `8699dcde7f9d749587d65c5e87d509351fb71f042ad4d3bdb49ba63e179373a4` |
| `native/swift/swift-export-standalone/resources/swift/KotlinRuntimeSupport.swift` | `e72b3dc1d6883d645f590de6b55909908b4ff237` | `821a2b07daa8a5b8c105ba88ef7d45e3e3181e657702e601f1f25a9fa08cd464` |
| `native/swift/swift-export-standalone/resources/swift/KotlinCoroutineSupport.swift` | `45f508d7d0be1f27e9d680cc77a363489925f174` | `2dd099bebc8f3d5409703acd4699878dd82042bea8ec511224bd62fbb7b2c88c` |
| `native/swift/sir-providers/src/org/jetbrains/kotlin/sir/providers/impl/BridgeProvider/SirBridgeProviderImpl.kt` | `1920fef799369534ecd3940920b3b62289b62152` | `16fb9085244ecc7bcf7342e9bccecc33ebdc98bfba31c95eef383949dc609562` |
| `native/swift/swift-export-standalone/resources/swift/KotlinCoroutineSupport.h` | `UNVERIFIED` | `1e89d71abdbd131075ff247d45df92ce4010840f9971abb8610b74f2c8ae7bc3` |

The header baseline is pinned to the official exporter resource SHA256 `62a8e7264901993a7ddd3921b455c027d160dec217904af273e84426bfb1ba50`; its upstream Git blob is still unverified. The other six baseline Git blobs are pinned by the source owner. Before applying, independently verify the exact revision, those blobs, the header blob and licenses in a real authorized Kotlin checkout. Preserve the raw patches; do not broaden bytecode normalization to hide unrelated changes.

The partial forward repair changes import handling, owned error text, coroutine cancellation/result handling, generated callback carriers and caller isolation. The isolated forward build passed; two named Swift tests and four Native cleanup checks passed with all actual product/test frontend commands using Swift6 and warnings-as-errors. Its experimental JAR hash `49851dd9c565dec2c0aab8806690946706713f42f5115e491214b78f19ed313e` is provenance only and is not an official coordinate or an authorized consumer selection.

The reverse-call source in this directory is separate. It passed strict object compilation against real retained Payload/KotlinBase declarations, but requires full caller-side code generation in the actual owning lexical isolation. It does not preserve arbitrary opaque actor-bound callback registration. Six lifecycle cases are not a replacement for the original22 producer cases or the four protocol/override cases; those acceptance gaps remain open.

## Required completion sequence

1. Resolve the existing reverse callback registration contract without silently narrowing accepted models/functions. Validate generated protocol requirements, Swift witnesses and open-class overrides on the actual installed compiler. Preserve all original22 behavior cases and add regression coverage.
2. In an authorized actual upstream Kotlin source checkout, apply the reviewed source diffs at their pinned base, verify the missing header Git provenance, and run the complete relevant generator/Native/runtime/Swift tests and ABI checks. No local toolchain injection into Anyhow establishes this result.
3. Submit the real source change through an authorized upstream route. Preserve the public API changes, Swift toolchain minimums and runtime/header coherence in the review. Neither a new official publication nor a source checkout has been authorized by this source-preservation step.
4. Obtain an immutable published compatible toolchain/exporter release through its legitimate publisher, with matching Kotlin Gradle plugin, Native compiler/runtime, generated C headers, Swift resources, coroutine ABI and variants. Do not overwrite an existing version or publish local bytes under org.jetbrains.kotlin.
5. Independently fetch published bytes and verify checksums/metadata/variants. Coordinate any catalog selection with its owner; preserve the current Kotlin pin until that scoped change is authorized.
6. Run Anyhow's real Backtrace tests and every supported runtime gate at the exact new head, then version/README, exact-final CI, guarded merge/mainCI/release/Maven checks. Also complete the existing Rust Cargo workspace/dependency/feature/behavior parity audit; a green Swift experiment does not establish port completeness.

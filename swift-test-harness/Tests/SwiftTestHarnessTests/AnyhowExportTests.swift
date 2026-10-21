import Testing
import Anyhow

@Suite struct AnyhowExportTests {
    @Test func testAnyhowErrorCreationAndMessage() {
        let err: Anyhow.Error = Anyhow.anyhow(message: "operation failed")
        #expect(err.toString() == "operation failed")
        #expect(displayString(err, alternate: false) == "operation failed")
    }

    @Test func testAnyhowResultSuccess() {
        let result: Anyhow.Result = Anyhow.Ok(value: "success value")
        #expect(result.isSuccess)
        #expect(!result.isFailure)
        #expect(result is Anyhow.Result.Ok)
        #expect((result.getOrNull() as? Swift.String) == "success value")
    }

    @Test func testNullableSuccess() {
        let result: Anyhow.Result = Anyhow.Result.Companion.shared.ok(value: nil)
        #expect(result.isSuccess)
        #expect(!result.isFailure)
        #expect(result is Anyhow.Result.Ok)
        #expect(result.getOrNull() == nil)
        #expect(result.exceptionOrNull() == nil)
    }

    @Test func testErrNull() {
        let result: Anyhow.Result = Anyhow.Result.Companion.shared.err(error: nil)
        #expect(result.isFailure)
        #expect(!result.isSuccess)
        #expect(result is Anyhow.Result.Err)
        #expect(result.getOrNull() == nil)
        #expect(result.exceptionOrNull() == nil)
    }

    @Test func testCustomErrorObjectIdentity() {
        let err: Anyhow.Error = Anyhow.anyhow(message: "custom error object")
        let result: Anyhow.Result = Anyhow.Result.Companion.shared.err(error: err)
        #expect(result.isFailure)
        #expect(!result.isSuccess)
        #expect(result is Anyhow.Result.Err)
        let extracted = result.exceptionOrNull() as? Anyhow.Error
        #expect(extracted != nil)
        #expect(extracted === err)
        #expect(extracted?.toString() == "custom error object")
    }

    @Test func testAnyhowResultFailureAndBail() {
        let result: Anyhow.Result = Anyhow.bail(message: "bad input")
        #expect(result.isFailure)
        #expect(!result.isSuccess)
        #expect(result.getOrNull() == nil)
        #expect(result is Anyhow.Result.Err)
        let err = result.exceptionOrNull() as? Anyhow.Error
        #expect(err != nil)
        #expect(err?.toString() == "bad input")
    }

    @Test func testCapturedBacktraceStatusAndText() {
        let backtrace: Backtrace = Anyhow.capture(Backtrace.Companion.shared)
        let capturedStatus: BacktraceStatus = Anyhow.status(backtrace)
        #expect(capturedStatus == .Captured)
        #expect(!backtrace.toString().isEmpty)
    }
}

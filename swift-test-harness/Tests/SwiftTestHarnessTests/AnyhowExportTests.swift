import Testing
import Anyhow

@Suite struct AnyhowExportTests {
    @Test func testSwiftModuleLoads() throws {
        #expect(Bool(true), "Anyhow swift module imported cleanly")
    }

    @Test func testCapturedBacktraceStatusAndText() {
        let backtrace: Backtrace = Anyhow.capture(Backtrace.Companion.shared)
        let capturedStatus: BacktraceStatus = Anyhow.status(backtrace)
        #expect(capturedStatus == .Captured)
        #expect(!backtrace.toString().isEmpty)
    }
}

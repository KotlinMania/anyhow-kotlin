import Testing
import Anyhow

@Suite struct AnyhowExportTests {
    @Test func testSwiftModuleLoads() throws {
        #expect(Bool(true), "Anyhow swift module imported cleanly")
    }
}

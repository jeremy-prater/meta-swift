import Foundation
import Dispatch
import Glibc
// awaiting integration of fix for swiftlang/swift#83915 before we can reenable
// C++ interop on Swift 6.2. This should be fixed in Swift 6.2.1.
// import CxxStdlib

func main() async throws {
    print("Hello, world! 👋")
    try await Task.sleep(for: .seconds(1))
    print("User: \(ProcessInfo.processInfo.fullUserName)")
    let dateFormatter = DateFormatter()
    dateFormatter.dateStyle = .full
    dateFormatter.timeStyle = .medium
    print(dateFormatter.string(from: Date()))
    throw URLError(.unknown)
}

let task = Task {
    var didCatchError = false
    do { try await main() }
    catch URLError.unknown { didCatchError = true }
    catch { fatalError() }
    assert(didCatchError)
}

RunLoop.main.run(until: Date() + 2)

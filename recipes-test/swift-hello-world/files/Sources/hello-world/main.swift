import Foundation
import Dispatch
import Glibc
import CxxStdlib

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

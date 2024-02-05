# Example of BundleBridgeSource error

To reproduce error run in command line:
```bash
$ sbt "project chipyard; runMain chipyard.example.TLAXI4StreamBlockExampleApp"
```

The error is:
```scala
[error] java.lang.ClassCastException: class freechips.rocketchip.amba.axi4stream.AXI4StreamBundle cannot be cast to class scala.runtime.Nothing$ (freechips.rocketchip.amba.axi4stream.AXI4StreamBundle is in unnamed module of loader sbt.internal.LayeredClassLoader @32b6f42; scala.runtime.Nothing$ is in unnamed module of loader sbt.internal.ScalaLibraryClassLoader @490fb4e8)
```

Which can be solved by modifying the [BundleBridge.scala](./generators/rocket-chip/src/main/scala/diplomacy/BundleBridge.scala) from:
```scala
val io: T = IO(if (inferInput) Input(chiselTypeOf(bundle)) else Flipped(chiselTypeClone(bundle)))
```
to
```scala
val io: T = IO(if (inferInput) Input(chiselTypeOf[T](bundle)) else Flipped(chiselTypeClone[T](bundle)))
```
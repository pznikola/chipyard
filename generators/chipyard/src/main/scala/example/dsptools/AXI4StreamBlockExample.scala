package chipyard.example

import chisel3._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import chisel3.util._
import dspblocks._
import freechips.rocketchip.amba.axi4stream._
import org.chipsalliance.cde.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.regmapper._
import freechips.rocketchip.tilelink._

import java.io._


/**
 * Just an example for the BundleSource problem
 */
abstract class BlockExample[D, U, E, O, B <: Data]
(implicit p: Parameters) extends DspBlock[D, U, E, O, B] with HasCSR {
  // stream node
  val streamNode = AXI4StreamIdentityNode()

  lazy val module = new LazyModuleImp(this) {
    require(streamNode.out.length == 1)
    require(streamNode.in.length  == 1)

    // IOs
    val out = streamNode.out.head._1
    val in = streamNode.in.head._1

    // Reg
    val regCount = RegInit(0.U(32.W))

    out.valid := in.valid
    out.bits.data := in.bits.data
    out.bits.last := in.bits.last
    in.ready := out.ready

    regmap(
      0x0 -> Seq(RegField.w(32, regCount))
    )
  }
}

class TLAXI4StreamBlockExample (csrAddress: AddressSet, beatBytes: Int)
                   (implicit p: Parameters) extends BlockExample[
  TLClientPortParameters, TLManagerPortParameters, TLEdgeOut, TLEdgeIn, TLBundle
] with TLHasCSR {
  val devname = "tlQueueIn"
  val devcompat = Seq("ucb-art", "dsptools")
  val device = new SimpleDevice(devname, devcompat) {
    override def describe(resources: ResourceBindings): Description = {
      val Description(name, mapping) = super.describe(resources)
      Description(name, mapping)
    }
  }
  // make diplomatic TL node for regmap
  override val mem = Some(TLRegisterNode(address = Seq(csrAddress), device = device, beatBytes = beatBytes))
}


object TLAXI4StreamBlockExampleApp extends App {
  val lazyDut = LazyModule(new TLAXI4StreamBlockExample(AddressSet(0x0, 0xFF), 4)(Parameters.empty) with TLStandaloneBlock {})
  (new ChiselStage).execute(
    Array("--target-dir", "verilog/TLAXI4StreamBlockExample"),
    Seq(ChiselGeneratorAnnotation(() => lazyDut.module))
  )
}

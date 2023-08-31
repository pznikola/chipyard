package chipyard.fpga.nexysvideo

import chisel3._
import chisel3.util._
import freechips.rocketchip.diplomacy._
import org.chipsalliance.cde.config.{Parameters}
import freechips.rocketchip.tilelink._
import freechips.rocketchip.prci.{ClockBundle, ClockBundleParameters}
import freechips.rocketchip.subsystem.{SystemBusKey}

import sifive.fpgashells.shell.xilinx._
import sifive.fpgashells.shell._
import sifive.fpgashells.clocks.{ClockGroup, ClockSinkNode, PLLFactoryKey, ResetWrangler}
import sifive.fpgashells.ip.xilinx.{IBUF, PowerOnResetFPGAOnly}

import sifive.blocks.devices.uart._

import chipyard._
import chipyard.harness._
import chipyard.iobinders.{HasIOBinders}

class NexysVideoHarness(override implicit val p: Parameters) extends NexysVideoShell {
  def dp = designParameters

  val clockOverlay = dp(ClockInputOverlayKey).map(_.place(ClockInputDesignInput())).head
  val harnessSysPLL = dp(PLLFactoryKey)
  val harnessSysPLLNode = harnessSysPLL()
  val dutFreqMHz = (dp(SystemBusKey).dtsFrequency.get / (1000 * 1000)).toInt
  val dutClock = ClockSinkNode(freqMHz = dutFreqMHz)
  println(s"NexysVideo FPGA Base Clock Freq: ${dutFreqMHz} MHz")
  val dutWrangler = LazyModule(new ResetWrangler())
  val dutGroup = ClockGroup()
  dutClock := dutWrangler.node := dutGroup := harnessSysPLLNode

  harnessSysPLLNode := clockOverlay.overlayOutput.node

  val io_uart_bb = BundleBridgeSource(() => new UARTPortIO(dp(PeripheryUARTKey).headOption.getOrElse(UARTParams(0))))
  val uartOverlay = dp(UARTOverlayKey).head.place(UARTDesignInput(io_uart_bb))

  val ddrOverlay = if (dp(DDRKey) != None) Some(dp(DDROverlayKey).head.place(DDRDesignInput(dp(ExtTLMem).get.master.base, dutWrangler.node, harnessSysPLLNode)).asInstanceOf[DDRNexysVideoPlacedOverlay]) else None
  val ddrClient = if (dp(DDRKey) != None) Some(TLClientNode(Seq(TLMasterPortParameters.v1(Seq(TLMasterParameters.v1(
    name = "chip_ddr",
    sourceId = IdRange(0, 1 << dp(ExtTLMem).get.master.idBits)
  )))))) else None
  val ddrBlockDuringReset = if (dp(DDRKey) != None) Some(LazyModule(new TLBlockDuringReset(4))) else None
  if (dp(DDRKey) != None) ddrOverlay.get.overlayOutput.ddr := ddrBlockDuringReset.get.node := ddrClient.get

  val ledOverlays = dp(LEDOverlayKey).map(_.place(LEDDesignInput()))
  val all_leds = ledOverlays.map(_.overlayOutput.led)
  val status_leds = all_leds.take(3)
  val other_leds = all_leds.drop(3)


  override lazy val module = new HarnessLikeImpl

  class HarnessLikeImpl extends Impl with HasHarnessInstantiators {
    clockOverlay.overlayOutput.node.out(0)._1.reset := ~resetPin

    val clk_100mhz = clockOverlay.overlayOutput.node.out.head._1.clock

    // Blink the status LEDs for sanity
    withClockAndReset(clk_100mhz, dutClock.in.head._1.reset) {
      val period = (BigInt(100) << 20) / status_leds.size
      val counter = RegInit(0.U(log2Ceil(period).W))
      val on = RegInit(0.U(log2Ceil(status_leds.size).W))
      status_leds.zipWithIndex.map { case (o,s) => o := on === s.U }
      counter := Mux(counter === (period-1).U, 0.U, counter + 1.U)
      when (counter === 0.U) {
        on := Mux(on === (status_leds.size-1).U, 0.U, on + 1.U)
      }
    }

    other_leds(0) := resetPin

    val ila = Module(new sifive.fpgashells.ip.xilinx.ILA_LEDS())
    ila.io.clk    := clk_100mhz
    ila.io.probe0 := dutClock.in.head._1.reset
    ila.io.probe1 := resetPin
    ila.io.probe2 := io_uart_bb.bundle.txd
    ila.io.probe3 := io_uart_bb.bundle.rxd

    harnessSysPLL.plls.foreach(_._1.getReset.get := pllReset)

    def referenceClockFreqMHz = dutFreqMHz
    def referenceClock = dutClock.in.head._1.clock
    def referenceReset = dutClock.in.head._1.reset
    def success = { require(false, "Unused"); false.B }

    if (dp(DDRKey) != None) {
      ddrOverlay.get.mig.module.clock := harnessBinderClock
      ddrOverlay.get.mig.module.reset := harnessBinderReset
      ddrBlockDuringReset.get.module.clock := harnessBinderClock
      ddrBlockDuringReset.get.module.reset := harnessBinderReset.asBool || !ddrOverlay.get.mig.module.io.port.init_calib_complete
    }

    // other_leds(6) := ddrOverlay.mig.module.io.port.init_calib_complete

    instantiateChipTops()
  }
}
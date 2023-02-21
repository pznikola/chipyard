package chipyard.harness

import chisel3._
import chisel3.util._
import chisel3.experimental.{Analog, BaseModule, DataMirror, Direction}

import freechips.rocketchip.config.{Field, Config, Parameters}
import freechips.rocketchip.diplomacy.{LazyModule, LazyModuleImpLike}
import freechips.rocketchip.amba.axi4.{AXI4Bundle, AXI4SlaveNode, AXI4MasterNode, AXI4EdgeParameters}
import freechips.rocketchip.amba.axi4stream.AXI4StreamBundle
import freechips.rocketchip.devices.debug._
import freechips.rocketchip.jtag.{JTAGIO}
import freechips.rocketchip.system.{SimAXIMem}
import freechips.rocketchip.subsystem._
import freechips.rocketchip.util._

import sifive.blocks.devices.gpio._
import sifive.blocks.devices.uart._
import sifive.blocks.devices.spi._

import barstools.iocell.chisel._

import testchipip._

import chipyard.{HasHarnessSignalReferences, HarnessClockInstantiatorKey}
import chipyard.clocking.{HasChipyardPRCI}
import chipyard.iobinders.{GetSystemParameters, JTAGChipIO, ClockWithFreq}

import tracegen.{TraceGenSystemModuleImp}
import icenet.{CanHavePeripheryIceNIC, SimNetwork, NicLoopback, NICKey, NICIOvonly}

import scala.reflect.{ClassTag}

import dissertation._

class WithTiedOffDissertation extends OverrideHarnessBinder({
  (system: CanHavePeripheryAXI4Dissertation, th: HasHarnessSignalReferences, ports: Seq[AXI4DissertationIO[AXI4StreamBundle]]) => {
    val p: Parameters = chipyard.iobinders.GetSystemParameters(system)
    ports.map { case port => AXI4DissertationAdapter.tieoff(port) }
  }
})
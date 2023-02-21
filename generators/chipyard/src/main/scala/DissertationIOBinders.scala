package chipyard.iobinders

import chisel3._
import chisel3.experimental.{Analog, IO, DataMirror}

import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.devices.debug._
import freechips.rocketchip.jtag.{JTAGIO}
import freechips.rocketchip.subsystem._
import freechips.rocketchip.system.{SimAXIMem}
import freechips.rocketchip.amba.axi4.{AXI4Bundle, AXI4SlaveNode, AXI4MasterNode, AXI4EdgeParameters}
import freechips.rocketchip.amba.axi4stream.AXI4StreamBundle
import freechips.rocketchip.util._
import freechips.rocketchip.prci._
import freechips.rocketchip.groundtest.{GroundTestSubsystemModuleImp, GroundTestSubsystem}

import sifive.blocks.devices.gpio._
import sifive.blocks.devices.uart._
import sifive.blocks.devices.spi._
import tracegen.{TraceGenSystemModuleImp}

import barstools.iocell.chisel._

import testchipip._
import icenet.{CanHavePeripheryIceNIC, SimNetwork, NicLoopback, NICKey, NICIOvonly}
import chipyard.clocking.{HasChipyardPRCI, DividerOnlyClockGenerator}

import scala.reflect.{ClassTag}

import dissertation._

class WithDissertationPunchthrough extends OverrideLazyIOBinder({
  (system: CanHavePeripheryAXI4Dissertation) => {
    implicit val p: Parameters = GetSystemParameters(system)

    InModuleBody {
      val ports: Seq[AXI4DissertationIO[AXI4StreamBundle]] = {
        val pins = IO(new AXI4DissertationIO(Vec(system.dissertation.get(0).length,Flipped(system.dissertation.get(0).head.cloneType)), 
                                             Vec(system.dissertation.get(1).length,Flipped(system.dissertation.get(1).head.cloneType)), 
                                             Vec(system.dissertation.get(2).length,Flipped(system.dissertation.get(2).head.cloneType))))
        (system.dissertation.get(0), pins.io_in_1D).zipped.map { (out, in) => { out <> in }}
        (pins.io_out_1D, system.dissertation.get(1)).zipped.map{ (out, in) => { out <> in }}
        (pins.io_out_1D, system.dissertation.get(2)).zipped.map{ (out, in) => { out <> in }}
        Seq(pins)
      }
      (ports, Nil)
    }
  }
})
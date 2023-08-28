package chipyard.fpga.nexysvideo

import chisel3._

import freechips.rocketchip.jtag.{JTAGIO}
import freechips.rocketchip.subsystem.{PeripheryBusKey}
import freechips.rocketchip.tilelink.{TLBundle}
import freechips.rocketchip.util.{HeterogeneousBag}
import freechips.rocketchip.diplomacy.{LazyRawModuleImp}

import sifive.blocks.devices.uart.{UARTPortIO, HasPeripheryUARTModuleImp, UARTParams}
import sifive.blocks.devices.jtag.{JTAGPins, JTAGPinsFromPort}
import sifive.blocks.devices.pinctrl.{BasePin}

import sifive.fpgashells.ip.xilinx.{IBUFG, IOBUF, PULLUP, PowerOnResetFPGAOnly}

import chipyard._
import chipyard.harness._
import chipyard.iobinders.JTAGChipIO

import testchipip._

class WithNexysVideoUARTTSI(uartBaudRate: BigInt = 115200) extends OverrideHarnessBinder({
  (system: CanHavePeripheryTLSerial, th: HasHarnessInstantiators, ports: Seq[ClockedIO[SerialIO]]) => {
    implicit val p = chipyard.iobinders.GetSystemParameters(system)
    ports.map({ port =>
      val ath = th.asInstanceOf[LazyRawModuleImp].wrapper.asInstanceOf[NexysVideoHarness]
      val freq = p(PeripheryBusKey).dtsFrequency.get
      val bits = port.bits
      port.clock := th.harnessBinderClock
      val ram = TSIHarness.connectRAM(system.serdesser.get, bits, th.harnessBinderReset)
      val uart_to_serial = Module(new UARTToSerial(freq, UARTParams(0, initBaudRate=uartBaudRate)))
      val serial_width_adapter = Module(new SerialWidthAdapter(narrowW = 8, wideW = TSI.WIDTH))
      serial_width_adapter.io.narrow.flipConnect(uart_to_serial.io.serial)

      ram.module.io.tsi.flipConnect(serial_width_adapter.io.wide)

      ath.io_uart_bb.bundle <> uart_to_serial.io.uart
      // ath.other_leds(0) := uart_to_serial.io.dropped

      ath.other_leds(1) := ram.module.io.tsi2tl_state(0)
      ath.other_leds(2) := ram.module.io.tsi2tl_state(1)
      ath.other_leds(3) := ram.module.io.tsi2tl_state(2)
      ath.other_leds(4) := ram.module.io.tsi2tl_state(3)
    })
  }
})

class WithNexysVideoDDRTL extends OverrideHarnessBinder({
  (system: CanHaveMasterTLMemPort, th: HasHarnessInstantiators, ports: Seq[HeterogeneousBag[TLBundle]]) => {
    require(ports.size == 1)
    val artyTh = th.asInstanceOf[LazyRawModuleImp].wrapper.asInstanceOf[NexysVideoHarness]
    val bundles = artyTh.ddrClient.out.map(_._1)
    val ddrClientBundle = Wire(new HeterogeneousBag(bundles.map(_.cloneType)))
    bundles.zip(ddrClientBundle).foreach { case (bundle, io) => bundle <> io }
    ddrClientBundle <> ports.head
  }
})

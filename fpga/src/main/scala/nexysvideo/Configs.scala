// See LICENSE for license details.
package chipyard.fpga.nexysvideo

import org.chipsalliance.cde.config._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.devices.debug._
import freechips.rocketchip.devices.tilelink._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.system._
import freechips.rocketchip.tile._

import sifive.blocks.devices.uart._
import sifive.fpgashells.shell.{DesignKey}

import testchipip.{SerialTLKey}

import chipyard.{BuildSystem}

// don't use FPGAShell's DesignKey
class WithNoDesignKey extends Config((site, here, up) => {
  case DesignKey => (p: Parameters) => new SimpleLazyModule()(p)
})


// class WithNexysVideoTweaks extends Config(
//   new WithNexysVideoUARTTSI ++
//   new WithNoDesignKey ++
//   new chipyard.config.WithNoDebug ++ // no jtag
//   new chipyard.config.WithNoUART ++ // use UART for the UART-TSI thing instad
//   new chipyard.config.WithTLBackingMemory ++ // FPGA-shells converts the AXI to TL for us
//   new freechips.rocketchip.subsystem.WithoutTLMonitors)

// class RocketNexysVideoConfig extends Config(
//   new WithNexysVideoTweaks ++
//   new chipyard.config.WithMemoryBusFrequency(50.0) ++
//   new chipyard.config.WithSystemBusFrequency(50.0) ++
//   new chipyard.config.WithPeripheryBusFrequency(50.0) ++
//   new chipyard.config.WithBroadcastManager ++ // no l2
//   new chipyard.TinyRocketConfig)


class WithNexysVideoTweaks extends Config(
  new chipyard.harness.WithAllClocksFromHarnessClockInstantiator ++
  new WithNexysVideoUARTTSI ++
  new WithNexysVideoDDRTL ++
  new WithNoDesignKey ++
  new chipyard.harness.WithHarnessBinderClockFreqMHz(50) ++
  new chipyard.config.WithMemoryBusFrequency(50.0) ++
  new chipyard.config.WithSystemBusFrequency(50.0) ++
  new chipyard.config.WithPeripheryBusFrequency(50.0) ++
  new chipyard.harness.WithAllClocksFromHarnessClockInstantiator ++
  new chipyard.clocking.WithPassthroughClockGenerator ++
  new chipyard.config.WithNoDebug ++ // no jtag
  new chipyard.config.WithNoUART ++ // use UART for the UART-TSI thing instad
  new chipyard.config.WithTLBackingMemory ++ // FPGA-shells converts the AXI to TL for us
  new freechips.rocketchip.subsystem.WithExtMemSize(BigInt(512) << 20) ++ // 512mb on Nexys Video
  new freechips.rocketchip.subsystem.WithoutTLMonitors)

class RocketNexysVideoConfig extends Config(
  new WithNexysVideoTweaks ++
  new chipyard.config.WithBroadcastManager ++ // no l2
  new chipyard.RocketConfig)


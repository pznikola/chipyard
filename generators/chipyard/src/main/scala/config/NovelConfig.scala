package chipyard

import org.chipsalliance.cde.config.{Config}
import freechips.rocketchip.diplomacy.{AsynchronousCrossing}

// --------------
// Configs Configs
// --------------

class NovelIbexConfig extends Config(
  new ibex.WithNIbexCores(1) ++
  new chipyard.config.AbstractConfig)

class NovelCVA6Config extends Config(
  new chipyard.harness.WithSerialAdapterTiedOff ++ // Tie off the serial port, override default instantiation of SimSerial
  new chipyard.config.WithDMIDTM ++                // have debug module expose a clocked DMI port
  new cva6.WithNCVA6Cores(1) ++                    // single CVA6 core
  new chipyard.config.AbstractConfig)

class NovelSodor5StageConfig extends Config(
  // Create a Sodor 5-stage core
  new sodor.common.WithNSodorCores(1, internalTile = sodor.common.Stage5Factory) ++
  new testchipip.WithSerialTLWidth(32) ++
  new testchipip.WithSerialPBusMem ++
  new freechips.rocketchip.subsystem.WithScratchpadsOnly ++    // use sodor tile-internal scratchpad
  new freechips.rocketchip.subsystem.WithNoMemPort ++          // use no external memory
  new freechips.rocketchip.subsystem.WithNBanks(0) ++
  new chipyard.config.AbstractConfig)

class NovelSodor2StageConfig extends Config(
  // Create a Sodor 2-stage core
  new sodor.common.WithNSodorCores(1, internalTile = sodor.common.Stage2Factory) ++
  new testchipip.WithSerialTLWidth(32) ++
  new testchipip.WithSerialPBusMem ++
  new freechips.rocketchip.subsystem.WithScratchpadsOnly ++    // use sodor tile-internal scratchpad
  new freechips.rocketchip.subsystem.WithNoMemPort ++          // use no external memory
  new freechips.rocketchip.subsystem.WithNBanks(0) ++
  new chipyard.config.AbstractConfig)

class NovelTinyRocketConfig extends Config(
  new chipyard.config.WithTLSerialLocation(
    freechips.rocketchip.subsystem.FBUS,
    freechips.rocketchip.subsystem.PBUS) ++                       // attach TL serial adapter to f/p busses
  new freechips.rocketchip.subsystem.WithIncoherentBusTopology ++ // use incoherent bus topology
  new freechips.rocketchip.subsystem.WithNBanks(0) ++             // remove L2$
  new freechips.rocketchip.subsystem.WithNoMemPort ++             // remove backing memory
  new freechips.rocketchip.subsystem.With1TinyCore ++             // single tiny rocket-core
  new chipyard.config.AbstractConfig)
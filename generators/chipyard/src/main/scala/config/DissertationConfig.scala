package chipyard

import freechips.rocketchip.config.{Config}
import freechips.rocketchip.diplomacy.{AsynchronousCrossing}

import dissertation._

// --------------
// Rocket Configs
// --------------

class DissertationRocketConfig extends Config(
    new dissertation.WithAXI4Dissertation((new DissertationMeasurementParams(rangeFFTSize = 1024, dopplerFFTSize = 256, ddrType = NoDDR, channels = 4)).params) ++
    new chipyard.iobinders.WithDissertationPunchthrough ++
    new chipyard.harness.WithTiedOffDissertation        ++
    new freechips.rocketchip.subsystem.WithNBigCores(1) ++         // single rocket-core
    new chipyard.config.AbstractConfig)

class DissertationMedRocketConfig extends Config(
    new dissertation.WithAXI4Dissertation((new DissertationMeasurementParams(rangeFFTSize = 1024, dopplerFFTSize = 256, ddrType = NoDDR, channels = 4)).params) ++
    new chipyard.iobinders.WithDissertationPunchthrough ++
    new chipyard.harness.WithTiedOffDissertation        ++
    new freechips.rocketchip.subsystem.WithNMedCores(1) ++         // single rocket-core
    new chipyard.config.AbstractConfig)

class DissertationTinyRocketConfig extends Config(
    new dissertation.WithAXI4Dissertation((new DissertationMeasurementParams(rangeFFTSize = 1024, dopplerFFTSize = 256, ddrType = NoDDR, channels = 4)).params) ++
    new chipyard.iobinders.WithDissertationPunchthrough ++
    new chipyard.harness.WithTiedOffDissertation        ++
    new chipyard.config.WithTLSerialLocation(
        freechips.rocketchip.subsystem.FBUS,
        freechips.rocketchip.subsystem.PBUS) ++                     // attach TL serial adapter to f/p busses
    new freechips.rocketchip.subsystem.WithIncoherentBusTopology ++ // use incoherent bus topology
    new freechips.rocketchip.subsystem.WithNBanks(0) ++             // remove L2$
    new freechips.rocketchip.subsystem.WithNoMemPort ++             // remove backing memory
    new freechips.rocketchip.subsystem.With1TinyCore ++             // single tiny rocket-core
    new chipyard.config.AbstractConfig)

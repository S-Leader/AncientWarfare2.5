package net.shadowmage.ancientwarfare.structure.worldgen;

import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;
import org.apache.logging.log4j.util.Supplier;

public class WorldGenDetailedLogHelper {
    public static boolean shouldLogValidationMessages = false;

    public static void log(String msg, Supplier<?>... paramSuppliers) {
        if (shouldLogValidationMessages) {
            AncientWarfareStructure.LOG.debug(msg, paramSuppliers);
        }
    }
}

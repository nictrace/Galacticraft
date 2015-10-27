package micdoodle8.mods.galacticraft.planets.venus;

import cpw.mods.fml.common.FMLLog;
import micdoodle8.mods.galacticraft.core.Constants;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static net.minecraftforge.common.config.Configuration.CATEGORY_GENERAL;

public class ConfigManagerVenus
{
    public static boolean loaded;

    static Configuration config;

    public ConfigManagerVenus(File file)
    {
        if (!ConfigManagerVenus.loaded)
        {
            ConfigManagerVenus.config = new Configuration(file);
            ConfigManagerVenus.syncConfig(true);
        }
    }

    // DIMENSIONS
    public static int dimensionIDVenus;

    // SCHEMATIC

    // GENERAL

    // WGEN

    public static void syncConfig(boolean load)
    {
        List<String> propOrder = new ArrayList<String>();

        try
        {
            Property prop;

            if (!config.isChild)
            {
                if (load)
                {
                    config.load();
                }
            }

            prop = config.get(Constants.CONFIG_CATEGORY_DIMENSIONS, "dimensionIDVenus", -31);
            prop.comment = "Dimension ID for Venus";
            prop.setLanguageKey("gc.configgui.dimensionIDVenus").setRequiresMcRestart(true);
            dimensionIDVenus = prop.getInt();
            propOrder.add(prop.getName());

            //

            //

            config.setCategoryPropertyOrder(CATEGORY_GENERAL, propOrder);

            if (config.hasChanged())
            {
                config.save();
            }
        }
        catch (final Exception e)
        {
            FMLLog.log(Level.ERROR, e, "Galacticraft Venus (Planets) has a problem loading it's config");
        }
    }
}

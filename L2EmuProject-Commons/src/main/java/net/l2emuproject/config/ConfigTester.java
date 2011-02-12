package net.l2emuproject.config;

import net.l2emuproject.config.annotation.ConfigClass;
import net.l2emuproject.config.annotation.ConfigField;
import net.l2emuproject.config.annotation.ConfigGroupBeginning;
import net.l2emuproject.config.annotation.ConfigGroupEnding;
import net.l2emuproject.config.gui.Configurator;
import net.l2emuproject.config.model.ConfigClassInfo;
import net.l2emuproject.config.model.ConfigClassInfo.PrintMode;

@ConfigClass(folderName = "config", fileName = "test")
public class ConfigTester
{
	@ConfigGroupBeginning(name = "OUTER GROUP", comment = "group start comment")
	@ConfigField(name = "configValue1", value = "default1")
	public static String TEST1;
	
	@ConfigGroupBeginning(name = "MIXED GROUP", comment = { "line1", "line2", "line3" })
	@ConfigField(name = "configValue2", value = "default2")
	public static String TEST2;
	
	@ConfigGroupBeginning(name = "INNER GROUP", comment = "group start comment")
	@ConfigField(name = "configValue3", value = "default3")
	@ConfigGroupEnding(name = "INNER GROUP", comment = "group end comment")
	public static String TEST3;
	
	@ConfigField(name = "configValue4", value = "default4")
	@ConfigGroupEnding(name = "OUTER GROUP", comment = "group end comment")
	public static String TEST4;
	
	@ConfigField(name = "configValue5", value = "default5")
	public static String TEST5;
	
	@ConfigField(name = "configValue6", value = "default6")
	public static String TEST6;
	
	@ConfigField(name = "configValue7", value = "default7")
	public static String TEST7;
	
	@ConfigField(name = "configValue8", value = "default8")
	@ConfigGroupEnding(name = "MIXED GROUP")
	public static String TEST8;
	
	@ConfigField(name = "configValue9", value = "default9", comment = { "This", "is", "a", "multi-line", "comment!" })
	public static String TEST9;
	
	@ConfigField(name = "doubleTest", value = "0.12,1.34", eternal = true)
	public static double[] DOUBLE_ARRAY;
	
	public static void main(String[] args) throws Exception
	{
		final ConfigClassInfo info = ConfigClassInfo.valueOf(ConfigTester.class);
		
		// required only for test launch
		if (info.getConfigFile().exists())
		{
			info.load();
			info.load();
		}
		
		ConfigTester.TEST1 = "default11";
		ConfigTester.TEST2 = "default21";
		ConfigTester.TEST3 = "default31";
		ConfigTester.TEST4 = "default41";
		ConfigTester.TEST5 = "default51";
		ConfigTester.TEST6 = "default61";
		ConfigTester.TEST7 = "default71";
		ConfigTester.TEST8 = "default81";
		ConfigTester.TEST9 = "default91";
		
		info.print(System.out, PrintMode.MODIFIED);
		info.store();
		
		new Configurator(info);
	}
}

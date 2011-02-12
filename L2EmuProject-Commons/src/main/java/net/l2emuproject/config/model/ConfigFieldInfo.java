package net.l2emuproject.config.model;

import java.io.PrintWriter;
import java.lang.reflect.Field;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.l2emuproject.config.L2Properties;
import net.l2emuproject.config.annotation.ConfigField;
import net.l2emuproject.config.annotation.ConfigGroupBeginning;
import net.l2emuproject.config.annotation.ConfigGroupEnding;
import net.l2emuproject.config.converters.Converter;
import net.l2emuproject.config.model.ConfigClassInfo.PrintMode;

public final class ConfigFieldInfo
{
	private static final Log _log = LogFactory.getLog(ConfigFieldInfo.class);
	
	private final Field _field;
	private final ConfigField _configField;
	private final Converter _converter;
	private final ConfigGroupBeginning _configGroupBeginning;
	private final ConfigGroupEnding _configGroupEnding;
	
	private ConfigGroup _beginningGroup;
	private ConfigGroup _endingGroup;
	
	private volatile boolean _fieldValueLoaded = false;
	
	public ConfigFieldInfo(Field field) throws InstantiationException, IllegalAccessException
	{
		_field = field;
		_configField = field.getAnnotation(ConfigField.class);
		_converter = getConfigField().converter().newInstance();
		_configGroupBeginning = field.getAnnotation(ConfigGroupBeginning.class);
		_configGroupEnding = field.getAnnotation(ConfigGroupEnding.class);
	}
	
	public Field getField()
	{
		return _field;
	}
	
	public String getCurrentValue()
	{
		Object obj = null;
		
		try
		{
			obj = getField().get(null);
		}
		catch (IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
		
		return getConverter().convertToString(getField().getType(), obj);
	}
	
	public void setCurrentValue(String value)
	{
		Object obj = getConverter().convertFromString(getField().getType(), value);
		
		if (_fieldValueLoaded && getConfigField().eternal())
			_log.warn("Eternal config field (" + getField() + ") (" + getConfigField() + ") assigned multiple times!");
		
		try
		{
			getField().set(null, obj);
		}
		catch (IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
		
		_fieldValueLoaded = true;
	}
	
	public void setCurrentValue(L2Properties properties)
	{
		final String newValue = properties.getProperty(getName(), getDefaultValue());
		
		setCurrentValue(newValue);
	}
	
	public ConfigField getConfigField()
	{
		return _configField;
	}
	
	public String getName()
	{
		return getConfigField().name();
	}
	
	public String getDefaultValue()
	{
		return getConfigField().value();
	}
	
	public boolean isModified()
	{
		final String currentValue = getCurrentValue();
		
		// config value wasn't initialized
		if (currentValue == null)
			return false;
		
		return !getDefaultValue().equals(currentValue);
	}
	
	public Converter getConverter()
	{
		return _converter;
	}
	
	public ConfigGroupBeginning getConfigGroupBeginning()
	{
		return _configGroupBeginning;
	}
	
	public ConfigGroupEnding getConfigGroupEnding()
	{
		return _configGroupEnding;
	}
	
	public ConfigGroup getBeginningGroup()
	{
		return _beginningGroup;
	}
	
	public void setBeginningGroup(ConfigGroup beginningGroup)
	{
		_beginningGroup = beginningGroup;
	}
	
	public ConfigGroup getEndingGroup()
	{
		return _endingGroup;
	}
	
	public void setEndingGroup(ConfigGroup endingGroup)
	{
		_endingGroup = endingGroup;
	}
	
	public void print(PrintWriter out, PrintMode mode)
	{
		if (getBeginningGroup() != null && (mode != PrintMode.MODIFIED || getBeginningGroup().isModified()))
		{
			out.println("########################################");
			out.println("## " + getConfigGroupBeginning().name());
			
			if (!ArrayUtils.isEmpty(getConfigGroupBeginning().comment()))
				for (String line : getConfigGroupBeginning().comment())
					out.println("# " + line);
			
			out.println();
		}
		
		if (mode != PrintMode.MODIFIED || isModified())
		{
			if (!ArrayUtils.isEmpty(getConfigField().comment()))
				for (String line : getConfigField().comment())
					out.println("# " + line);
			
			out.println("# Default: " + getDefaultValue());
			out.println(getName() + " = " + (mode == PrintMode.DEFAULT ? getDefaultValue() : getCurrentValue()));
			out.println();
		}
		
		if (getEndingGroup() != null && (mode != PrintMode.MODIFIED || getEndingGroup().isModified()))
		{
			if (!ArrayUtils.isEmpty(getConfigGroupEnding().comment()))
				for (String line : getConfigGroupEnding().comment())
					out.println("# " + line);
			
			out.println("## " + getConfigGroupEnding().name());
			out.println("########################################");
			
			out.println();
		}
	}
}

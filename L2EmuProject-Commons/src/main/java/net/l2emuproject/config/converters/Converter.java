package net.l2emuproject.config.converters;

public interface Converter
{
	public Object convertFromString(Class<?> type, String value);
	
	public String convertToString(Class<?> type, Object obj);
}

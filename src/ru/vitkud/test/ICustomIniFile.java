package ru.vitkud.test;

public interface ICustomIniFile extends AutoCloseable {

	int readInteger(String section, String ident, int defaultValue);
	void writeInteger(String section, String ident, int value);

	boolean readBool(final String section, final String ident, boolean defaultValue);
	void writeBool(final String section, final String ident, boolean value);

	void deleteKey(final String section, final String ident);

    @Override
	void close();
}

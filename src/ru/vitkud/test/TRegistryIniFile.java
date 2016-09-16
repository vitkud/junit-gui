package ru.vitkud.test;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class TRegistryIniFile implements ICustomIniFile {

	private Preferences fPreferences;

	public TRegistryIniFile(String fileName) {
		fPreferences = Preferences.userRoot().node(fileName.replace('\\', '/'));
	}

	@Override
	public int readInteger(String section, String ident, int defaultValue) {
		return fPreferences.node(section).getInt(ident, defaultValue);
	}

	@Override
	public void writeInteger(String section, String ident, int value) {
		fPreferences.node(section).putInt(ident, value);
	}

	@Override
	public boolean readBool(String section, String ident, boolean defaultValue) {
		return fPreferences.node(section).getBoolean(ident, defaultValue);
	}

	@Override
	public void writeBool(String section, String ident, boolean value) {
		fPreferences.node(section).putBoolean(ident, value);
	}

	@Override
	public void deleteKey(String section, String ident) {
		fPreferences.node(section).remove(ident);
	}

	@Override
	public void close() {
		try {
			fPreferences.flush();
		} catch (BackingStoreException e) {
			throw new RuntimeException(e);
		}
	}

}

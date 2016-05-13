package ru.vitkud.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class TIniFile implements ICustomIniFile {

	private String fFileName;
	private Properties fProperties;
	private boolean modified;

	public TIniFile(String fileName) {
		fFileName = fileName;
		fProperties = new Properties();
		try (InputStream is = new FileInputStream(fileName)) {
			fProperties.load(is);
		} catch (FileNotFoundException e) {
			// do nothing 
		} catch (IOException e) {
			// XXX do nothing 
			e.printStackTrace();
		}
		modified = false;
	}

	@Override
	public int readInteger(String section, String ident, int defaultValue) {
		return Integer.parseInt(fProperties.getProperty(section + "." + ident, Integer.toString(defaultValue)));
	}

	@Override
	public void writeInteger(String section, String ident, int value) {
		fProperties.setProperty(section + "." + ident, Integer.toString(value));
		modified = true;
	}

	@Override
	public boolean readBool(String section, String ident, boolean defaultValue) {
		return Boolean.parseBoolean(fProperties.getProperty(section + "." + ident, Boolean.toString(defaultValue)));
	}

	@Override
	public void writeBool(String section, String ident, boolean value) {
		fProperties.setProperty(section + "." + ident, Boolean.toString(value));
		modified = true;
	}

	@Override
	public void deleteKey(String section, String ident) {
		fProperties.remove(section + "." + ident);
		modified = true;
	}

	@Override
	public void close() {
		if (modified) {
			try (OutputStream os = new FileOutputStream(fFileName)) {
				fProperties.store(os, null);
			} catch (IOException e) {
				// XXX do nothing
				e.printStackTrace();
			}
		}
	}

}

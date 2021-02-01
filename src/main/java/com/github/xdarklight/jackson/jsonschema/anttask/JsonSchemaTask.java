package com.github.xdarklight.jackson.jsonschema.anttask;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator;

public class JsonSchemaTask extends Task {

	private final ObjectMapper objectMapper = new ObjectMapper();
	private String classname;
	private File destfile;

	public JsonSchemaTask() {
		objectMapper.findAndRegisterModules();
                objectMapper.registerModule(new JavaTimeModule());
	}

	@Override
	public void execute() throws BuildException {
		validateParameters();

		final Class<?> clazz = getClazz();
		final JsonNode schema = generateJsonSchema(clazz);

		try {
			objectMapper.writerWithDefaultPrettyPrinter().writeValue(getOutputFile(), schema);
		} catch (IOException e) {
			final String message = String.format("Failed to write JSON schema for class '%s' to '%s'", clazz, destfile);
			throw new BuildException(message, e);
		}
	}

	private JsonNode generateJsonSchema(final Class<?> clazz) {
                
		final JavaType javaType = objectMapper.constructType(clazz);
                return new JsonSchemaGenerator(objectMapper).generateJsonSchema(javaType);
	}

	private Class<?> getClazz() {
		try {
			return Class.forName(getClassname());
		} catch (ClassNotFoundException e) {
			throw new BuildException("Unknown class: " + getClassname(), e);
		}
	}

	private File getOutputFile() {
		if (destfile.getParentFile() == null) {
			return destfile;
		}

		if (destfile.exists()) {
			return destfile;
		}
		
		if (!destfile.getParentFile().exists() && !destfile.getParentFile().mkdirs()) {
			throw new BuildException("Failed to create parent directories for " + destfile);
		}

		return destfile;
	}
	
	private void validateParameters() throws BuildException {
		if (classname == null || classname.trim().isEmpty()) {
			throw new BuildException("A non-empty 'classname' must be given");
		}

		if (destfile == null) {
			throw new BuildException("A 'destfile' must be given!");
		}

		if (destfile.exists() && !destfile.isFile()) {
			throw new BuildException("'destfile' must point to a file, not a directory!");
		}
	}

	public void setClassname(String className) {
		this.classname = className;
	}

	public void setDestfile(final File destfile) {
		this.destfile = destfile;
	}

	private String getClassname() {
		return classname;
	}
}

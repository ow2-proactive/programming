package org.objectweb.proactive.core.component.adl.types;

import static org.objectweb.fractal.adl.error.ErrorTemplateValidator.validErrorTemplate;

import org.objectweb.fractal.adl.error.ErrorTemplate;
import org.objectweb.fractal.adl.types.TypeInterface;

public enum PATypeErrors implements ErrorTemplate {

	/** */
	INVALID_ROLE("Invalid role \"%s\". Expecting one of \""
			+ TypeInterface.CLIENT_ROLE + "\", \"" + TypeInterface.SERVER_ROLE + "\", \"" 
			+ PATypeInterface.INTERNAL_CLIENT_ROLE + "\", or \""+ PATypeInterface.INTERNAL_SERVER_ROLE 
			+ "\".", "role")

			;

	/** The groupId of ErrorTemplates defined in this enumeration. */
	public static final String GROUP_ID = "TYP";

	private int                id;
	private String             format;

	private PATypeErrors(final String format, final Object... args) {
		this.id = ordinal();
		this.format = format;

		assert validErrorTemplate(this, args);
	}

	@Override
	public int getErrorId() {
		return id;
	}

	@Override
	public String getFormat() {
		return format;
	}

	@Override
	public String getFormatedMessage(Object... args) {
		return String.format(format, args);
	}

	@Override
	public String getGroupId() {
		return GROUP_ID;
	}

}

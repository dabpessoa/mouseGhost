package me.dabpessoa.mouseghost.exceptions;

import lombok.Getter;

public class ApplicationRuntimeException extends RuntimeException {

	@Getter
	private final Integer errorCode;
	
	public ApplicationRuntimeException() {
		this(null, null, null);
	}

	public ApplicationRuntimeException(Throwable throwable) {
		this(null, null, throwable);
	}
	
	public ApplicationRuntimeException(Integer errorCode, Throwable throwable) {
		this(errorCode, null, throwable);
	}
	
	public ApplicationRuntimeException(Integer errorCode, String message) {
		this(errorCode, message, null);
	}
	
	public ApplicationRuntimeException(String message) {
		this(null, message, null);
	}
	
	public ApplicationRuntimeException(String message, Throwable throwable) {
		this(null, message, throwable);
	}

	public ApplicationRuntimeException(Integer errorCode, String message, Throwable throwable) {
		super(message, throwable);
		this.errorCode = errorCode;
	}

}

package org.springframework.http.converter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.util.FileCopyUtils;

/**
 * Implementation of {@link org.springframework.http.converter.HttpMessageConverter} that can read and write strings.
 *
 * <p>By default, this converter supports all media types (<code>&#42;&#47;&#42;</code>), and writes with a {@code
 * Content-Type} of {@code text/plain}. This can be overridden by setting the {@link
 * #setSupportedMediaTypes(java.util.List) supportedMediaTypes} property.
 *
 * @author Arjen Poutsma
 * @since 3.0
 */
public class CustomStringMessageConverter extends AbstractHttpMessageConverter<String> {

	public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

	private final List<Charset> availableCharsets;

	private boolean writeAcceptCharset = true;

	public CustomStringMessageConverter() {
		super(new MediaType("text", "plain", DEFAULT_CHARSET), MediaType.ALL);
		this.availableCharsets = new ArrayList<Charset>(Charset.availableCharsets().values());
	}

	/**
	 * Indicates whether the {@code Accept-Charset} should be written to any outgoing request.
	 * <p>Default is {@code true}.
	 */
	public void setWriteAcceptCharset(boolean writeAcceptCharset) {
		this.writeAcceptCharset = writeAcceptCharset;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return String.class.equals(clazz);
	}

	@Override
	protected String readInternal(Class clazz, HttpInputMessage inputMessage) throws IOException {
		Charset charset = Charset.forName("UTF-8");
		return FileCopyUtils.copyToString(new InputStreamReader(inputMessage.getBody(), charset));
	}

	@Override
	protected Long getContentLength(String s, MediaType contentType) {
		Charset charset = Charset.forName("UTF-8");
		try {
			return (long) s.getBytes(charset.name()).length;
		}
		catch (UnsupportedEncodingException ex) {
			// should not occur
			throw new InternalError(ex.getMessage());
		}
	}

	@Override
	protected void writeInternal(String s, HttpOutputMessage outputMessage) throws IOException {
		if (writeAcceptCharset) {
			outputMessage.getHeaders().setAcceptCharset(getAcceptedCharsets());
		}
		//System.out.println(outputMessage.getBody());
		Charset charset = Charset.forName("UTF-8");
		FileCopyUtils.copy(s, new OutputStreamWriter(outputMessage.getBody(), charset));
	}

	/**
	 * Return the list of supported {@link java.nio.charset.Charset}.
	 *
	 * <p>By default, returns {@link java.nio.charset.Charset#availableCharsets()}. Can be overridden in subclasses.
	 *
	 * @return the list of accepted charsets
	 */
	protected List<Charset> getAcceptedCharsets() {
		return this.availableCharsets;
	}

	private Charset getContentTypeCharset(MediaType contentType) {
	
			return DEFAULT_CHARSET;
		
	}

}
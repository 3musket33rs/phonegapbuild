package org.springframework.http.converter.custom;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.CustomStringMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;

/**
 * Implementation of {@link org.springframework.http.converter.HttpMessageConverter} that can handle form data, including multipart form data
 * (i.e. file uploads).
 *
 * <p>This converter can write the {@code application/x-www-form-urlencoded} and {@code multipart/form-data} media
 * types, and read the {@code application/x-www-form-urlencoded}) media type (but not {@code multipart/form-data}).
 *
 * <p>In other words, this converter can read and write 'normal' HTML forms (as
 * {@link org.springframework.util.MultiValueMap MultiValueMap&lt;String, String&gt;}), and it can write multipart form (as
 * {@link org.springframework.util.MultiValueMap MultiValueMap&lt;String, Object&gt;}. When writing multipart, this converter uses other
 * {@link org.springframework.http.converter.HttpMessageConverter HttpMessageConverters} to write the respective MIME parts. By default, basic converters
 * are registered (supporting {@code Strings} and {@code Resources}, for instance); these can be overridden by setting
 * the {@link #setPartConverters(java.util.List) partConverters} property.
 *
 * <p>For example, the following snippet shows how to submit an HTML form:
 * <pre class="code">
 * RestTemplate template = new RestTemplate(); // FormHttpMessageConverter is configured by default
 * MultiValueMap&lt;String, String&gt; form = new LinkedMultiValueMap&lt;String, String&gt;();
 * form.add("field 1", "value 1");
 * form.add("field 2", "value 2");
 * form.add("field 2", "value 3");
 * template.postForLocation("http://example.com/myForm", form);
 * </pre>
 * <p>The following snippet shows how to do a file upload:
 * <pre class="code">
 * MultiValueMap&lt;String, Object&gt; parts = new LinkedMultiValueMap&lt;String, Object&gt;();
 * parts.add("field 1", "value 1");
 * parts.add("file", new ClassPathResource("myFile.jpg"));
 * template.postForLocation("http://example.com/myFileUpload", parts);
 * </pre>
 *
 * <p>Some methods in this class were inspired by {@link org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity}.
 *
 * @author Arjen Poutsma
 * @see org.springframework.util.MultiValueMap
 * @since 3.0
 */
public class CustomHttpMessageconverter implements HttpMessageConverter<MultiValueMap<String, ?>> {

	private static final byte[] BOUNDARY_CHARS =
			new byte[]{'-', '_', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', 'a', 'b', 'c', 'd', 'e', 'f', 'g',
					'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A',
					'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
				 	'V', 'W', 'X', 'Y', 'Z'};

	private final Random rnd = new Random();

	private Charset charset = Charset.forName("UTF-8");

	private List<HttpMessageConverter<?>> partConverters = new ArrayList<HttpMessageConverter<?>>();

	public CustomHttpMessageconverter() {
		this.partConverters.add(new ByteArrayHttpMessageConverter());
		StringHttpMessageConverter stringHttpMessageConverter = new StringHttpMessageConverter();
		stringHttpMessageConverter.setWriteAcceptCharset(false);
		this.partConverters.add(new CustomStringMessageConverter());
		this.partConverters.add(new ResourceHttpMessageConverter());
		this.partConverters.add(new SourceHttpMessageConverter());
	}

	/**
	 * Set the message body converters to use. These converters are used to convert objects to MIME parts.
	 */
	public void setPartConverters(List<HttpMessageConverter<?>> partConverters) {
		Assert.notEmpty(partConverters, "'messageConverters' must not be empty");
		this.partConverters = partConverters;
	}

	/**
	 * Sets the character set used for writing form data.
	 */
	public void setCharset(Charset charset) {
		this.charset = charset;
	}

	public boolean canRead(Class<?> clazz, MediaType mediaType) {
		if (!MultiValueMap.class.isAssignableFrom(clazz)) {
			return false;
		}
		if (mediaType != null) {
			return MediaType.APPLICATION_FORM_URLENCODED.includes(mediaType);
		}
		else {
			return true;
		}
	}

	public boolean canWrite(Class<?> clazz, MediaType mediaType) {
		if (!MultiValueMap.class.isAssignableFrom(clazz)) {
			return false;
		}
		if (mediaType != null) {
			return mediaType.isCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED) ||
					mediaType.isCompatibleWith(MediaType.MULTIPART_FORM_DATA);
		}
		else {
			return true;
		}
	}

	public List<MediaType> getSupportedMediaTypes() {
		return Arrays.asList(MediaType.APPLICATION_FORM_URLENCODED, MediaType.MULTIPART_FORM_DATA);
	}

	public MultiValueMap<String, String> read(Class<? extends MultiValueMap<String, ?>> clazz,
											  HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException {
		MediaType contentType = inputMessage.getHeaders().getContentType();
		//Charset charset = contentType.getCharSet() != null ? contentType.getCharSet() : this.charset;
		String body = FileCopyUtils.copyToString(new InputStreamReader(inputMessage.getBody(), charset));

		String[] pairs = StringUtils.tokenizeToStringArray(body, "&");

		MultiValueMap<String, String> result = new LinkedMultiValueMap<String, String>(pairs.length);

		for (String pair : pairs) {
			int idx = pair.indexOf('=');
			if (idx == -1) {
				result.add(URLDecoder.decode(pair, charset.name()), null);
			}
			else {
				String name = URLDecoder.decode(pair.substring(0, idx), charset.name());
				String value = URLDecoder.decode(pair.substring(idx + 1), charset.name());
				result.add(name, value);
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public void write(MultiValueMap<String, ?> map, MediaType contentType, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		if (!isMultipart(map, contentType)) {
			writeForm((MultiValueMap<String, String>) map, outputMessage);
		}
		else {
			writeMultipart((MultiValueMap<String, Object>) map, outputMessage);
		}
	}

	private boolean isMultipart(MultiValueMap<String, ?> map, MediaType contentType) {
		if (contentType != null) {
			return MediaType.MULTIPART_FORM_DATA.equals(contentType);
		}
		for (String name : map.keySet()) {
			for (Object value : map.get(name)) {
				if (value != null && !(value instanceof String)) {
					return true;
				}
			}
		}
		return false;
	}

	private void writeForm(MultiValueMap<String, String> form, HttpOutputMessage outputMessage) throws IOException {

		outputMessage.getHeaders().setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		StringBuilder builder = new StringBuilder();
		for (Iterator<String> nameIterator = form.keySet().iterator(); nameIterator.hasNext();) {
			String name = nameIterator.next();
			for (Iterator<String> valueIterator = form.get(name).iterator(); valueIterator.hasNext();) {
				String value = valueIterator.next();
				builder.append(URLEncoder.encode(name, charset.name()));
				if (value != null) {
					builder.append('=');
					builder.append(URLEncoder.encode(value, charset.name()));
					if (valueIterator.hasNext()) {
						builder.append('&');
					}
				}
			}
			if (nameIterator.hasNext()) {
				builder.append('&');
			}
		}
		FileCopyUtils.copy(builder.toString(), new OutputStreamWriter(outputMessage.getBody(), charset));
	}

	private void writeMultipart(MultiValueMap<String, Object> parts, HttpOutputMessage outputMessage)
			throws IOException {
		byte[] boundary = generateMultipartBoundary();

		Map<String, String> parameters = Collections.singletonMap("boundary", new String(boundary, "UTF-8"));
		MediaType contentType = new MediaType(MediaType.MULTIPART_FORM_DATA, parameters);
		outputMessage.getHeaders().setContentType(contentType);

		writeParts(outputMessage.getBody(), parts, boundary);
		writeEnd(boundary, outputMessage.getBody());
	}

	private void writeParts(OutputStream os, MultiValueMap<String, Object> parts, byte[] boundary) throws IOException {
		for (Map.Entry<String, List<Object>> entry : parts.entrySet()) {
			String name = entry.getKey();
			for (Object part : entry.getValue()) {
				writeBoundary(boundary, os);
				HttpEntity entity = getEntity(part);
				writePart(name, entity, os);
				writeNewLine(os);
			}
		}
	}

	private void writeBoundary(byte[] boundary, OutputStream os) throws IOException {
		os.write('-');
		os.write('-');
		os.write(boundary);
		writeNewLine(os);
	}

	@SuppressWarnings("unchecked")
	private HttpEntity getEntity(Object part) {
		if (part instanceof HttpEntity) {
			return (HttpEntity) part;
		}
		else {
			return new HttpEntity(part);
		}
	}

	@SuppressWarnings("unchecked")
	private void writePart(String name, HttpEntity partEntity, OutputStream os) throws IOException {
		Object partBody = partEntity.getBody();
		Class<?> partType = partBody.getClass();
		HttpHeaders partHeaders = partEntity.getHeaders();
		
		
		MediaType partContentType = partHeaders.getContentType();
		if(partContentType==null){
			partContentType = MediaType.parseMediaType("multipart/form-data");
		}
		for (HttpMessageConverter messageConverter : partConverters) {
			if (messageConverter.canWrite(partType, partContentType)) {
				MultipartHttpOutputMessage multipartOutputMessage = new MultipartHttpOutputMessage(os);
				multipartOutputMessage.getHeaders().setContentDispositionFormData(name, getFilename(partBody));
				if (partContentType!=null) {
					multipartOutputMessage.getHeaders().putAll(partHeaders);
				}
				multipartOutputMessage.currentPart = name;
				messageConverter.write(partBody, partContentType, multipartOutputMessage);
				return;
			}
		}
		throw new HttpMessageNotWritableException(
				"Could not write request: no suitable HttpMessageConverter found for request type [" +
						partType.getName() + "]");
	}

	private void writeEnd(byte[] boundary, OutputStream os) throws IOException {
		os.write('-');
		os.write('-');
		os.write(boundary);
		os.write('-');
		os.write('-');
		writeNewLine(os);
	}

	private void writeNewLine(OutputStream os) throws IOException {
		os.write("\r".getBytes());
		os.write("\n".getBytes());
		
		
	}

	/**
	 * Generate a multipart boundary.
	 *
	 * <p>Default implementation returns a random boundary. Can be overridden in subclasses.
	 */
	protected byte[] generateMultipartBoundary() {
		byte[] boundary = new byte[rnd.nextInt(11) + 30];
		for (int i = 0; i < boundary.length; i++) {
			boundary[i] = BOUNDARY_CHARS[rnd.nextInt(BOUNDARY_CHARS.length)];
		}
		return boundary;
	}

	/**
	 * Returns the filename of the given multipart part. This value will be used for the {@code Content-Disposition} header.
	 *
	 * <p>Default implementation returns {@link org.springframework.core.io.Resource#getFilename()} if the part is a {@code Resource}, and
	 * {@code null} in other cases. Can be overridden in subclasses.
	 *
	 * @param part the part to determine the file name for
	 * @return the filename, or {@code null} if not known
	 */
	protected String getFilename(Object part) {
		if (part instanceof Resource) {
			Resource resource = (Resource) part;
			return resource.getFilename();
		}
		else {
			return null;
		}
	}

	/**
	 * Implementation of {@link org.springframework.http.HttpOutputMessage} used for writing multipart data.
	 */

	private class MultipartHttpOutputMessage implements HttpOutputMessage {

		private final HttpHeaders headers = new HttpHeaders();

		private final OutputStream os;
		public String currentPart;
		private boolean headersWritten = false;

		public MultipartHttpOutputMessage(OutputStream os) {
			this.os = os;
		}

		public HttpHeaders getHeaders() {
			return headersWritten ? HttpHeaders.readOnlyHttpHeaders(headers) : this.headers;
		}

		public OutputStream getBody() throws IOException {
			writeHeaders();
			return this.os;
		}

		private void writeHeaders() throws IOException {
			if (!this.headersWritten) {
				for (Map.Entry<String, List<String>> entry : this.headers.entrySet()) {
					byte[] headerName = getAsciiBytes(entry.getKey());
					for (String headerValueString : entry.getValue()) {
						if(!entry.getKey().equals("Content-Type") ){
							
						
						byte[] headerValue = getAsciiBytes(headerValueString);
						os.write(headerName);
						os.write(':');
						os.write(' ');
						if(currentPart.equals("data") && entry.getKey().equals("Content-Type")){
							os.write(headerValue,0,headerValue.length-1);
						}
						else {
							os.write(headerValue,0,headerValue.length);
						}
						
						writeNewLine(os);
						}
					}
				}
				writeNewLine(os);
				this.headersWritten = true;
			}
		}

		protected byte[] getAsciiBytes(String name) {
			try {
				return name.getBytes("ISO-8859-1");
			}
			catch (UnsupportedEncodingException ex) {
				// should not happen, US-ASCII is always supported
				throw new IllegalStateException(ex);
			}
		}


	}
}

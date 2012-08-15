package grails.plugins.rest.client

import org.springframework.core.io.*
import org.springframework.web.client.RestTemplate
import org.springframework.http.*
import org.springframework.util.*
import static org.springframework.http.MediaType.*
import grails.converters.*
import grails.web.*
import org.springframework.http.client.*
import org.codehaus.groovy.grails.plugins.codecs.Base64Codec
import groovy.util.slurpersupport.*
import org.codehaus.groovy.grails.web.json.*
import java.nio.charset.*
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpAccessor;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.custom.CustomHttpMessageconverter;
import org.springframework.http.converter.feed.AtomFeedHttpMessageConverter;
import org.springframework.http.converter.feed.RssChannelHttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.http.converter.xml.XmlAwareFormHttpMessageConverter
class CustomRestBuilder {

	RestTemplate restTemplate

	CustomRestBuilder() {
		restTemplate = new RestTemplate()
		restTemplate.setRequestFactory(new
				SimpleClientHttpRequestFactory())
		CustomHttpMessageconverter converter = new CustomHttpMessageconverter()
		converter.charset = Charset.forName("UTF-8")
		restTemplate.messageConverters = [converter,new ByteArrayHttpMessageConverter(),new StringHttpMessageConverter(),new ResourceHttpMessageConverter(),new SourceHttpMessageConverter()]



	}



	CustomRestBuilder(Map settings) {
		this()

		def proxyHost = System.getProperty("http.proxyHost")
		def proxyPort = System.getProperty("http.proxyPort")

		if(proxyHost && proxyPort) {
			if(settings.proxy == null) {
				settings.proxy = new Proxy(Proxy.Type.HTTP, new
						InetSocketAddress(proxyHost, proxyPort.toInteger()))
			}
		}
		if(settings.proxy instanceof Map) {
			def ps = settings.proxy.entrySet().iterator().next()
			if(ps.value) {
				def proxy = new Proxy(Proxy.Type.HTTP, new
						InetSocketAddress(ps.key, ps.value.toInteger()))
				settings.proxy = proxy
			}
		}


		restTemplate = new RestTemplate()
		restTemplate.setRequestFactory(new
				SimpleClientHttpRequestFactory(settings))
		CustomHttpMessageconverter converter = new CustomHttpMessageconverter()
		converter.charset = Charset.forName("UTF-8")
		restTemplate.messageConverters = [converter,new ByteArrayHttpMessageConverter(),new StringHttpMessageConverter(),new ResourceHttpMessageConverter(),new SourceHttpMessageConverter()]
	}

	/**
	 * Issues a GET request and returns the response in the most
	 appropriate type
	 * @param url The URL
	 * @param url The closure customizer used to customize request attributes
	 */
	def get(String url, Closure customizer=null) {
		doRequestInternal( url, customizer, HttpMethod.GET)
	}

	/**
	 * Issues a PUT request and returns the response in the most
	 appropriate type
	 *
	 * @param url The URL
	 * @param customizer The clouser customizer
	 */
	def put(String url, Closure customizer = null) {
		doRequestInternal( url, customizer, HttpMethod.PUT)
	}

	/**
	 * Issues a POST request and returns the response
	 * @param url The URL
	 * @param customizer (optional) The closure customizer
	 */
	def post(String url, Closure customizer = null) {
		doRequestInternal( url, customizer, HttpMethod.POST)
	}

	/**
	 * Issues DELETE a request and returns the response
	 * @param url The URL
	 * @param customizer (optional) The closure customizer
	 */
	def delete(String url, Closure customizer = null) {
		doRequestInternal( url, customizer, HttpMethod.DELETE)
	}

	protected doRequestInternal(String url, Closure customizer,
	HttpMethod method) {

		def requestCustomizer = new CustomRequestCustomizer()
		if(customizer != null) {
			customizer.delegate = requestCustomizer
			customizer.call()
		}
		try {

			def responseEntity = restTemplate.exchange(url,
					method,requestCustomizer.createEntity(),String)
			handleResponse(responseEntity)
		}
		catch(org.springframework.web.client.HttpClientErrorException e) {
			restTemplate.messageConverters.each {
				if(it instanceof
				org.springframework.http.converter.xml.XmlAwareFormHttpMessageConverter){
					Charset charset = Charset.forName("UTF-8")
					it.charset = charset
				}
			}
			return new CustomErrorResponse(error:e)
		}
	}
	protected handleResponse(ResponseEntity responseEntity) {
		return new CustomRestResponse(responseEntity: responseEntity)
	}
}
class CustomErrorResponse {
	@Delegate
	org.springframework.web.client.HttpClientErrorException error
	@Lazy
	String text = { error.responseBodyAsString }()

	byte[] getBody() {
		error.responseBodyAsByteArray
	}

	int getStatus() {
		error.statusCode?.value() ?: 200
	}
}
class CustomRestResponse {
	@Delegate
	ResponseEntity responseEntity
	@Lazy
	JSONElement json = {
		def body = responseEntity.body
		if(body) {
			return JSON.parse(body)
		}
	}()
	@Lazy
	GPathResult xml = {
		def body = responseEntity.body
		if(body) {
			return XML.parse(body)
		}
	}()

	@Lazy
	String text = {
		def body = responseEntity.body
		if(body) {
			return body.toString()
		}
		else {
			responseEntity.statusCode.reasonPhrase
		}
	}()

	int getStatus() {
		responseEntity?.statusCode?.value() ?: 200
	}
}
class CustomRequestCustomizer {
	HttpHeaders headers = new HttpHeaders()
	def body
	MultiValueMap<String, Object> mvm = new
	LinkedMultiValueMap<String, Object>()

	// configures basic author
	CustomRequestCustomizer auth(String username, String password) {
		String authStr = "$username:$password"
		String encoded = Base64Codec.encode(authStr)
		headers["Authorization"] = "Basic $encoded".toString()
		return this
	}

	CustomRequestCustomizer contentType(String contentType) {
		headers.setContentType(MediaType.valueOf(contentType))
		return this
	}

	CustomRequestCustomizer accept(String...contentTypes) {
		def list = contentTypes.collect { MediaType.valueOf(it) }
		headers.setAccept(list)
		return this
	}

	CustomRequestCustomizer header(String name, String value) {
		headers[name] = value
		return this
	}

	CustomRequestCustomizer json(Closure callable) {
		def builder = new JSONBuilder()
		callable.resolveStrategy = Closure.DELEGATE_FIRST
		JSON json = builder.build(callable)

		body = json.toString()
		return this
	}

	CustomRequestCustomizer xml(Closure closure) {
		def b = new groovy.xml.StreamingMarkupBuilder()
		def markup = b.bind(closure)
		def StringWriter sw = new StringWriter()
		markup.writeTo(sw)
		this.body = sw.toString()
		return this
	}

	CustomRequestCustomizer body(content) {
		this.body = content
		return this
	}

	HttpEntity createEntity() {
		if(mvm) {
			return new HttpEntity(mvm, headers)
		}
		else {
			return new HttpEntity(body, headers)
		}
	}

	void setProperty(String name, value) {
		if(value instanceof File) {
			value = new FileSystemResource(value)
		}
		else if(value instanceof URL) {
			value = new UrlResource(value)
		}
		else if(value instanceof InputStream) {
			value = new InputStreamResource(value)
		}
		mvm[name] = value
	}
}
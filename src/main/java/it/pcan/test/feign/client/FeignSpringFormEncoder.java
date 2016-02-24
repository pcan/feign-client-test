package it.pcan.test.feign.client;

import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

/**
 * A custom {@link feign.codec.Encoder} that supports Multipart requests. It uses
 * {@link HttpMessageConverter}s like {@link RestTemplate} does.
 *
 * @author Pierantonio Cangianiello
 */
public class FeignSpringFormEncoder implements Encoder {

    private final List<HttpMessageConverter<?>> converters = new RestTemplate().getMessageConverters();
    private final HttpHeaders multipartHeaders = new HttpHeaders();
    private final HttpHeaders jsonHeaders = new HttpHeaders();

    public static final Charset UTF_8 = Charset.forName("UTF-8");

    public FeignSpringFormEncoder() {
        multipartHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void encode(Object object, Type bodyType, RequestTemplate template) throws EncodeException {
        if (isFormRequest(bodyType)) {
            encodeMultipartFormRequest((Map<String, ?>) object, template);
        } else {
            encodeRequest(object, jsonHeaders, template);
        }
    }

    /**
     * Encodes the request as a multipart form. It can detect a single {@link MultipartFile}, an
     * array of {@link MultipartFile}s, or POJOs (that are converted to JSON).
     *
     * @param formMap
     * @param template
     * @throws EncodeException
     */
    private void encodeMultipartFormRequest(Map<String, ?> formMap, RequestTemplate template) throws EncodeException {
        if (formMap == null) {
            throw new EncodeException("Cannot encode request with null form.");
        }
        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        for (Entry<String, ?> entry : formMap.entrySet()) {
            Object value = entry.getValue();
            if (isMultipartFile(value)) {
                map.add(entry.getKey(), encodeMultipartFile((MultipartFile) value));
            } else if (isMultipartFileArray(value)) {
                encodeMultipartFiles(map, entry.getKey(), Arrays.asList((MultipartFile[]) value));
            } else {
                map.add(entry.getKey(), encodeJsonObject(value));
            }
        }
        encodeRequest(map, multipartHeaders, template);
    }

    private boolean isMultipartFile(Object object) {
        return object instanceof MultipartFile;
    }

    private boolean isMultipartFileArray(Object o) {
        return o != null && o.getClass().isArray() && MultipartFile.class.isAssignableFrom(o.getClass().getComponentType());
    }

    /**
     * Wraps a single {@link MultipartFile} into a {@link HttpEntity} and sets the
     * {@code Content-type} header to {@code application/octet-stream}
     *
     * @param file
     * @return
     */
    private HttpEntity<?> encodeMultipartFile(MultipartFile file) {
        HttpHeaders filePartHeaders = new HttpHeaders();
        filePartHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        try {
            Resource multipartFileResource = new MultipartFileResource(file.getBytes(), file.getOriginalFilename());
            return new HttpEntity<>(multipartFileResource, filePartHeaders);
        } catch (IOException ex) {
            throw new EncodeException("Cannot encode request.", ex);
        }
    }

    /**
     * Fills the request map with {@link HttpEntity}s containing the given {@link MultipartFile}s.
     * Sets the {@code Content-type} header to {@code application/octet-stream} for each file.
     *
     * @param the current request map.
     * @param name the name of the array field in the multipart form.
     * @param files
     */
    private void encodeMultipartFiles(LinkedMultiValueMap<String, Object> map, String name, List<? extends MultipartFile> files) {
        HttpHeaders filePartHeaders = new HttpHeaders();
        filePartHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        try {
            for (MultipartFile file : files) {
                Resource multipartFileResource = new MultipartFileResource(file.getBytes(), file.getOriginalFilename());
                map.add(name, new HttpEntity<>(multipartFileResource, filePartHeaders));
            }
        } catch (IOException ex) {
            throw new EncodeException("Cannot encode request.", ex);
        }
    }

    /**
     * Wraps an object into a {@link HttpEntity} and sets the {@code Content-type} header to
     * {@code application/json}
     *
     * @param o
     * @return
     */
    private HttpEntity<?> encodeJsonObject(Object o) {
        HttpHeaders jsonPartHeaders = new HttpHeaders();
        jsonPartHeaders.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(o, jsonPartHeaders);
    }

    /**
     * Calls the conversion chain actually used by
     * {@link org.springframework.web.client.RestTemplate}, filling the body of the request
     * template.
     *
     * @param value
     * @param requestHeaders
     * @param template
     * @throws EncodeException
     */
    private void encodeRequest(Object value, HttpHeaders requestHeaders, RequestTemplate template) throws EncodeException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        HttpOutputMessage dummyRequest = new HttpOutputMessageImpl(outputStream, requestHeaders);
        try {
            Class<?> requestType = value.getClass();
            MediaType requestContentType = requestHeaders.getContentType();
            for (HttpMessageConverter<?> messageConverter : converters) {
                if (messageConverter.canWrite(requestType, requestContentType)) {
                    ((HttpMessageConverter<Object>) messageConverter).write(
                            value, requestContentType, dummyRequest);
                    break;
                }
            }
        } catch (IOException ex) {
            throw new EncodeException("Cannot encode request.", ex);
        }
        HttpHeaders headers = dummyRequest.getHeaders();
        if (headers != null) {
            for (Entry<String, List<String>> entry : headers.entrySet()) {
                template.header(entry.getKey(), entry.getValue());
            }
        }
        template.body(outputStream.toByteArray(), UTF_8);
    }

    /**
     * Minimal implementation of {@link org.springframework.http.HttpOutputMessage}. It's needed to
     * provide the request body output stream to
     * {@link org.springframework.http.converter.HttpMessageConverter}s
     */
    private class HttpOutputMessageImpl implements HttpOutputMessage {

        private final OutputStream body;
        private final HttpHeaders headers;

        public HttpOutputMessageImpl(OutputStream body, HttpHeaders headers) {
            this.body = body;
            this.headers = headers;
        }

        @Override
        public OutputStream getBody() throws IOException {
            return body;
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }

    }

    /**
     * Heuristic check for multipart requests.
     *
     * @param type
     * @return
     * @see feign.Types#MAP_STRING_WILDCARD
     */
    static boolean isFormRequest(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            Type[] actualTypeArguments = pt.getActualTypeArguments();
            if (pt.getRawType() == Map.class && actualTypeArguments != null
                    && actualTypeArguments.length == 2 && actualTypeArguments[0] == String.class
                    && actualTypeArguments[1] instanceof WildcardType) {
                WildcardType wt = (WildcardType) actualTypeArguments[1];
                Type[] upperBounds = wt.getUpperBounds();
                Type[] lowerBounds = wt.getLowerBounds();

                return upperBounds != null && upperBounds.length == 1 && upperBounds[0] == Object.class
                        && lowerBounds != null && lowerBounds.length == 0;
            }
        }
        return false;
    }

    /**
     * Dummy resource class. Wraps file content and its original name.
     */
    static class MultipartFileResource extends ByteArrayResource {

        private final String filename;

        public MultipartFileResource(byte[] payload, String originalFileName) throws IOException {
            super(payload);
            this.filename = originalFileName;
        }

        @Override
        public String getFilename() {
            return this.filename;
        }
    }

}

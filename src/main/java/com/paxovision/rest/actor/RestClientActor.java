package com.paxovision.rest.actor;

//import com.mlp.raptor.RaptorException;
import com.paxovision.rest.auth.KerberosAuthenticator;
import com.paxovision.rest.auth.NTLMAuthenticator;
import com.paxovision.rest.exception.PaxoRestException;
import com.paxovision.rest.request.RestDeleteRequestBuilder;
import com.paxovision.rest.request.RestGetRequestBuilder;
import com.paxovision.rest.request.RestHeadRequestBuilder;
import com.paxovision.rest.request.RestPatchRequestBuilder;
import com.paxovision.rest.request.RestPostRequestBuilder;
import com.paxovision.rest.request.RestPutRequestBuilder;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.RateLimiter;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import devcsrj.okhttp3.logging.HttpLoggingInterceptor;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/** Raptor actor for REST API testing. */
public class RestClientActor implements Actor{

    private final OkHttpClient okHttpClient;
    private final String baseURL;

    /**
     *	Constructor for RestClientActor. Prefer using {@link #newBuilder} for creating new actor
     *	instance
     *
     * @param builder RestClientActor.Builder instance of the builder
     */
    public RestClientActor(Builder builder) {
        this.okHttpClient = builder.okHttpClientBuilder.build();
        this.baseURL = builder.baseURL;
        // configure JsonPath
        Configuration.setDefaults(
                new Configuration.Defaults() {
                    private final JsonProvider jsonProvider = new JacksonJsonNodeJsonProvider();
                    private final MappingProvider mappingProvider = new JacksonMappingProvider();

                    @Override
                    public JsonProvider jsonProvider() {
                        return jsonProvider;
                    }

                    @Override
                    public MappingProvider mappingProvider() {
                        return mappingProvider;
                    }

                    @Override
                    public Set<Option> options() {
                        return EnumSet.noneOf(Option.class);
                    }
                });
    }

    public static final Builder newBuilder() {
        return new Builder();
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     *	Returns complete URL to perform the request
     *
     *	@param baseURL base URL
     *	@param requestURL request URL
     *	@return if requestURL is starting from "http://" or "https://", then completeURL is
     *	requestURL otherwise completeURL is concatenation of baseURL and requestURL
     */
    private String getCompleteURL(String baseURL, String requestURL) {
        return (requestURL.startsWith("http://") || requestURL.startsWith("https://"))
                ? requestURL
                : baseURL + requestURL;
    }

    /**
    *	Fluent interface for HTTP GET requests (relative path as String)
    *
    *	@param path path for the request (path for the request relative to the baseURL)
    *	@return RestGetRequestBuilder instance
    */
    public RestGetRequestBuilder get(String path) {
        return new RestGetRequestBuilder(getCompleteURL(baseURL, path), okHttpClient);
    }

    /**
     *	Fluent interface for HTTP GET requests (relative path as template + parameters)
     *
     *	@param template path for the request with placeholders for parameters (relative to the
     *	baseURL)
     *	@param params parameters to be used in template
     *	^return RestGetRequestBuilder instance
     */
    public RestGetRequestBuilder get(String template, Object... params) {
        return get(String.format(template, params));
    }

    /**
     *	Fluent interface for HTTP POST requests
     *
     *	@param path path for the request (path for the request relative to the baseURL)
     *	^return RestPostRequestBuilder instance
     */
    public RestPostRequestBuilder post(String path) {
        return new RestPostRequestBuilder(getCompleteURL(baseURL, path), okHttpClient);
    }

    /**
     *	Fluent interface for HTTP POST requests (relative path as template + parameters)
     *
     *	@param template path for the request with placeholders for parameters (relative to the
     *	baseURL)
     *	@param params parameters to be used in template
     *	@return RestGetRequestBuilder instance
     */
    public RestPostRequestBuilder post(String template, Object... params) {
        return post(String.format(template, params));
    }

    /**
    *	Fluent interface for HTTP PUT requests
    *
    *	@param path path for the request (path for the request relative to the baseURL)
    *	@return RestPutRequestBuilder instance
    */
    public RestPutRequestBuilder put(String path) {
        return new RestPutRequestBuilder(getCompleteURL(baseURL, path), okHttpClient);
    }

    /**
    *	Fluent interface for HTTP PUT requests (relative path as template + parameters)
    *
    *	@param template path for the request with placeholders for parameters (relative to the
    *	baseURL)
    *	@param params parameters to be used in template
    *	@return RestPostRequestBuilder instance
    */
    public RestPutRequestBuilder put(String template, Object... params) {
        return put(String.format(template, params));
    }

    /**
     *	Fluent interface for HTTP HEAD requests
     *
     *	@param path path for the request (path for the request relative to the baseURL)
     *	^return RestHeadRequestBuilder instance
     */
    public RestHeadRequestBuilder head(String path) {
        return new RestHeadRequestBuilder(getCompleteURL(baseURL, path), okHttpClient);
    }

    /**
     *	Fluent interface for HTTP HEAD requests (relative path as template + parameters)
     *
     *	@param template path for the request with placeholders for parameters (relative to the
     *	baseURL)
     *	@param params parameters to be used in template
     *	^return RestHeadRequestBuilder instance
     */
    public RestHeadRequestBuilder head(String template, Object... params) {
        return head(String.format(template, params));
    }

    /**
     *	Fluent interface for HTTP PATCH requests
     *
     *	@param path path for the request (path for the request relative to the baseURL)
     *	@return RestPatchRequestBuilder instance
     */
    public RestPatchRequestBuilder patch(String path) {
        return new RestPatchRequestBuilder(getCompleteURL(baseURL, path), okHttpClient);
    }

    /**
    *	Fluent interface for HTTP PATCH requests (relative path as template + parameters)
    *
    *	@param template path for the request with placeholders for parameters (relative to the
    *	baseURL)
    *	@param params parameters to be used in template
    *	@return RestPatchRequestBuilder instance
    */
    public RestPatchRequestBuilder patch(String template, Object... params) {
        return patch(String.format(template, params));
    }

    /**
     *	Fluent interface for HTTP DELETE requests
     *
     *	@param path path for the request (path for the request relative to the baseURL)
     *	@return RestDeleteRequestBuilder instance
     */
    public RestDeleteRequestBuilder delete(String path) {
        return new RestDeleteRequestBuilder(getCompleteURL(baseURL, path), okHttpClient);
    }

    /**
    *	Fluent interface for HTTP DELETE requests (relative path as template + parameters)
    *
    *	@param template path for the request with placeholders for parameters (relative to the
    *	baseURL)
    *	@param params parameters to be used in template
    *	@return RestDeleteRequestBuilder instance
    */
    public RestDeleteRequestBuilder delete(String template, Object... params) {
        return delete(String.format(template, params));
    }

    /** Builder for the {@link com.paxovision.rest.actor.RestClientActor} */
    public static final class Builder {

        final OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
        String baseURL;
        String hostName;
        boolean enableLogging = true;

        /**
         * Build the new {@link com.paxovision.rest.actor.RestClientActor} instance using current builder
         * <p>
         * ^return new instance of {@link com.paxovision.rest.actor.RestClientActor}
         */
        public RestClientActor build() {
            if (enableLogging) {
                okHttpClientBuilder.addInterceptor(new HttpLoggingInterceptor());
            }
            return new RestClientActor(this);
        }

        /**
         * Define base URL for the {@link com.paxovision.rest.actor.RestClientActor}. All requests URLs
         * will be relative for this URL
         * I
         *
         * @param baseURL value for the baseURL
         *                (©return self
         */
        public RestClientActor.Builder withBaseURL(String baseURL) {
            this.baseURL = Preconditions.checkNotNull(baseURL, "Base URL can't be null!");
            try {
                hostName = new URL(baseURL).getHost();
            } catch (MalformedURLException ex) {
                throw new PaxoRestException("Provider baseURL value is not a valid URL: ", ex);
            }
            return this;
        }

        /**
        *	Sets the default connect timeout for new connections.A value of 0 means no timeout,
        *	otherwise values must be between 1 and {@link Integer#MAX_VALUE} when converted to
        *	milliseconds.
        *
        *	<p>The connect timeout is applied when connecting a TCP socket to the target host. The
        *	default value is 10 seconds.
        *
        *	@param timeout connect timeout duration value
        *	@param unit timeout unit
        *	(©return self
        */
        public RestClientActor.Builder withConnectTimeout(long timeout, TimeUnit unit) {
            okHttpClientBuilder.connectTimeout(timeout, unit);
            return this;
        }

        /**
        *	Sets the default connect timeout for new connections.A value of 0 means no timeout,
        *	otherwise values must be between 1 and {@link Integer#MAX_VALUE} when converted to
        *	milliseconds.
        *
        *	<p>The connect timeout is applied when connecting a TCP socket to the target host. The
        *	default value is 10 seconds.
        *
        *	@param duration connect timeout duration value
        *	@return self
        */
        public RestClientActor.Builder withConnectTimeout(Duration duration) {
            return withConnectTimeout(duration.toMillis(), TimeUnit.MILLISECONDS);
        }

        /**
         *	Sets the default read timeout for new connections.A value of 0 means no timeout,
         *	otherwise values must be between 1 and {@link Integer#MAX_VALUE} when converted to
         *	milliseconds.
         *
         *	<p>The read timeout is applied to both the TCP socket and for individual read 10
         *	operations including on Source of the Response. The default value is 10 seconds.
         *
         *	@param timeout read timeout duration value
         *	@param unit read timeout duration unit
         *	(©return self
         */
        public RestClientActor.Builder withReadTimeout(long timeout, TimeUnit unit) {
            okHttpClientBuilder.readTimeout(timeout, unit);
            return this;
        }

        /**
        *	Sets the default read timeout for new connections.A value of 0 means no timeout,
        *	otherwise values must be between 1 and {@link Integer#MAX_VALUE} when converted to
        *	milliseconds.
        *
        *	<p>The read timeout is applied to both the TCP socket and for individual read 10
        *	operations including on Source of the Response. The default value is 10 seconds.
        *
        *	@param duration read timeout duration value
        *	@return self
        */
        public RestClientActor.Builder withReadTimeout(Duration duration) {
            return withReadTimeout(duration.toMillis(), TimeUnit.MILLISECONDS);

        }

        /**
         *	Sets the default write timeout for new connections. A value of 0 means no timeout,
         *	otherwise values must be between 1 and {@link Integer#MAX_VALUE} when converted to
         *	milliseconds.
         *
         *	<p>The write timeout is applied for individual write 10 operations. The default value is
         *	10 seconds.
         *
         *	@param timeout write timeout duration value
         *	@param unit write timeout duration unit
         *	(©return self
         */
        public RestClientActor.Builder withWriteTimeout(long timeout, TimeUnit unit) {
            okHttpClientBuilder.writeTimeout(timeout, unit);
            return this;
        }

        /**
        *	Sets the default write timeout for new connections.A value of 0 means no timeout,
        *	otherwise values must be between 1 and {@link Integer#MAX_VALUE} when converted to
        *	milliseconds.
        *	I
        *	<p>The write timeout is applied for individual write 10 operations. The default value is
        *	10 seconds.
        *
        *	@param duration write timeout duration value
        *	@return self
        */
        public RestClientActor.Builder withWriteTimeout(Duration duration) {
            return withWriteTimeout(duration.toMillis(), TimeUnit.MILLISECONDS);
        }

        /**
        *	Sets the rate limit for this instance of the REST client in messages-per-second. If acto
        *	will try to send more faster, than the defined value, rate will be limited to the given
        *	value
        *
        *	@param rateLimit number of messages-per-seconds actor is allowed to sent
        *	(©return self
        */
        public RestClientActor.Builder withRateLimit(int rateLimit) {
            Preconditions.checkArgument(rateLimit > 0, "RateLimit must be positive integer value!");
            okHttpClientBuilder.addNetworkInterceptor(
                    new Interceptor() {
                        private final RateLimiter rateLimiter = RateLimiter.create(rateLimit);

                        @Override
                        public Response intercept(Interceptor.Chain chain) throws IOException {
                            final Request request = chain.request();
                            rateLimiter.acquire();
                            return chain.proceed(request);
                        }
                    });
            return this;
        }

        /**
        *	Sets the default header name/value, which will be added to every request sent by this
        *	instance of {@link com.paxovision.rest.actor.RestClientActor} unless header with same name
        *	already set for the request. Convenient to define dynamic headers, which needs to be
        *	generated for each request (i.e. Kerberos authentication token)
        *
        *	@param name header name
        *	@param valueSupplier header value supplier
        *	(©return self	I
        */
        public RestClientActor.Builder withDefaultHeader(
                String name, Supplier<String> valueSupplier) {

            okHttpClientBuilder.addInterceptor(
                    chain -> {
                        final Request request = chain. request();
                        return chain.proceed(
                                request.header(name) != null
                                        ? request
                                        : chain.request()
                                            .newBuilder()
                                            .header(name, valueSupplier.get())
                                            .build());
                    });
            return this;
        }


        /**
         *	Sets the default header name/value, which will be added to every request sent by this
         *	instance of {@link com.paxovision.rest.actor.RestClientActor}
         *
         *	(©param name header name
         *	@param value header value
         *	(©return self
         */
        public RestClientActor.Builder withDefaultHeader(String name, String value) {
            return withDefaultHeader(name, () -> value);
        }

        /**
        *	Sets the default headers name/value, which will be added to every request sent by this
        *	instance of {@link com.paxovision.rest.actor.RestClientActor} unless header with same name is
        *	already set in request
        *
        *	@param headers map for headers with values
        *	(©return self
        */
        public RestClientActor.Builder withDefaultHeaders(Map<String, String> headers) {
            headers.forEach(this ::withDefaultHeader);
            return this;
        }

        /**
         *	Sets the userld/secret for basic HTTP Authentication
         *
         *	@param userid user identifier (username)
         *	@param secret user secret (password)
         *	(©return self
         */
        public RestClientActor.Builder withBasicAuth(String userid, String secret) {
            return withDefaultHeader(AUTHORIZATION, Credentials.basic(userid, secret));
        }


        /**
         *	Sets the userld/secret for NTLLM Authentication
        *
        *	@param userid user identifier (username including domain, i.e. user@mlp.com)
        *	@param secret user secret (password)
        *	(©return self
        */
        public RestClientActor.Builder withNTLMAuth(String userid, String secret) {
            okHttpClientBuilder.authenticator(new NTLMAuthenticator(userid, secret));
            return this;
        }

        /**
        *	Sets the options for Kerberos authentication
        *
        *	@param krbOptions Kerberos authentication options
        *	@return self
        */
        public RestClientActor.Builder withKerberosAuth(Map<String, String> krbOptions) {
            Preconditions.checkNotNull(
                    baseURL, "baseURL must be defined for Kerberos authentication!");
            return withDefaultHeader(
                    AUTHORIZATION,
                    () ->
                        "Negotiate "
                                + new KerberosAuthenticator(krbOptions)
                                    .buildAuthorizationHeader("HTTP/" + hostName));
        }


        /**
        *	Configure this client to follow redirects from HTTPS to HTTP and from HTTP to HTTPS.
        *
        *	<p>If unset, protocol redirects will be followed. This is different than the built-in
        *	{@code HttpURLConnection}'s default.
        *
        *	@param followProtocolRedirects if true follow the protocol redirects
        *	@return self
        */
        public RestClientActor.Builder setFollowSslRedirects(boolean followProtocolRedirects) {
            okHttpClientBuilder.followSslRedirects(followProtocolRedirects);
            return this;
        }

        /**
        *	Configure this client to follow redirects.If unset, redirects will be followed.
        *
        *	@param followRedirects if true, follow the redirects
        *	(©return self
        */
        public RestClientActor.Builder setFollowRedirects(boolean followRedirects) {
            okHttpClientBuilder.followRedirects(followRedirects);
            return this;
        }


        /**
        *	Configure this client to retry or not when a connectivity problem is encountered.By
        *	default, this client silently recovers from the following problems:
        *
        * <ul>
        *   <li><strong>Unreachable IP addresses.</strong> If the URL's host has multiple IP
        *    addresses, failure to reach any individual IP address doesn't fail the overall
        *    request.This can increase availability of multi-homed services.
        *   <li><strong>Stale pooled connections.</strong> The {@link okhttp3.ConnectionPool}
        *     sockets to decrease request latency, but these connections will occasionally
        *    time out.
        *   <li><strong>Unreachable proxy servers.</strong> A {@link okhttp3} can be
        *      used to attempt multiple proxy servers in sequence, eventually falling back to a
        *      direct connection.
        *	</ul>
        *
        *	Set this to false to avoid retrying requests when doing so is destructive. In this case
        *	the calling application should do its own recovery of connectivity failures.
        *
        *	@param retryOnConnectionFailure true to enable retry on connection failure
        *	@return self
        */
        public RestClientActor.Builder setRetryOnConnectionFailure(
                boolean retryOnConnectionFailure) {
            okHttpClientBuilder.retryOnConnectionFailure(retryOnConnectionFailure);
            return this;
        }

        /**
        *	Do not perform any SSL checks (completely ignores all SSL-related errors)
        *
        * @return self
        */
        public RestClientActor.Builder skipSSLChecks() {
            try {
                // Create a trust manager that does not validate certificate chains
                final TrustManager[] trustAllCerts =
                        new TrustManager[]{
                                new X509TrustManager() {
                                    @Override
                                    public void checkClientTrusted(
                                            java.security.cert.X509Certificate[] chain,
                                            String authType) {
                                        // do not perform any checks for client
                                    }

                                    @Override
                                    public void checkServerTrusted(
                                            java.security.cert.X509Certificate[] chain,
                                            String authType) {
                                        // do not perform any checks for server
                                    }

                                    @Override
                                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                        return new java.security.cert.X509Certificate[]{};
                                    }
                                }
                        };

                        // Install the all-trusting trust manager
                        final SSLContext sslContext = SSLContext.getInstance("SSL");
                        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                        // Create an ssl socket factory with our all-trusting manager
                        final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
                        okHttpClientBuilder.sslSocketFactory(
                                sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
                        okHttpClientBuilder.hostnameVerifier((hostname, session) -> true);
            } catch (KeyManagementException | NoSuchAlgorithmException e) {
                        throw new PaxoRestException("Skipping SSL certificate checks failed: ", e);
            }
            return this;
        }

        /**
         *	Disable request/response body/headers logging
         *
         * @return self
         */
        public RestClientActor.Builder disableLogging() {
            this.enableLogging = false;
            return this;
        }
    }
}

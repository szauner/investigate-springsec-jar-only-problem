package com.company.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.iv.RandomIvGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.company.service.security.CustomJwtAccessTokenConverter;
import com.company.service.security.CustomTokenEnhancer;
import com.company.service.util.ApplicationPropertyAccess;
import com.company.service.util.MyStringUtils;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSecurity
@EnableAuthorizationServer
@EnableResourceServer
public class SecurityConfiguration extends WebSecurityConfigurerAdapter
implements AuthorizationServerConfigurer, ResourceServerConfigurer  {
    private final static Logger log = LogManager.getLogger(SecurityConfiguration.class);

    public final static String ASSOCIATED_CLIENT = "frontend";

    private final static String PASSWORD_ENDODING_ALGORITHM_ID_BCRYPT = "bcrypt";
    private final static String REALM = "INVESTIGATION_REALM";
    private final static String ASSOCIATED_CLIENT_SECRET
        = "{bcrypt}$2a$10$/rbWcV1DDh.ra40c2Kdwf.nJaGtRsm6EsGyDMwGnanHvex20rsPI."; // "test"

    public final static String ENHANCING_PROPERTY_USER_ID = "uid";
    public final static String ENHANCING_PROPERTY_APPLICATION_ROLE = "uar";

    @Autowired
    private ApplicationPropertyAccess applicationProperties;
    @Autowired
    @Qualifier("authenticationManagerBean")
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private CorsConfiguration corsConfig;

    public SecurityConfiguration() {
        // Registering Jackson for security object serialisation speeds up things like distributed sessions
        ObjectMapper mapper = new ObjectMapper();
        ClassLoader loader = getClass().getClassLoader();
        List<Module> modules = SecurityJackson2Modules.getModules(loader);
        mapper.registerModules(modules);
    }

    @Bean
    public DaoAuthenticationProvider authProvider() {
        final DaoAuthenticationProvider  authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setForcePrincipalAsString(true);
        return authProvider;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authProvider());
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeRequests()
            .antMatchers(HttpMethod.GET, "/service/user/public").permitAll()
            .antMatchers(HttpMethod.GET, "/service/user").hasAuthority("READ_DATA")
            .antMatchers(HttpMethod.PUT, "/service/user").hasAuthority("WRITE_DATA")
            .antMatchers(HttpMethod.DELETE, "/service/user").hasAuthority("WRITE_DATA")
            .antMatchers("/**").denyAll()
            .and()
            .httpBasic()
            // this enables a cors filter for all filter chains associated with the
            // HttpSecurity object given to this method which probably are all except for
            // the oauth2-filter-chain. Furthermore this probably renders the Jersey
            // CORS filter useless, what has yet to be checked though. Maybe it is still needed
            // for the permitAll()-filters above.
            .and()
            .addFilterAfter(createCorsFilter(), LogoutFilter.class);
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        String idForEncode = PASSWORD_ENDODING_ALGORITHM_ID_BCRYPT;
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put(PASSWORD_ENDODING_ALGORITHM_ID_BCRYPT, new BCryptPasswordEncoder());
        return new DelegatingPasswordEncoder(idForEncode, encoders);
    }

    @Bean
    public PooledPBEStringEncryptor strongEncryptor() {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        String algorithm = applicationProperties.getEncryptorAlgorithm();
        log.debug("Using the following algorithm for encryption: {}", algorithm);
        encryptor.setAlgorithm(algorithm);
        encryptor.setPoolSize(applicationProperties.getEncryptorPoolSize());
        encryptor.setPassword(applicationProperties.getEncryptorPassword());
        encryptor.setIvGenerator(new RandomIvGenerator());
        return encryptor;
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients
            .inMemory()
            .withClient(ASSOCIATED_CLIENT)
            .scopes("ALL")
            .secret(ASSOCIATED_CLIENT_SECRET)
            // Activating autoApprove is important so that the the user (resource owner) is not
            // asked which scopes he permits when logging in. That is not necessary because the
            // authentication and authorisation happens for the XConsult platform only.
            .autoApprove(true)
            .authorities("ROLE_CLIENT")
            .authorizedGrantTypes("refresh_token", "password", "authorization_code")
            .accessTokenValiditySeconds(applicationProperties.getOauth2AccessTokenExpirationTime())
            .refreshTokenValiditySeconds(applicationProperties.getOauth2RefreshTokenExpirationTime());
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
        oauthServer
            .realm(REALM)
            .checkTokenAccess("isAuthenticated()")
            .tokenKeyAccess("hasAuthority('TOKEN_MANAGEMENT')")
            // The following method enables us to configure the filter chain for requests to
            // the oauth2 endpoints. Any filter added via this method will be executed before
            // the BasicAuthenticationFilter.
            .addTokenEndpointAuthenticationFilter(createCorsFilter());
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
        tokenEnhancerChain.setTokenEnhancers(Arrays.asList(tokenEnhancer(), jwtTokenConverter()));

        endpoints
            .tokenStore(tokenStore())
            .tokenEnhancer(tokenEnhancerChain)
            .authenticationManager(authenticationManager)
            // the user details service has to be provided to the token endpoint for refresh_tokens to work
            // look here: https://github.com/spring-projects/spring-security-oauth/issues/813
            .userDetailsService(userDetailsService);
    }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.resourceId("service").tokenStore(tokenStore());
    }

    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(jwtTokenConverter());
    }

    @Bean
    protected JwtAccessTokenConverter jwtTokenConverter() {
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(new ClassPathResource("jwt.jks"), "myPassword".toCharArray());
        JwtAccessTokenConverter converter = new CustomJwtAccessTokenConverter();
        converter.setKeyPair(keyStoreKeyFactory.getKeyPair("myAlias", "myPassword".toCharArray()));
        return converter;
    }

    @Bean
    public TokenEnhancer tokenEnhancer() {
        return new CustomTokenEnhancer();
    }

    private CorsFilter createCorsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return new CorsFilter(source);
    }

    @Bean
    public CorsConfiguration corsConfiguration(Environment env) {
        CorsConfiguration corsConfig = new CorsConfiguration();

        corsConfig.setAllowedOrigins(MyStringUtils.convertCsvToList(env.getRequiredProperty("http.cors.allowedOrigins")));
        corsConfig.setAllowedMethods(MyStringUtils.convertCsvToList(env.getRequiredProperty("http.cors.allowedMethods")));
        // setAllowedHeaders is important! Without it, OPTIONS preflight request
        // will fail with 403 Invalid CORS request
        corsConfig.setAllowedHeaders(MyStringUtils.convertCsvToList(env.getRequiredProperty("http.cors.allowedHeaders")));
        corsConfig.setExposedHeaders(MyStringUtils.convertCsvToList(env.getRequiredProperty("http.cors.exposedHeaders")));
        // setAllowCredentials(true) can be important, otherwise:
        // The value of the 'Access-Control-Allow-Origin' header in the response must not be the wildcard
        // '*' when the request's credentials mode is 'include'.
        corsConfig.setAllowCredentials(Boolean.valueOf(env.getRequiredProperty("http.cors.allowCredentials")));
        corsConfig.setMaxAge(Long.valueOf(env.getRequiredProperty("http.cors.maxAge")));

        return corsConfig;
    }
}
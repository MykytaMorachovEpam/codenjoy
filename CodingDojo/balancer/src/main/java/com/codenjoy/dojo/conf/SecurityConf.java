package com.codenjoy.dojo.conf;

/*-
 * #%L
 * Codenjoy - it's a dojo-like platform from developers to developers.
 * %%
 * Copyright (C) 2018 - 2019 Codenjoy
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.codenjoy.dojo.web.rest.RestController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/**
 * @author Igor_Petrov@epam.com
 * Created at 4/8/2019
 */
@EnableWebSecurity
public class SecurityConf extends WebSecurityConfigurerAdapter {
    
    @Value("${admin.login}")
    private String adminLogin;

    @Value("${admin.password}")
    private String adminPassword;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        User.UserBuilder userBuilder = User.builder()
                .passwordEncoder(pwd -> passwordEncoder().encode(pwd));
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        manager.createUser(userBuilder.username(adminLogin).password(adminPassword).roles("ADMIN").build());
        return manager;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors()
                .and()
                    .authorizeRequests()
                        .antMatchers(
                                "/login",
                                "/logout",
                                RestController.URI + RestController.REGISTER,
                                RestController.URI + RestController.LOGIN)
                            .permitAll()
                
                        .antMatchers(
                                "/resources/html/admin.html",  
                                RestController.URI + "/**")
                            .hasRole("ADMIN")
                
                        .antMatchers(
                                RestController.URI + RestController.UPDATE,
                                RestController.URI + RestController.PLAYER)
                            .hasRole("USER")
                            // TODO надо как-то при создании юзера через /rest/register прописывать роль USER и сохранять в базе, затем сразу логинить
                            // TODO надо как-то при логине юзера через /rest/login акторизировать этого юзера в spring security                 
                
                        .anyRequest()
                            .denyAll()
                .and()
                    .headers()
                        .httpStrictTransportSecurity().maxAgeInSeconds(31536000)
                    .and()
                        .contentTypeOptions()
                    .and()
                        .contentSecurityPolicy(
                        "default-src 'self';" +
                                "script-src 'self' 'unsafe-eval' 'unsafe-inline' http://www.google-analytics.com;" +
                                "img-src 'self' data: http://www.google-analytics.com;" +
                                "connect-src 'self' ws: wss: http: https:;" +
                                "font-src 'self';" +
                                "style-src 'self' 'unsafe-inline';")
                    .and()
                .and()
                    .httpBasic()
                .and()
                    .formLogin()
                .and()
                .csrf().disable();

    }
}

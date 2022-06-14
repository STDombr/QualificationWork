package com.knu.service.web.manager;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import service.login.ClientInfoOuterClass;
import service.login.StatusOuterClass;

import java.util.ArrayList;

@Component
public class WebAuthenticationProvider implements AuthenticationProvider {

    private LoginManager loginManager = new LoginManager("localhost", "5577");

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String name = authentication.getName();
        String password = authentication.getCredentials().toString();

        StatusOuterClass.Status status = loginManager.signIn(ClientInfoOuterClass.ClientInfo.newBuilder()
                .setUsername(name)
                .setPassword(password)
                .build());

        if (status.getEnum() == StatusOuterClass.Status.Enum.SUCCESS) {
            ArrayList<GrantedAuthority> arrayList = new ArrayList<>();
            arrayList.add(new GrantedAuthority() {
                @Override
                public String getAuthority() {
                    return status.getClientId();
                }
            });

            return new UsernamePasswordAuthenticationToken(name, password, arrayList);
        } else {
            return null;
        }
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.equals(UsernamePasswordAuthenticationToken.class);
    }

}

package com.upcraft.keycloak.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class KeycloakRoleConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    @SuppressWarnings("unchecked")
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Set<String> roles = new HashSet<>();

        Object realmAccess = jwt.getClaims().get("realm_access");
        if (realmAccess instanceof Map<?, ?> realmMap) {
            Object realmRoles = realmMap.get("roles");
            if (realmRoles instanceof List<?> roleList) {
                roleList.forEach(role -> roles.add(String.valueOf(role)));
            }
        }

        Object resourceAccess = jwt.getClaims().get("resource_access");
        if (resourceAccess instanceof Map<?, ?> resourceMap) {
            for (Object clientValue : resourceMap.values()) {
                if (clientValue instanceof Map<?, ?> clientRoleMap) {
                    Object clientRoles = clientRoleMap.get("roles");
                    if (clientRoles instanceof List<?> roleList) {
                        roleList.forEach(role -> roles.add(String.valueOf(role)));
                    }
                }
            }
        }

        Collection<GrantedAuthority> authorities = roles.stream()
                .map(r -> "ROLE_" + r.toUpperCase())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        return new JwtAuthenticationToken(jwt, authorities, jwt.getClaimAsString("preferred_username"));
    }
}

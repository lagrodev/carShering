package org.example.carshering.it.all;

import org.example.carshering.security.ClientDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockClientDetails.Factory.class)
public @interface WithMockClientDetails {
    long id() default 1L;
    String username() default "testuser";
    String mail() default "testuser@mail.ru";
    String password() default "password";
    boolean banned() default false;
    boolean deleted() default false;
    String[] roles() default {"USER"};

    class Factory implements WithSecurityContextFactory<WithMockClientDetails> {
        @Override
        public SecurityContext createSecurityContext(WithMockClientDetails annotation) {
            SecurityContext context = SecurityContextHolder.createEmptyContext();

            ClientDetails principal = new ClientDetails(
                    annotation.id(),
                    annotation.username(),
                    annotation.password(),
                    annotation.mail(),
                    AuthorityUtils.createAuthorityList(annotation.roles()),
                    annotation.banned(), annotation.deleted()
            );

            // установите другие необходимые поля

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    AuthorityUtils.createAuthorityList(annotation.roles())
            );

            context.setAuthentication(auth);
            return context;
        }
    }
}

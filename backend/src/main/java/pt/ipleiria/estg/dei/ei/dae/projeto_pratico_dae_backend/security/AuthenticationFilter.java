package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.Priority;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ejbs.UserBean;
import javax.crypto.spec.SecretKeySpec;
import java.security.Principal;
@Provider
@Authenticated
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {
    @EJB
    private UserBean userBean;
    @Context
    private UriInfo uriInfo;
    @Context
    private ResourceInfo resourceInfo;
    @Override
    public void filter(ContainerRequestContext requestContext) {
        var method = resourceInfo.getResourceMethod();
        if (method != null && method.isAnnotationPresent(PermitAll.class)) {
            return;
        }
        
        var header = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            throw new
                    NotAuthorizedException("Authorization header must be provided");
        }
        String token = header.substring("Bearer".length()).trim();
        var user = userBean.find(getUsername(token));
        if (user == null) {
            throw new NotAuthorizedException("Invalid JWT user");
        }
        requestContext.setSecurityContext(new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                return () -> user.getUsername();
            }
            @Override
            public boolean isUserInRole(String s) {
                if (user == null || user.getRole() == null || user.getRole().getName() == null) return false;
                return user.getRole().getName().equals(s);
            }
            @Override
            public boolean isSecure() {
                return uriInfo.getAbsolutePath().toString().startsWith("https");}
            @Override
            public String getAuthenticationScheme() { return "Bearer"; }
        });
    }
    private String getUsername(String token) {
        var key = new SecretKeySpec(
                TokenIssuer.SECRET_KEY,
                SignatureAlgorithm.HS256.getJcaName()
        );

        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (Exception e) {
            throw new NotAuthorizedException("Invalid JWT");
        }
    }
}

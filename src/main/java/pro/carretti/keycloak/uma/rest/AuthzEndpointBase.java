package pro.carretti.keycloak.uma.rest;

import pro.carretti.keycloak.uma.util.ServerRequest;
import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.ClientAuthorizationContext;

public class AuthzEndpointBase {

    private static final Logger LOG = Logger.getLogger(AuthzEndpointBase.class.getName());
    
    @Context HttpServletRequest request;
    
    String impersonate(String username) throws IOException, org.keycloak.adapters.ServerRequest.HttpFailure {
        RefreshableKeycloakSecurityContext context = (RefreshableKeycloakSecurityContext) getKeycloakSecurityContext();
        return ServerRequest.invokeTokenExchange(context.getDeployment(), username).getToken();
    }
    
    AuthzClient getAuthzClient() {
        return getAuthorizationContext().getClient();
    }

    ClientAuthorizationContext getAuthorizationContext() {
        return ClientAuthorizationContext.class.cast(getKeycloakSecurityContext().getAuthorizationContext());
    }

    KeycloakSecurityContext getKeycloakSecurityContext() {
        return KeycloakSecurityContext.class.cast(request.getAttribute(KeycloakSecurityContext.class.getName()));
    }

}

package pro.carretti.keycloak.uma;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.keycloak.adapters.ServerRequest;
import org.keycloak.representations.idm.authorization.PermissionTicketRepresentation;
import org.keycloak.representations.idm.authorization.ResourceRepresentation;

@RequestScoped
public class StuffShareEndpoint extends AuthzEndpointBase {

    private static final Logger LOG = Logger.getLogger(StuffShareEndpoint.class.getName());

    private String id;

    public StuffShareEndpoint() {
    }

    void setID(String id) {
        this.id = id;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get() throws IOException, ServerRequest.HttpFailure {

        List<ResourceRepresentation> resources = getAuthzClient().protection().resource().findByUri("/stuff/" + id);
        ResourceRepresentation resource = resources.get(0);
        String sub = getKeycloakSecurityContext().getToken().getSubject();
        String owner = resource.getOwner().getId();
        String token = owner.equals(sub) ? getKeycloakSecurityContext().getTokenString() : impersonate(owner);

        List<PermissionTicketRepresentation> tickets = getAuthzClient().protection(token).permission().findByResource(resource.getId());

        return Response.ok(tickets).build();

    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response post(ShareRepresentation share) throws IOException, ServerRequest.HttpFailure {

        List<ResourceRepresentation> resources = getAuthzClient().protection().resource().findByUri("/stuff/" + id);
        ResourceRepresentation resource = resources.get(0);

        String sub = getKeycloakSecurityContext().getToken().getSubject();
        String owner = resource.getOwner().getId();
        String ownerName = resource.getOwner().getName();
        String token = owner.equals(sub) ? getKeycloakSecurityContext().getTokenString() : impersonate(owner);

        LOG.log(Level.FINE, "sub   = {0}", sub);
        LOG.log(Level.FINE, "owner = {0}", owner);

        for (String scope : share.getScopes()) {
            PermissionTicketRepresentation ticket = new PermissionTicketRepresentation();
            ticket.setOwner(owner);
            ticket.setOwner(ownerName);
            ticket.setRequester(share.getRequester());
            ticket.setRequesterName(share.getRequesterName());
            ticket.setScope(scope);
            ticket.setScopeName(scope);
            ticket.setResource(resource.getId());
            ticket.setResourceName(resource.getName());
            ticket.setGranted(true);
            getAuthzClient().protection(token).permission().create(ticket);
        }

        return Response.ok().build();

    }

    public static class ShareRepresentation {

        private String requester;
        private String requesterName;
        private List<String> scopes;

        public String getRequester() {
            return requester;
        }

        public void setRequester(String id) {
            this.requester = id;
        }

        public List<String> getScopes() {
            return scopes;
        }

        public void setScopes(List<String> scopes) {
            this.scopes = scopes;
        }

        public String getRequesterName() {
            return requesterName;
        }

        public void setRequesterName(String user) {
            this.requesterName = user;
        }

    }
}

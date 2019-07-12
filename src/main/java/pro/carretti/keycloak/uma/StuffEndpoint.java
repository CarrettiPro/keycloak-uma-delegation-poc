package pro.carretti.keycloak.uma;

import java.util.HashSet;
import java.util.logging.Logger;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.FormParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import org.keycloak.representations.idm.authorization.ResourceRepresentation;
import org.keycloak.representations.idm.authorization.ScopeRepresentation;

@Path("/stuff")
public class StuffEndpoint extends AuthzEndpointBase {

    private static final Logger LOG = Logger.getLogger(StuffEndpoint.class.getName());

    private final String SCOPE_STUFF_READ =   "stuff:read";
    private final String SCOPE_STUFF_WRITE =  "stuff:write";
    private final String SCOPE_STUFF_DELETE = "stuff:delete";
    private final String SCOPE_STUFF_SHARE =  "stuff:share";

    @Inject Instance<StuffShareEndpoint> _endpoint;

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public Response create(@FormParam("id") String id) {
        String rid = createProtectedResource(id);
        return Response.ok(id).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get() {
        String[] resources = getAuthzClient().protection().resource().findAll();
        return Response.ok(resources).build();
    }

    @Path("{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("id") String id) {
        ResourceRepresentation resource = getAuthzClient().protection().resource().findByUri("/stuff/" + id).get(0);
        return Response.ok(resource).build();
    }

    @Path("{id}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") String id) {
        ResourceRepresentation res = getAuthzClient().protection().resource().findByUri("/stuff/" + id).get(0);
        getAuthzClient().protection().resource().delete(res.getId());
        return Response.status(Status.NO_CONTENT).build();
    }


    @Path("{id}/share")
    public StuffShareEndpoint share(@PathParam("id") String id) throws InstantiationException, IllegalAccessException {
        if (getAuthorizationContext().hasScopePermission(SCOPE_STUFF_SHARE)) {
            StuffShareEndpoint endpoint = _endpoint.get();
            endpoint.setID(id);
            return endpoint;
        } else {
            throw new ForbiddenException();
        }
    }

    private String createProtectedResource(String id) {

        try {

            HashSet<ScopeRepresentation> scopes = new HashSet<>();

            scopes.add(new ScopeRepresentation(SCOPE_STUFF_READ));
            scopes.add(new ScopeRepresentation(SCOPE_STUFF_WRITE));
            scopes.add(new ScopeRepresentation(SCOPE_STUFF_DELETE));
            scopes.add(new ScopeRepresentation(SCOPE_STUFF_SHARE));

            ResourceRepresentation stuffResource = new ResourceRepresentation("Stuff " + id, scopes, "/stuff/" + id, "urn:carretti.pro:stuff");

            stuffResource.setOwner(request.getUserPrincipal().getName());
            stuffResource.setOwnerManagedAccess(true);

            ResourceRepresentation response = getAuthzClient().protection().resource().create(stuffResource);

            return response.getId();

        } catch (Exception e) {
            throw new RuntimeException("Could not register protected resource.", e);
        }

    }

}

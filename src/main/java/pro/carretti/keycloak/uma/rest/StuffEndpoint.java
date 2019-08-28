package pro.carretti.keycloak.uma.rest;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.keycloak.authorization.client.resource.ProtectionResource;
import org.keycloak.representations.idm.authorization.ResourceRepresentation;
import org.keycloak.representations.idm.authorization.ScopeRepresentation;

import pro.carretti.keycloak.uma.ErrorResponse;
import pro.carretti.keycloak.uma.entity.Stuff;

@RequestScoped
@Transactional
@Path("/stuff")
public class StuffEndpoint extends AuthzEndpointBase {

    private static final Logger LOG = Logger.getLogger(StuffEndpoint.class.getName());

    private final String SCOPE_STUFF_READ =   "stuff:read";
    private final String SCOPE_STUFF_WRITE =  "stuff:write";
    private final String SCOPE_STUFF_DELETE = "stuff:delete";
    private final String SCOPE_STUFF_SHARE =  "stuff:share";

    @PersistenceContext
    private EntityManager em;

    @Inject
    private Instance<StuffShareEndpoint> _endpoint;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(Stuff stuff) {

        Principal userPrincipal = request.getUserPrincipal();

        stuff.setId(UUID.randomUUID().toString());
        stuff.setUserId(userPrincipal.getName());

        Query queryDuplicatedStuff = this.em.createQuery("from Stuff where userId = :userId and description = :description");

        queryDuplicatedStuff.setParameter("description", stuff.getDescription());
        queryDuplicatedStuff.setParameter("userId", userPrincipal.getName());

        if (!queryDuplicatedStuff.getResultList().isEmpty()) {
            throw new ErrorResponse("Stuff [" + stuff.getDescription()+ "] already exists. Choose another one.", Status.CONFLICT);
        }

        try {
            this.em.persist(stuff);
            createProtectedResource(stuff);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Exception caught while creating protected resource", e);
            getAuthzClient().protection().resource().delete(stuff.getExternalId());
        }

        return Response.ok(stuff).build();

    }

    @GET
    @Produces("application/json")
    public Response findAll() {
        return Response.ok(this.em.createQuery("from Stuff where userId = :id").setParameter("id", request.getUserPrincipal().getName()).getResultList()).build();
    }

    @GET
    @Path("{id}")
    @Produces("application/json")
    public Response findById(@PathParam("id") String id) {

        List result = this.em.createQuery("from Stuff where id = :id").setParameter("id", id).getResultList();

        if (result.isEmpty()) {
            return Response.status(Status.NOT_FOUND).build();
        }

        return Response.ok(result.get(0)).build();
    }

    @Path("{id}")
    @DELETE
    public Response delete(@PathParam("id") String id) {

        Stuff stuff = this.em.find(Stuff.class, id);

        try {
            deleteProtectedResource(stuff);
            this.em.remove(stuff);
        } catch (Exception e) {
            throw new RuntimeException("Could not delete stuff.", e);
        }

        return Response.ok().build();
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

    private String createProtectedResource(Stuff stuff) {

        try {

            HashSet<ScopeRepresentation> scopes = new HashSet<>();

            scopes.add(new ScopeRepresentation(SCOPE_STUFF_READ));
            scopes.add(new ScopeRepresentation(SCOPE_STUFF_WRITE));
            scopes.add(new ScopeRepresentation(SCOPE_STUFF_DELETE));
            scopes.add(new ScopeRepresentation(SCOPE_STUFF_SHARE));

            ResourceRepresentation stuffResource = new ResourceRepresentation(stuff.getDescription(), scopes, "/stuff/" + stuff.getId(), "urn:carretti.pro:stuff");

            stuffResource.setOwner(request.getUserPrincipal().getName());
            stuffResource.setOwnerManagedAccess(true);

            ResourceRepresentation response = getAuthzClient().protection().resource().create(stuffResource);
            stuff.setExternalId(response.getId());

            return response.getId();

        } catch (Exception e) {
            throw new RuntimeException("Could not register protected resource.", e);
        }

    }

    private void deleteProtectedResource(Stuff stuff) {
        String uri = "/stuff/" + stuff.getId();

        try {
            ProtectionResource protection = getAuthzClient().protection();
            List<ResourceRepresentation> search = protection.resource().findByUri(uri);

            if (search.isEmpty()) {
                throw new RuntimeException("Could not find protected resource with URI [" + uri + "]");
            }

            protection.resource().delete(search.get(0).getId());
        } catch (RuntimeException e) {
            throw new RuntimeException("Could not search protected resource.", e);
        }
    }

}

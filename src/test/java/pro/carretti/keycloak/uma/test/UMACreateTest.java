package pro.carretti.keycloak.uma.test;

import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.keycloak.authorization.client.AuthorizationDeniedException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UMACreateTest extends UMATestBase {

    public UMACreateTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void t10_create() {
        do_create();
    }

    @Test
    public void t21_get() throws IOException {
        do_get(TOKEN1, 200);
    }

    @Test(expected = AuthorizationDeniedException.class)
    public void t22_get() throws IOException {
        do_get(TOKEN2, 403);
    }

    @Test(expected = AuthorizationDeniedException.class)
    public void t23_get() throws IOException {
        do_get(TOKEN3, 403);
    }

    @Test
    public void t41_get_share() throws IOException {
        do_get(TOKEN1, 200, "share");
    }

    @Test(expected = AuthorizationDeniedException.class)
    public void t42_get_share() throws IOException {
        do_get(TOKEN2, 403, "share");
    }

    @Test(expected = AuthorizationDeniedException.class)
    public void t43_get_share() throws IOException {
        do_get(TOKEN3, 403, "share");
    }

    @Test
    public void t99_delete() throws IOException {
        do_delete();
    }

}

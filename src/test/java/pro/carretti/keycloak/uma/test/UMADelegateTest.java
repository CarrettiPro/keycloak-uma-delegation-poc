package pro.carretti.keycloak.uma.test;

import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.keycloak.authorization.client.AuthorizationDeniedException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UMADelegateTest extends UMATestBase {

    public UMADelegateTest() {
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
    public void t20_share() throws IOException {
        do_share(TOKEN1, TOKEN2, "stuff:read", "stuff:write", "stuff:share");
    }

    @Test
    public void t21_get() {
        do_get(TOKEN1, 200);
    }

    @Test
    public void t22_get() {
        do_get(TOKEN2, 200);
    }

    @Test(expected = AuthorizationDeniedException.class)
    public void t23_get() {
        do_get(TOKEN3, 401);
    }

    @Test
    public void t41_get_share() {
        do_get(TOKEN1, 200, "share");
    }

    @Test
    public void t42_get_share() {
        do_get(TOKEN2, 200, "share");
    }

    @Test(expected = AuthorizationDeniedException.class)
    public void t43_get_share() {
        do_get(TOKEN3, 401, "share");
    }

    @Test
    public void t52_share2() throws IOException {
        do_share(TOKEN2, TOKEN3, "stuff:read");
    }

    @Test
    public void t53_get() {
        do_get(TOKEN3, 200);
    }

    @Test
    public void t99_delete() {
        do_delete();
    }

}

package pro.carretti.keycloak.uma.test;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import java.io.IOException;
import java.util.Arrays;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.common.util.Base64Url;
import org.keycloak.representations.AccessToken;
import org.keycloak.util.JsonSerialization;
import pro.carretti.keycloak.uma.StuffShareEndpoint;

public class UMATestBase {

    static final String USER1 = "beavis";
    static final String PASS1 = "beavis";

    static final String USER2 = "butthead";
    static final String PASS2 = "butthead";

    static final String USER3 = "daria";
    static final String PASS3 = "daria";

    static final String APPLICATION_URL = "http://localhost:8080/uma-poc";

    static RequestSpecification APP_RS;

    static String TOKEN1;
    static String TOKEN2;
    static String TOKEN3;

    static String RID = "1";

    static AuthzClient AUTHZ;

    @BeforeClass
    public static void setUpClass() {

        APP_RS = new RequestSpecBuilder()
                .setBaseUri(APPLICATION_URL)
                .build();

        AUTHZ = AuthzClient.create();

        TOKEN1 = token(USER1, PASS1);
        TOKEN2 = token(USER2, PASS2);
        TOKEN3 = token(USER3, PASS3);

    }

    @AfterClass
    public static void tearDownClass() {
    }

    void do_create() {

        RID = RestAssured
            .given()
                .spec(APP_RS)
                .auth()
                    .oauth2(TOKEN1)
            .when()
                .param("id", "1")
                .post("/stuff")
            .then()
                .assertThat()
                    .statusCode(200)
                .extract()
                    .asString();

    }

    void do_delete() {

        RestAssured
            .given()
                .spec(APP_RS)
                .auth()
                    .oauth2(rpt(TOKEN1))
            .when()
                .delete("/stuff/" + RID)
            .then()
                .assertThat()
                    .statusCode(204);

    }

    void do_get(String token, int rc, String... paths) {

        String url = String.join("/", ArrayUtils.addAll(new String[] { "/stuff", RID }, paths));

        RestAssured
            .given()
                .spec(APP_RS)
                .auth()
                    .oauth2(rpt(token))
            .when()
                .get(url)
            .then()
                .assertThat()
                    .statusCode(rc);

    }

    void do_share(String owner, String requester, String... scopes) {

        AccessToken token = parseToken(requester, AccessToken.class);
        StuffShareEndpoint.ShareRepresentation share = new StuffShareEndpoint.ShareRepresentation();
        share.setRequester(token.getSubject());
        share.setRequesterName(token.getPreferredUsername());
        share.setScopes(Arrays.asList(scopes));

        RestAssured
            .given()
                .spec(APP_RS)
                .auth()
                    .oauth2(rpt(owner))
            .when()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(share)
                .post("/stuff/" + RID + "/share")
            .then()
                .assertThat()
                    .statusCode(200);

    }

    static String token(String username, String password) {
        return AUTHZ.obtainAccessToken(username, password).getToken();
    }

    static String rpt(String token) {
        return AUTHZ.authorization(token).authorize().getToken();
    }

    // Just decode token without any verifications
    static <T> T parseToken(String encoded, Class<T> clazz) {

        if (encoded == null) {
            return null;
        }

        String[] parts = encoded.split("\\.");
        if (parts.length < 2 || parts.length > 3) {
            throw new IllegalArgumentException("Parsing error");
        }

        byte[] bytes = Base64Url.decode(parts[1]);

        try {
            return JsonSerialization.readValue(bytes, clazz);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected error during authorization request.", e);
        }

    }

}

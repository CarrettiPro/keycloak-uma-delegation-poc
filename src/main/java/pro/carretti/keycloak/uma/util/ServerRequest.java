package pro.carretti.keycloak.uma.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.keycloak.OAuth2Constants;
import org.keycloak.adapters.KeycloakDeployment;
import static org.keycloak.adapters.ServerRequest.error;
import org.keycloak.adapters.authentication.ClientCredentialsProviderUtils;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.util.JsonSerialization;

public class ServerRequest {

    public static AccessTokenResponse invokeTokenExchange(KeycloakDeployment deployment, String requestedSubject) throws IOException, org.keycloak.adapters.ServerRequest.HttpFailure {
        
        List<NameValuePair> formparams = new ArrayList<>();
        formparams.add(new BasicNameValuePair(OAuth2Constants.GRANT_TYPE, OAuth2Constants.TOKEN_EXCHANGE_GRANT_TYPE));
        formparams.add(new BasicNameValuePair(OAuth2Constants.REQUESTED_SUBJECT, requestedSubject));
        
        HttpPost post = new HttpPost(deployment.getTokenUrl());
        ClientCredentialsProviderUtils.setClientCredentials(deployment, post, formparams);

        UrlEncodedFormEntity form = new UrlEncodedFormEntity(formparams, "UTF-8");
        post.setEntity(form);
        HttpResponse response = deployment.getClient().execute(post);
        int status = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        if (status != 200) {
            error(status, entity);
        }
        if (entity == null) {
            throw new org.keycloak.adapters.ServerRequest.HttpFailure(status, null);
        }
        InputStream is = entity.getContent();
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            int c;
            while ((c = is.read()) != -1) {
                os.write(c);
            }
            byte[] bytes = os.toByteArray();
            String json = new String(bytes);
            try {
                return JsonSerialization.readValue(json, AccessTokenResponse.class);
            } catch (IOException e) {
                throw new IOException(json, e);
            }
        } finally {
            try {
                is.close();
            } catch (IOException ignored) {

            }
        }
        
    }
    
}

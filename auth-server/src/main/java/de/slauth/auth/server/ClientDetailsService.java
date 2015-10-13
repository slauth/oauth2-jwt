package de.slauth.auth.server;

import de.slauth.auth.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.stereotype.Component;

@Component
public class ClientDetailsService implements org.springframework.security.oauth2.provider.ClientDetailsService {

    @Autowired
    private ClientRepo clientRepo;

    @Override
    public Client loadClientByClientId(String clientId) {
        ClientEntity result = clientRepo.findOne(clientId);
        if (result == null) {
            throw new NoSuchClientException(String.format("Client '%s' does not exist", clientId));
        }
        return convert(result);
    }

    private Client convert(ClientEntity entity) {
        return new Client.Builder()
                .withId(entity.getId())
                .withSecret(entity.getSecret())
                .build();
    }
}

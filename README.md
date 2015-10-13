# oauth2-jwt
Sample [OAuth 2.0](http://oauth.net/2/) and [JSON Web Token](http://jwt.io/) integration.
Tokens are signed and verified with an RSA key – no need to hit the database for verifying incoming tokens.
Additionally, a [Hazelcast](http://hazelcast.org/) cluster is used to be able to revoke tokens already issued.

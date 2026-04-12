# Goal
create a spring boot 3 application to demo spring method argument resolver usage

## requirement
1. create a rest LoginController to take debitCard and password from request body, and channel from request header, returns a unique token string;
   - valid creditCard and password cannot be null/empty;
   - channel cannot be null / empty;
   - token is required in the subsequent requests' header accessToken, checking via PerRequestFilter to ensure each requests must have x-access-token except the LoginController;
   - the unique token will be used as a key to store creditCard and Channel into the Redis Server via docker compose;
2. Create a rest TransferController "/transfer" api which takes debitCard from custom method argument resolver, "fromAccount" and "toAccount" from request body
   - The customer PerRequestFilter will ensure the request header has x-access-token;
   - The custom method argument resolver for Debit Card will obtain from Redis server based on the access-token value obtained from request header "x-access-token"
   - if cannot find, throw business security exception;
3. create 2 different custom method argument resolvers to resolver debit card, channel based on x-access-token from request header


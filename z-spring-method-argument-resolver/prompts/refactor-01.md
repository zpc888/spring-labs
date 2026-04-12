# Goal
enhance the application to support different card to login

## requirement
1. change the login request debitCard to card type which is an enum CreditCard, DebitCard for now and card number which is string for now
2. create a new model CardTypeAndNumber to wrap it
3. For transfer api, it should auto resolve this CardTypeAndNumber via Custom method argument resolver
4. Change all the place using DebitCard to CardTypeAndNumber

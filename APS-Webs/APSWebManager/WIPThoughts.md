# Work In Progress Thoughts

## Frontend

### Model Service

- Frontend local.
- Clients register models with name and structure.
- Components subscribe to model values.
- Models can be told to fetch data from backend.
- Models can be updated by push from backend (usually as a response to 
  a request from frontend).

- (Lockable fields.)

### Component Locking (clients only)

- Make it possible for clients to lock / unlock certain input fields in
other clients.  
- Needs to handle page being loaded while field is locked.  
  - Use backend cluster store ? 
    - Backend component lock service ?

## Backend

- LogService (app global - should publish to!) 

## Backend & Frontend

- Need to update APS bus to register routes.

## General Rules

- Backend always stateless!
  - Clients must pass all data required for update or fetch of data.
- Clients always pass their address so that a possible reply message can
  be sent to that client.
- Let frontend be just GUI ?
  - ...
- Push updates to other clients ?
  - Create unique bus address for each data ?
  


# Proxy Backend

This backend demonstrates the Proxy design pattern in a practical way.

## Pattern used

The application exposes a protected access layer in front of a real service:

- `RealResourceService`: executes the actual business logic.
- `ProtectedResourceProxy`: controls access, validates permissions, and caches results.

This structure is useful when we need to:

- restrict access to sensitive data,
- reduce expensive calls,
- monitor and log operations,
- keep the real service independent from the access layer.

## Endpoints

- `GET /api/health`
- `GET /api/resources`
- `GET /api/resources/:id`

## Example access control

Use the request header `x-user-role` with values such as:

- `admin`
- `member`
- `guest`

A `guest` user only sees public resources. An `admin` user can view all information.

## Run locally

```bash
npm install
npm run dev
```

The API listens by default on port `4000`.

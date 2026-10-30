# AI Proxy Platform Backend

This backend implements the Proxy design pattern for a simulated AI API platform.

## Functional requirements covered

- `IAService` interface defines the generation contract.
- `MockAIGenerationService` simulates AI text generation with a delay and deterministic output.
- `RateLimitProxyService` limits requests per minute by user and plan.
- `QuotaProxyService` validates token quota by month and plan.
- The request flow passes through the rate limit proxy before the quota proxy.
- A scheduled task resets the rate-limit window each minute and resets monthly usage on the first day of the month.

## Plans

- FREE: 10 requests/minute, 50,000 tokens/month
- PRO: 60 requests/minute, 500,000 tokens/month
- ENTERPRISE: unlimited requests and quota

## API endpoints

### POST `/api/generate`
Creates a simulated AI response based on the prompt and user plan.

Request body:

```json
{
  "userId": "demo-user",
  "prompt": "Write a short summary for a software engineer.",
  "plan": "FREE"
}
```

### GET `/api/quota/status?userId={id}&plan={plan}`
Returns the current usage state and tokens remaining.

### GET `/api/quota/history?userId={id}`
Returns the last seven days of usage history.

### POST `/api/quota/update`
Updates the user plan.

```json
{
  "userId": "demo-user",
  "plan": "PRO"
}
```

## Local execution

```bash
mvn clean install
mvn spring-boot:run
```

The backend runs on port `8080` by default.

## Deployment

### Railway
1. Push the repository to GitHub.
2. Open Railway and create a new project.
3. Deploy from the backend repository.
4. Set the startup command:

```bash
mvn spring-boot:run
```

5. Set the port to `8080` in your project settings if required.

### Render
1. Create a new Web Service.
2. Connect the GitHub repository.
3. Use the following build command:

```bash
./mvnw clean install
```

4. Use the following start command:

```bash
java -jar target/proxy-platform-1.0.0.jar
```

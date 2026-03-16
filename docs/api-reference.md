# API Reference

The Task Manager exposes two REST endpoints. Both are available at `http://localhost:8080` during local development. An interactive Swagger UI is available at `http://localhost:8080/swagger-ui.html`.

## Endpoints

### Submit a Task

**`POST /api/tasks`**

Accepts a task payload, persists it with `PENDING` status, and enqueues it for async processing.

**Request body:**

```json
{
  "type": "<TASK_TYPE>",
  "payload": { ... }
}
```

**Response — 201 Created:**

```json
{
  "taskId": "8e87f12d-57b3-49af-b1cf-7b7df136955b"
}
```

Use the returned `taskId` to poll for the result.

---

### Get Task Status

**`GET /api/tasks/{taskId}`**

Returns the current state of a task.

**Response — pending:**

```json
{
  "id": "8e87f12d-57b3-49af-b1cf-7b7df136955b",
  "type": "CONVERT_CURRENCY",
  "payload": {
    "amount": 100.0,
    "fromCurrency": "EUR",
    "toCurrency": "USD"
  },
  "result": null,
  "status": "PENDING"
}
```

**Response — completed:**

```json
{
  "id": "8e87f12d-57b3-49af-b1cf-7b7df136955b",
  "type": "CONVERT_CURRENCY",
  "payload": {
    "amount": 100.0,
    "fromCurrency": "EUR",
    "toCurrency": "USD"
  },
  "result": "110.00",
  "status": "COMPLETED"
}
```

---

## Task Types

### `CONVERT_CURRENCY`

Converts an amount between currencies using mock exchange rates.

**Supported currencies:** `EUR`, `USD`, `GBP`

> Note: Exchange rates are mocked. A production implementation would integrate with a real exchange rate provider and support additional currencies.

**Payload schema:**

| Field | Type | Description |
|-------|------|-------------|
| `amount` | Double | Amount to convert |
| `fromCurrency` | String | Source currency code (`EUR`, `USD`, `GBP`) |
| `toCurrency` | String | Target currency code (`EUR`, `USD`, `GBP`) |

**Example:**

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "type": "CONVERT_CURRENCY",
    "payload": {
      "amount": 100.00,
      "fromCurrency": "EUR",
      "toCurrency": "USD"
    }
  }'
```

---

### `CALCULATE_INTEREST`

Calculates simple interest for a given principal, annual rate, and number of days.

**Formula:** `Interest = Principal × Rate × (Days / 365)`

**Payload schema:**

| Field | Type | Description |
|-------|------|-------------|
| `principal` | Double | Principal amount |
| `annualRate` | Double | Annual interest rate as a percentage (e.g. `5.5` for 5.5%) |
| `days` | Integer | Number of days |

**Example:**

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "type": "CALCULATE_INTEREST",
    "payload": {
      "principal": 1000.00,
      "annualRate": 5.5,
      "days": 90
    }
  }'
```

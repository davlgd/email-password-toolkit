# Email and Password Toolkit

REST API for email validation, password strength analysis, and secure password generation.

## Features

- Email validation (RFC compliant)
- Password strength analysis with entropy calculation
- Secure password generation (24-128 characters)
- Bearer token authentication
- OpenAPI/Swagger documentation

## Quick Start

```bash
sbt run
```

Server starts on `http://localhost:8080`
Documentation at `http://localhost:8080/docs`

## API Endpoints

**POST** `/valid/email` - Validate email format (requires auth)
**POST** `/valid/password` - Analyze password strength (requires auth)
**GET** `/generate/password?length=32` - Generate secure password (public)

## Stack

Scala 3.7.4 • http4s • Tapir • Circe • Passay

## License

Apache 2.0 - Copyright 2025 davlgd

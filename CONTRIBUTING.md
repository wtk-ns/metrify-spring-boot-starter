# Contributing to Metrify

Thank you for your interest in contributing to Metrify!

## Development Setup

1. Clone the repository
2. Ensure Java 21+ is installed
3. Build: `mvn clean install`
4. Run tests: `mvn test`

## Code Style

This project strictly follows the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html).

Key rules:
- 2-space indentation, no tabs
- 100-character column limit
- K&R brace style
- No wildcard imports

## Project Conventions

- **No nested classes** — every class must be in its own file
- **No comments in code** — code must be self-explanatory
- **Services**: interface in `service/`, implementation in `service/impl/`
- **DTOs**: in `model/dto/`
- **Enums**: in `model/enums/`
- All `@Bean` methods must have `@ConditionalOnMissingBean`
- All `@Bean` methods must return concrete types

## Testing

- Auto-configuration tests use `ApplicationContextRunner`
- AOP integration tests use `@SpringBootTest` with `SimpleMeterRegistry`
- Every test config and test service must be a separate top-level class

## Pull Requests

1. Fork the repository
2. Create a feature branch from `master`
3. Write tests for new functionality
4. Ensure all tests pass: `mvn test`
5. Submit a pull request with a clear description

## Reporting Issues

Open an issue on GitHub with:
- Steps to reproduce
- Expected vs actual behavior
- Spring Boot and Java version

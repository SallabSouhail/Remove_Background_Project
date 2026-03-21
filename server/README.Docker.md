### Local Docker run

1. Copy `.env.example` to `.env`.
2. Fill in your real database, Clerk, and Remove.bg values.
3. Start the app with `docker compose up --build`.

The API will be available at `http://localhost:8080`.

### Build the image manually

Use:
`docker build -t removebg-api .`

If you are building on a different CPU architecture than AWS, use:
`docker build --platform=linux/amd64 -t removebg-api .`

### Deploy to AWS

1. Build and tag the image for ECR.
2. Push it to your ECR repository.
3. Run it in ECS, App Runner, or Elastic Beanstalk.
4. Set the same environment variables from `.env.example` in the AWS service configuration.

Keep secrets in AWS Secrets Manager or SSM Parameter Store instead of committing them to git.

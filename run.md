I'll help you run this project locally and deploy documentation to GitHub Pages.

Running the Project Locally
This is a Java-based microservices project using Spring Boot, Kafka, PostgreSQL, and Redis. Here's how to run it:

1. Prerequisites Check
   First, verify you have the required tools installed:


java -version    # Should show Java 17
mvn -version     # Should show Maven 3.x
docker --version # Should show Docker
2. Start Infrastructure Services

cd ai-notification-platform
docker-compose up -d
Wait 30-60 seconds for all services (PostgreSQL, Redis, Kafka, Zookeeper) to be healthy:


docker-compose ps
3. Start the Microservices
   Open 3 separate terminal windows:

Terminal 1 - Notification Service (Port 8081):


cd ai-notification-platform/notification-service
mvn spring-boot:run
Terminal 2 - Worker Service:


cd ai-notification-platform/worker-service
mvn spring-boot:run
Terminal 3 - AI Service (Port 8083):


cd ai-notification-platform/ai-service
mvn spring-boot:run
4. Test the Setup

# Test notification service
curl http://localhost:8081/notifications/health

# Test AI service
curl http://localhost:8083/ai/health

# Create a notification
curl -X POST http://localhost:8081/notifications \
-H "Content-Type: application/json" \
-d '{
"recipient": "test@example.com",
"subject": "Test Notification",
"message": "Hello from the notification platform!",
"channel": "EMAIL"
}'
5. Monitor
   Kafka UI: http://localhost:8080
   Check logs in each terminal window
6. Stop Everything
   Press Ctrl+C in each terminal, then:


docker-compose down
Deploying Documentation to GitHub Pages
Now let me create a GitHub Pages documentation site for your project. I'll create a docs site with Jekyll that includes:

Project overview
Setup instructions
API documentation
Architecture details Now let me create an updated README in the architecture docs folder that references the GitHub Pages documentation:

0123456789
0123456789
actions taken
Perfect! I've created comprehensive documentation for your project. Here's what has been set up:

Summary
Running the Project Locally
The project requires Java 17, Maven, and Docker. Here's the quick start:

Start infrastructure:


cd ai-notification-platform
docker-compose up -d
Start services in 3 terminals:


cd notification-service && mvn spring-boot:run
cd worker-service && mvn spring-boot:run
cd ai-service && mvn spring-boot:run
Test:


curl -X POST http://localhost:8081/notifications \
-H "Content-Type: application/json" \
-d '{"recipient":"test@example.com","subject":"Test","message":"Hello","channel":"EMAIL"}'
GitHub Pages Documentation
Created a complete documentation site with Jekyll:

Files Created:

docs/index.md - Homepage with project overview
docs/getting-started.md - Installation and setup guide
docs/api-reference.md - Complete API documentation
docs/architecture.md - System architecture details
docs/development.md - Developer guide
docs/_config.yml - Jekyll configuration
docs/Gemfile - Ruby dependencies
.github/workflows/deploy-docs.yml - GitHub Actions workflow
docs/DEPLOYMENT.md - Deployment instructions
To Deploy:

Push to GitHub:


git init
git add .
git commit -m "Initial commit with documentation"
git remote add origin https://github.com/yourusername/ai-notification-platform.git
git push -u origin main
Enable GitHub Pages:

Go to repository Settings
Navigate to Pages
Under Source, select "GitHub Actions"
Update configuration:
Edit docs/_config.yml and replace yourusername with your GitHub username

Access documentation:
Your docs will be available at: https://yourusername.github.io/ai-notification-platform/

The GitHub Action automatically builds and deploys the documentation whenever you push changes to the docs/ folder.

Add local run & docs deployment setup

Version 2

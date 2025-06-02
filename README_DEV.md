# Developer Notes

```shell
# Build the Docker image
docker build -t datasahi-siyadb:0.1 -t datasahi-siyadb:latest .

# Docker Hub push (replace 'yourusername' with your actual Docker Hub username)
docker login
docker tag datasahi-siyadb:0.1 yourusername/datasahi-siyadb:0.1
docker tag datasahi-siyadb:0.1 yourusername/datasahi-siyadb:latest
docker push yourusername/datasahi-siyadb:0.1
docker push yourusername/datasahi-siyadb:latest
```

# Github actions workflow
**To set this up:**

Create GitHub Secrets:

Go to your GitHub repository
        Settings > Secrets and variables > Actions
        Add two secrets:
            DOCKERHUB_USERNAME: Your Docker Hub username
            DOCKERHUB_TOKEN: A personal access token from Docker Hub

   
2. Generate Docker Hub Token:
   Log in to Docker Hub
   Go to Account Settings
   Security > New Access Token
   Select appropriate permissions (Read, Write, Delete)
   Copy the generated token
   Store the workflow file:
   Create the file at .github/workflows/docker-image.yml in your repository
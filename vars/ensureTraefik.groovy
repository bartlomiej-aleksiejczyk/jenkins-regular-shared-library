def call() {
    script {
        // Retrieve the standard Traefik Docker network
        def networkName = env.STANDARD_TRAEFIK_DOCKER_NETWORK // Use environment variable correctly
        if (!networkName) {
            echo 'The STANDARD_TRAEFIK_DOCKER_NETWORK environment variable is not set.'
            return
        }
        echo "STANDARD_TRAEFIK_DOCKER_NETWORK: ${networkName}"

        // Check and create network if necessary
        sh """
        #!/bin/bash
        # Check if the specified network exists
        NETWORK_EXISTS=\$(docker network ls --filter "name=^${networkName}\$5" --format "{{ '{{.Name}}' }}")
        if [ "\$NETWORK_EXISTS" != "${networkName}" ]; then
            echo "Creating Docker network: ${networkName}"
            if docker network create ${networkName}; then
                echo "Docker network created successfully."
            else
                echo "Failed to create Docker network."
                exit 1
            fi
        else
            echo "Docker network '${networkName}' already exists."
        fi
        """

        // Start the Traefik container
        sh """
        #!/bin/bash
        RUNNING=\$(docker ps --filter "name=^/traefik$" --format "{{ '{{.Names}}' }}")
        if [ "\$RUNNING" != "traefik" ]; then
            echo "Starting Traefik container..."
            docker rm traefik || true
            if docker run -d --name traefik \\
                --restart=unless-stopped \\
                --network="${networkName}" \\
                -p 80:80 \\
                -p 8085:8080 \\
                -v /var/run/docker.sock:/var/run/docker.sock \\
                traefik:v3.0 \\
                --api.insecure=true \\
                --providers.docker \\
                --entrypoints.web.address=:80; then
                echo "Traefik container started successfully."
            else
                echo "Failed to start Traefik container."
                exit 1
            fi
        else
            echo "Traefik container is already running."
        fi
        """
    }
}

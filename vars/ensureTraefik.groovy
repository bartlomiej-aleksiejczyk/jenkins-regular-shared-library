def call() {
    script {
        // Retrieve the standard Traefik Docker network
        def networkName = STANDARD_TRAEFIK_DOCKER_NETWORK
        if (!networkName) {
            echo 'The STANDARD_TRAEFIK_DOCKER_NETWORK environment variable is not set.'
            
            return
        }
        echo STANDARD_TRAEFIK_DOCKER_NETWORK

        // Check and create network with given namme if necessary
        sh '''
        # Check if the specified network exists
        NETWORK_EXISTS=$(docker network ls --filter "name=^${networkName}$" --format "{{.Name}}")
        if [ "$NETWORK_EXISTS" != "${networkName}" ]; then
            echo "Creating Docker network: ${networkName}"
            docker network create ${networkName}
        else
            echo "Docker network '${networkName}' already exists."
        fi
        '''

        // Start the Traefik container
        sh '''
        RUNNING=$(docker ps --filter "name=^/traefik$" --format "{{.Names}}")
        if [ "$RUNNING" != "traefik" ]; then
            echo "Starting Traefik container..."
            docker rm traefik || true
            docker run -d --name traefik \
                --restart=unless-stopped \
                --network="${STANDARD_TRAEFIK_DOCKER_NETWORK}" \
                -p 80:80 \
                -p 8085:8080 \
                -v /var/run/docker.sock:/var/run/docker.sock \
                traefik:v3.0 \
                --api.insecure=true \
                --providers.docker \
                --entrypoints.web.address=:80
        else
            echo "Traefik container is already running."
        fi
        '''
    }
}

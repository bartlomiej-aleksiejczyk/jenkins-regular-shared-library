def call() {
    script {
        sh '''
        RUNNING=$(docker ps --filter "name=^/traefik$" --format "{{.Names}}")
        if [ "$RUNNING" != "traefik" ]; then
        echo "Starting Traefik container..."
        docker rm traefik || true
        docker run -d --name traefik \
            --restart=unless-stopped \
            -p 80:80 \
            -p 8085:8080 \
            -v /var/run/docker.sock:/var/run/docker.sock \
            traefik:v2.5 \
            --api.insecure=true \
            --providers.docker \
            --entrypoints.web.address=:80
        else
        echo "Traefik container is already running."
        fi
        '''
    }
}

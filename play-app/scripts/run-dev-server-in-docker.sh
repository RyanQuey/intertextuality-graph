DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

# assumes you already built the image using docker build
docker run -v $DIR:/app --entrypoint '/app/entrypoint.dev.sh' -p 9000:9000 intertextuality-graph
